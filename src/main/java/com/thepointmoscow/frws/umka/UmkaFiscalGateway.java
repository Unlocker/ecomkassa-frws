package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.*;
import com.thepointmoscow.frws.exceptions.FrwsException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static com.thepointmoscow.frws.AgentType.AGENT_TYPE_FFD_TAG;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Optional.ofNullable;

/**
 * Fiscal gateway using "umka" devices.
 *
 * @author unlocker
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class UmkaFiscalGateway implements FiscalGateway {

    private static final int SESSION_EXPIRED_ERROR = 136;
    private static final int SUMMARY_AMOUNT_DENOMINATOR = 1000;
    // Status modes.
    static final int STATUS_OPEN_SESSION = 2;
    static final int STATUS_EXPIRED_SESSION = 3;
    static final int STATUS_CLOSED_SESSION = 4;

    private final String umkaHost;
    private final int umkaPort;
    private final BuildProperties buildProperties;
    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

    private volatile RegInfo lastStatus;

    /**
     * Makes an URL using the host, port and an ending path.
     *
     * @param endingPath ending path
     * @return URL
     */
    private String makeUrl(String endingPath) {
        return String.format("http://%s:%s/%s", getUmkaHost(), getUmkaPort(), endingPath);
    }

    /**
     * Prepares a status object.
     *
     * @return status
     */
    private StatusResult prepareStatus() {
        return new StatusResult().setAppVersion(getBuildProperties().getVersion());
    }

    @Override
    public RegistrationResult register(Order order, Long issueID, boolean openSession) {
        if (openSession) {
            openSession();
        }

        boolean isCorrection = SaleCharge.valueOf(order.getSaleCharge()).isCorrection();
        Map<String, Object> request = isCorrection ? correctionOrder(order, issueID) : regularOrder(order, issueID);

        String responseStr = getRestTemplate().postForObject(
                makeUrl("fiscalcheck.json"),
                new HttpEntity<>(request, generateHttpHeaders()),
                String.class);
        RegistrationResult registration = new RegistrationResult().apply(status());
        try {
            JsonNode response = mapper.readTree(responseStr);
            val propsArr = response.path("document").path("data").path("fiscprops").iterator();
            val codes = new HashSet<Integer>(Arrays.asList(1040, 1042));
            val values = new HashMap<Integer, Integer>();
            Optional<ZonedDateTime> regDate = Optional.empty();
            Optional<String> signature = Optional.empty();
            while (propsArr.hasNext()) {
                val current = propsArr.next();
                final int tag = current.get("tag").asInt();
                if (1012 == tag) {
                    regDate = Optional.of(
                            OffsetDateTime.parse(current.get("value").asText()
                                    , RFC_1123_DATE_TIME
                            ).toZonedDateTime()
                    );
                    continue;
                }
                if (1077 == tag) {
                    signature = Optional.of(current.get("value").asText());
                    continue;
                }
                if (codes.contains(tag)) {
                    values.put(tag, current.get("value").asInt());
                }
            }
            val documentNumber = ofNullable(values.get(1040));
            val sessionCheck = ofNullable(values.get(1042));
            if (Stream.of(regDate, signature, documentNumber, sessionCheck)
                    .anyMatch(opt -> !opt.isPresent())) {
                throw new FrwsException(
                        String.format(
                                "There is missed one or several required attributes for ORDER_ID=%s, ISSUE_ID=%s."
                                , order.get_id()
                                , issueID
                        )
                );
            }
            val regInfo = new RegistrationResult.Registration()
                    .setIssueID(issueID.toString())
                    .setRegDate(regDate.get())
                    .setDocNo(documentNumber.get().toString())
                    .setSignature(signature.get())
                    .setSessionCheck(sessionCheck.get());
            return registration.setRegistration(regInfo);
        } catch (Exception e) {
            log.error("Error parsing the response: {} | {}", responseStr, e.getMessage());
            if (e instanceof FrwsException) {
                throw (FrwsException) e;
            } else {
                throw new FrwsException(e);
            }
        }
    }

    /**
     * Makes a regular order.
     *
     * @param order   order
     * @param issueID issue ID
     * @return codified order
     */
    private Map<String, Object> regularOrder(Order order, Long issueID) {
        val doc = new FiscalDoc();
        doc.setPrint(1);
        doc.setSessionId(issueID.toString());
        FiscalData data = new FiscalData();
        doc.setData(data);
        data.setDocName("Кассовый чек");
        val paymentType = order.getPayments().stream()
                .findFirst()
                .map(Order.Payment::getPaymentType)
                .map(PaymentType::valueOf)
                .orElse(PaymentType.CASH);

        data.setMoneyType(paymentType.getCode());
        data.setType(SaleChargeGeneral.valueOf(order.getSaleCharge()).getCode());
        data.setSum(0);
        val tags = new ArrayList<FiscalProperty>();
        data.setFiscprops(tags);

        val info = getLastStatus();
        // Registration number, Tax identifier, Tax Variant
        tags.add(new FiscalProperty().setTag(1037).setValue(info.getRegNumber()));
        tags.add(new FiscalProperty().setTag(1018).setValue(info.getInn()));
        tags.add(new FiscalProperty().setTag(1055).setValue(info.getTaxVariant()));
        // check total
        order.getPayments().stream()
                .map(payment -> new FiscalProperty()
                        .setTag(PaymentType.valueOf(payment.getPaymentType()).getTag())
                        .setValue(payment.getAmount())
                )
                .forEach(tags::add);

        // Sale Charge
        tags.add(new FiscalProperty().setTag(1054)
                .setValue(SaleCharge.valueOf(order.getSaleCharge()).getCode()));
        // customer id: email or phone
        ofNullable(order.getCustomer().getId())
                .map(customer -> new FiscalProperty().setTag(1008).setValue(customer))
                .ifPresent(tags::add);

        for (Order.Item i : order.getItems()) {
            List<FiscalProperty> itemTags = new LinkedList<>();
            PaymentMethod paymentMethod = i.paymentMethod();
            itemTags.add(
                    new FiscalProperty()
                            .setTag(paymentMethod.getFfdTag())
                            .setValue(paymentMethod.getCode())
            );
            PaymentObject paymentObject = i.paymentObject();
            itemTags.add(
                    new FiscalProperty()
                            .setTag(paymentObject.getFfdTag())
                            .setValue(paymentObject.getCode())
            );
            itemTags.add(new FiscalProperty().setTag(1030).setValue(i.getName()));
            itemTags.add(new FiscalProperty().setTag(1079).setValue(i.getPrice()));
            itemTags.add(new FiscalProperty().setTag(1023)
                    .setValue(String.format("%.3f", ((double) i.getAmount()) / SUMMARY_AMOUNT_DENOMINATOR)));
            itemTags.add(new FiscalProperty().setTag(1199)
                    .setValue(ItemVatType.valueOf(i.getVatType()).getCode()));
            val total = i.getAmount() * i.getPrice() / SUMMARY_AMOUNT_DENOMINATOR;
            itemTags.add(new FiscalProperty().setTag(1043).setValue(total));
            ofNullable(i.getMeasurementUnit())
                    .map(it -> new FiscalProperty().setTag(1197).setValue(it))
                    .ifPresent(itemTags::add);
            ofNullable(i.getUserData())
                    .map(it -> new FiscalProperty().setTag(1191).setValue(it))
                    .ifPresent(itemTags::add);
            // supplier information
            ofNullable(i.getSupplier()).ifPresent(suppInfo -> {
                List<FiscalProperty> suppProps = new LinkedList<>();
                ofNullable(suppInfo.getSupplierPhones()).ifPresent(
                        phones -> phones.forEach(
                                phone -> suppProps.add(new FiscalProperty().setTag(1171).setValue(phone))
                        )
                );
                ofNullable(suppInfo.getSupplierName()).ifPresent(
                        it -> suppProps.add(new FiscalProperty().setTag(1225).setValue(it))
                );
                ofNullable(suppInfo.getSupplierInn()).ifPresent(
                        it -> suppProps.add(new FiscalProperty().setTag(1226).setValue(it))
                );
                itemTags.add(new FiscalProperty().setTag(1224).setFiscprops(suppProps));
            });
            // agent information
            ofNullable(i.getAgent()).ifPresent(agent -> {
                ofNullable(agent.getAgentType())
                        .map(agentType -> new FiscalProperty().setTag(AGENT_TYPE_FFD_TAG).setValue(agentType.getFfdCode()))
                        .ifPresent(itemTags::add);
                List<FiscalProperty> agentProps = new LinkedList<>();
                ofNullable(agent.getPayingOperation())
                        .map(operation -> new FiscalProperty().setTag(1044).setValue(operation))
                        .ifPresent(agentProps::add);
                ofNullable(agent.getPayingPhones())
                        .map(phones -> phones.stream().map(phone -> new FiscalProperty().setTag(1073).setValue(phone)))
                        .ifPresent(phoneProps -> phoneProps.forEach(agentProps::add));
                ofNullable(agent.getReceiverPhones())
                        .map(phones -> phones.stream().map(phone -> new FiscalProperty().setTag(1074).setValue(phone)))
                        .ifPresent(phoneProps -> phoneProps.forEach(agentProps::add));
                ofNullable(agent.getTransferPhones())
                        .map(phones -> phones.stream().map(phone -> new FiscalProperty().setTag(1075).setValue(phone)))
                        .ifPresent(phoneProps -> phoneProps.forEach(agentProps::add));
                ofNullable(agent.getTransferName())
                        .map(value -> new FiscalProperty().setTag(1026).setValue(value))
                        .ifPresent(agentProps::add);
                ofNullable(agent.getTransferAddress())
                        .map(value -> new FiscalProperty().setTag(1005).setValue(value))
                        .ifPresent(agentProps::add);
                ofNullable(agent.getTransferInn())
                        .map(operation -> new FiscalProperty().setTag(1016).setValue(operation))
                        .ifPresent(agentProps::add);
                itemTags.add(new FiscalProperty().setTag(1223).setFiscprops(agentProps));
            });

            val item = new FiscalProperty().setTag(1059).setFiscprops(itemTags);
            tags.add(item);
        }
        tags.add(new FiscalProperty().setTag(1060).setValue("www.nalog.ru"));
        Map<String, Object> request = new HashMap<>();
        request.put("document", doc);
        return request;
    }

    /**
     * Makes a correction order.
     *
     * @param order   order
     * @param issueId issue ID
     * @return codified order
     */
    private Map<String, Object> correctionOrder(Order order, Long issueId) {
        val doc = new FiscalDoc();
        doc.setPrint(1);
        doc.setSessionId(issueId.toString());
        FiscalData data = new FiscalData();
        doc.setData(data);
        data.setDocName("Чек коррекции");
        val paymentType = order.getPayments().stream()
                .findFirst()
                .map(Order.Payment::getPaymentType)
                .map(PaymentType::valueOf)
                .orElse(PaymentType.CASH);

        data.setMoneyType(paymentType.getCode());
        data.setType(SaleChargeGeneral.valueOf(order.getSaleCharge()).getCode());
        data.setSum(0);
        val tags = new ArrayList<FiscalProperty>();
        data.setFiscprops(tags);
        val info = getLastStatus();
        // Registration number, Tax identifier, Tax Variant
        tags.add(new FiscalProperty().setTag(1037).setValue(info.getRegNumber()));
        tags.add(new FiscalProperty().setTag(1018).setValue(info.getInn()));
        tags.add(new FiscalProperty().setTag(1055).setValue(info.getTaxVariant()));
        // check total
        order.getPayments().stream()
                .map(payment -> new FiscalProperty()
                        .setTag(PaymentType.valueOf(payment.getPaymentType()).getTag())
                        .setValue(payment.getAmount())
                )
                .forEach(tags::add);

        // Sale Charge
        tags.add(new FiscalProperty().setTag(1054).setValue(SaleCharge.valueOf(order.getSaleCharge()).getCode()));
        if (order.getCorrection() == null) {
            throw new FrwsException(
                    String.format(
                            "Correction cannot be empty for ORDER_ID=%s, ISSUE_ID=%s"
                            , order.get_id()
                            , issueId
                    )
            );
        }
        val correction = order.getCorrection();
        tags.add(new FiscalProperty().setTag(1173).setValue("SELF_MADE".equals(correction.getCorrectionType()) ? 0 : 1));
        FiscalProperty corrTag = new FiscalProperty().setTag(1174).setFiscprops(new ArrayList<>());
        corrTag.getFiscprops().add(new FiscalProperty().setTag(1177).setValue(correction.getDescription()));
        corrTag.getFiscprops().add(new FiscalProperty().setTag(1178).setValue(
                LocalDate.parse(correction.getDocumentDate())
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .format(RFC_1123_DATE_TIME))
        );
        corrTag.getFiscprops().add(new FiscalProperty().setTag(1179).setValue(correction.getDocumentNumber()));
        tags.add(corrTag);
        Map<String, Object> request = new HashMap<>();
        request.put("document", doc);
        return request;
    }

    /**
     * Makes HTTP headers for JSON request.
     *
     * @return headers
     */
    private HttpHeaders generateHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    public StatusResult openSession() {
        getRestTemplate().getForObject(makeUrl("cycleopen.json?print=1"), String.class);
        return status();
    }

    @Override
    public StatusResult closeSession() {
        getRestTemplate().getForObject(makeUrl("cycleclose.json?print=1"), String.class);
        return status();
    }

    @Override
    public StatusResult cancelCheck() {
        throw new UnsupportedOperationException("cancelCheck");
    }

    @Override
    public StatusResult status() {
        String responseStr = getRestTemplate().getForObject(makeUrl("cashboxstatus.json"), String.class);
        val result = prepareStatus();
        JsonNode response;
        try {
            response = mapper.readTree(responseStr);

            val status = ofNullable(response.get("cashboxStatus"));
            result.setErrorCode(0);
            result.setCurrentDocNumber(
                    status.map(x -> x.path("fsStatus").path("lastDocNumber")).filter(JsonNode::isInt).map(JsonNode::asInt)
                            .orElse(-1));
            result.setCurrentSession(
                    status.map(x -> x.path("cycleNumber")).filter(JsonNode::isInt).map(JsonNode::asInt).orElse(-1));
            final Optional<OffsetDateTime> timestamp = status.map(x -> x.get("dt").asText())
                    .map(x -> OffsetDateTime.parse(x, RFC_1123_DATE_TIME));

            result.setFrDateTime(timestamp.map(OffsetDateTime::toLocalDateTime).orElse(LocalDateTime.MIN));
            result.setOnline(true);
            final String inn = status.map(x -> x.path("userInn")).filter(node -> !node.isMissingNode())
                    .map(JsonNode::asText).orElse("");
            result.setInn(inn);
            final String regNumber = status.map(x -> x.get("regNumber").asText()).orElse("");
            final int taxVariant = status.map(x -> x.path("taxes")).filter(JsonNode::isInt).map(JsonNode::asInt).orElse(0);

            boolean isOpen = status.map(x -> x.path("fsStatus").path("cycleIsOpen"))
                    .map(x -> x.isInt() && x.asInt() != 0).orElse(false);

            final Optional<OffsetDateTime> opened = status.map(x -> x.path("cycleOpened"))
                    .filter(JsonNode::isTextual)
                    .map(x -> OffsetDateTime.parse(x.asText(), RFC_1123_DATE_TIME));

            result.setModeFR(statusMode(isOpen, timestamp, opened));
            result.setSubModeFR(0);
            result.setSerialNumber(status.map(x -> x.get("serial").asText()).orElse(""));
            result.setStatusMessage(ofNullable(response.path("message")).map(JsonNode::asText).orElse(""));
            result.setStatus(response);
            this.lastStatus = new RegInfo(inn, taxVariant, regNumber);
            return result;
        } catch (Exception e) {
            log.error("Error while reading a cashbox status. {}", e.getMessage());
            throw new FrwsException(e);
        }
    }

    /**
     * Calculates status mode code.
     *
     * @param isOpen is session open
     * @param ts     timestamp
     * @param opened date session opened
     * @return status mode code
     */
    private int statusMode(boolean isOpen, Optional<OffsetDateTime> ts, Optional<OffsetDateTime> opened) {
        if (!isOpen) {
            return STATUS_CLOSED_SESSION;
        }
        if (ts.isPresent() && opened.isPresent()
                && Duration.between(opened.get(), ts.get()).toMinutes() >= 60 * 24) {
            return STATUS_EXPIRED_SESSION;
        }
        return STATUS_OPEN_SESSION;
    }

    /**
     * Reads last status.
     *
     * @return last status
     */
    private RegInfo getLastStatus() {
        if (lastStatus == null) {
            synchronized (this) {
                if (lastStatus == null) {
                    status();
                }
            }
        }
        return lastStatus;
    }

    @Override
    public StatusResult continuePrint() {
        throw new UnsupportedOperationException("continuePrint");
    }
}

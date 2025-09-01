package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.*;
import com.thepointmoscow.frws.exceptions.FiscalException;
import com.thepointmoscow.frws.exceptions.FrwsException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.thepointmoscow.frws.AgentType.AGENT_TYPE_FFD_TAG;
import static com.thepointmoscow.frws.umka.FiscalProperty.array;
import static com.thepointmoscow.frws.umka.FiscalProperty.simple;
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
    private static final Random RANDOM = new Random();
    // Status modes.
    static final int STATUS_OPEN_SESSION = 2;
    static final int STATUS_EXPIRED_SESSION = 3;
    static final int STATUS_CLOSED_SESSION = 4;
    /**
     * The maximal length of an item name.
     */
    private static final int MAX_ITEM_NAME_LENGTH = 128;

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
                makeUrl("fiscalcheck.json")
                , new HttpEntity<>(request, generateHttpHeaders())
                , String.class
        );

        RegistrationResult registration = new RegistrationResult();
        try {
            JsonNode response = mapper.readTree(responseStr);
            int errorCode = ofNullable(response.path("document").path("result"))
                    .filter(JsonNode::isInt)
                    .map(JsonNode::asInt)
                    .orElse(0);

            if (errorCode != 0) {
                String errorMessage = ofNullable(response.path("document").path("message").path("resultDescription"))
                        .filter(JsonNode::isTextual)
                        .map(JsonNode::asText)
                        .orElseGet(() -> String.format("An error with code %d has no description", errorCode));
                throw new FiscalException(new FiscalResultError(errorCode, errorMessage));
            }
            final var propsArr = response.path("document").path("data").path("fiscprops").iterator();
            final var codes = new HashSet<>(Arrays.asList(1040, 1042, 1038));
            final var values = new HashMap<Integer, Integer>();
            Optional<ZonedDateTime> regDate = Optional.empty();
            Optional<String> signature = Optional.empty();
            Optional<String> inn = Optional.empty();
            while (propsArr.hasNext()) {
                final var current = propsArr.next();
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
                if (1018 == tag) {
                    inn = Optional.of(current.get("value")).map(JsonNode::asText);
                    continue;
                }
                if (codes.contains(tag)) {
                    values.put(tag, current.get("value").asInt());
                }
            }
            final var documentNumber = ofNullable(values.get(1040));
            final var sessionCheck = ofNullable(values.get(1042));
            final var currentSession = ofNullable(values.get(1038));
            if (Stream.of(regDate, signature, documentNumber, sessionCheck, currentSession)
                    .anyMatch(Optional::isEmpty)) {
                throw new FrwsException(
                        String.format(
                                "There is missed one or several required attributes for ORDER_ID=%s, ISSUE_ID=%s."
                                , order.get_id()
                                , issueID
                        )
                );
            }
            StatusResult statusResult = new StatusResult()
                    .setFrDateTime(regDate.get().toLocalDateTime())
                    .setInn(inn.orElse(getLastStatus().getInn()))
                    .setOnline(true)
                    .setSerialNumber(getLastStatus().getSerialNumber())
                    .setModeFR(2)
                    .setSubModeFR(0)
                    .setErrorCode(0)
                    .setStatusMessage(null)
                    .setStatus(null)
                    .setCurrentSession(currentSession.get())
                    .setCurrentDocNumber(documentNumber.get())
                    .setAppVersion(buildProperties.getVersion());

            final var regInfo = new RegistrationResult.Registration()
                    .setIssueID(issueID.toString())
                    .setRegDate(regDate.get())
                    .setDocNo(documentNumber.get().toString())
                    .setSignature(signature.get())
                    .setSessionCheck(sessionCheck.get());
            return registration.setRegistration(regInfo).apply(statusResult);
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
    Map<String, Object> regularOrder(Order order, Long issueID) {
        final var doc = new FiscalDoc();
        doc.setPrint(1);
        doc.setSessionId(issueID.toString());
        FiscalData data = prepareFiscalData(order);
        doc.setData(data);
        data.setDocName("Кассовый чек");
        final var tags = new ArrayList<FiscalProperty>();
        data.setFiscprops(tags);

        final var info = getLastStatus();
        // Registration number, Tax identifier, Tax Variant
        final Order.Firm firm = order.getFirm();
        tags.add(simple(1037, info.getRegNumber()));
        tags.add(simple(1018, firm.getTaxIdentityNumber()));
        tags.add(simple(1187, firm.getAddress()));
        tags.add(simple(1055, firm.getTaxVariant().getFfdCode()));
        // sets a cashier name
        ofNullable(order.getCashier())
                .map(Objects::toString)
                .map(name -> simple(1021, name))
                .ifPresent(tags::add);
        tags.addAll(processPaymentTags(order));
        // Sale Charge
        tags.add(simple(1054, SaleCharge.valueOf(order.getSaleCharge()).getCode()));

        // == Customer attributes ==
        final Optional<Order.Customer> maybeCustomer = ofNullable(order.getCustomer());
        // customer id: email or phone
        maybeCustomer.map(Order.Customer::getId)
                .map(customerId -> simple(1008, customerId))
                .ifPresent(tags::add);
        // customer name
        maybeCustomer.map(Order.Customer::getName)
                .map(customerName -> simple(1227, customerName))
                .ifPresent(tags::add);
        // customer tax number
        maybeCustomer.map(Order.Customer::getTaxNumber)
                .map(customerTaxNo -> simple(1228, customerTaxNo))
                .ifPresent(tags::add);
        // additional property
        ofNullable(order.getAdditionalCheckProperty())
                .map(prop -> simple(1192, prop))
                .ifPresent(tags::add);

        for (Order.Item i : order.getItems()) {
            List<FiscalProperty> itemTags = new LinkedList<>();
            PaymentMethod paymentMethod = i.paymentMethod();
            itemTags.add(simple(paymentMethod.getFfdTag(), paymentMethod.getCode()));
            PaymentObject paymentObject = i.paymentObject();
            itemTags.add(simple(paymentObject.getFfdTag(), paymentObject.getCode()));
            Optional.of(i.getName())
                    .map(name -> (name.length() <= MAX_ITEM_NAME_LENGTH ? name : name.substring(0, MAX_ITEM_NAME_LENGTH)))
                    .map(name -> simple(1030, name))
                    .ifPresent(itemTags::add);
            itemTags.add(simple(1079, i.getPrice()));
            itemTags.add(
                    simple(1023, String.format("%.3f", ((double) i.getAmount()) / SUMMARY_AMOUNT_DENOMINATOR))
            );
            itemTags.add(simple(1199, i.getVatType().getCode()));
            final var total = i.getAmount() * i.getPrice() / SUMMARY_AMOUNT_DENOMINATOR;
            itemTags.add(simple(1043, total));
            ofNullable(i.getMeasurementUnit())
                    .map(it -> simple(1197, it))
                    .ifPresent(itemTags::add);
            ofNullable(i.getUserData())
                    .map(it -> simple(1191, it))
                    .ifPresent(itemTags::add);
            ofNullable(i.getNomenclatureCode())
                    .map(it -> simple(1162, it))
                    .ifPresent(itemTags::add);

            // supplier information
            ofNullable(i.getSupplier()).ifPresent(suppInfo -> {
                List<FiscalProperty> suppProps = new LinkedList<>();
                ofNullable(suppInfo.getSupplierPhones()).ifPresent(
                        phones -> phones.forEach(
                                phone -> suppProps.add(simple(1171, phone))
                        )
                );
                ofNullable(suppInfo.getSupplierName()).ifPresent(it -> suppProps.add(simple(1225, it)));
                itemTags.add(array(1224, suppProps));

                // supplier tax number writes directly to item tags
                ofNullable(suppInfo.getSupplierInn())
                        .map(it -> simple(1226, it))
                        .ifPresent(itemTags::add);
            });

            // agent information
            ofNullable(i.getAgent()).ifPresent(agent -> {
                ofNullable(agent.getAgentType())
                        .map(agentType -> simple(AGENT_TYPE_FFD_TAG, agentType.getFfdCode()))
                        .ifPresent(itemTags::add);
                List<FiscalProperty> agentProps = new LinkedList<>();
                ofNullable(agent.getPayingOperation())
                        .map(operation -> simple(1044, operation))
                        .ifPresent(agentProps::add);
                ofNullable(agent.getPayingPhones())
                        .map(phones -> phones.stream().map(phone -> simple(1073, phone)))
                        .ifPresent(phoneProps -> phoneProps.forEach(agentProps::add));
                ofNullable(agent.getReceiverPhones())
                        .map(phones -> phones.stream().map(phone -> simple(1074, phone)))
                        .ifPresent(phoneProps -> phoneProps.forEach(agentProps::add));
                ofNullable(agent.getTransferPhones())
                        .map(phones -> phones.stream().map(phone -> simple(1075, phone)))
                        .ifPresent(phoneProps -> phoneProps.forEach(agentProps::add));
                ofNullable(agent.getTransferName())
                        .map(value -> simple(1026, value))
                        .ifPresent(agentProps::add);
                ofNullable(agent.getTransferAddress())
                        .map(value -> simple(1005, value))
                        .ifPresent(agentProps::add);
                ofNullable(agent.getTransferInn())
                        .map(operation -> simple(1016, operation))
                        .ifPresent(agentProps::add);
                itemTags.add(array(1223, agentProps));
            });

            final var item = array(1059, itemTags);
            tags.add(item);
        }
        tags.add(simple(1060, "www.nalog.ru"));
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
    Map<String, Object> correctionOrder(Order order, Long issueId) {
        final var doc = new FiscalDoc();
        doc.setPrint(1);
        doc.setSessionId(issueId.toString());
        FiscalData data = prepareFiscalData(order);
        doc.setData(data);
        data.setDocName("Чек коррекции");
        final var info = getLastStatus();
        // Registration number, Tax identifier, Tax Variant
        final Order.Firm firm = order.getFirm();
        final var tags = new ArrayList<FiscalProperty>();
        tags.add(simple(1037, info.getRegNumber()));
        tags.add(simple(1018, firm.getTaxIdentityNumber()));
        tags.add(simple(1187, firm.getAddress()));
        tags.add(simple(1055, firm.getTaxVariant().getFfdCode()));
        tags.addAll(processPaymentTags(order));
        // Sale Charge
        tags.add(simple(1054, SaleCharge.valueOf(order.getSaleCharge()).getCode()));
        if (order.getCorrection() == null) {
            throw new FrwsException(
                    String.format(
                            "Correction cannot be empty for ORDER_ID=%s, ISSUE_ID=%s"
                            , order.get_id()
                            , issueId
                    )
            );
        }
        final var correction = order.getCorrection();
        tags.add(simple(1173, "SELF_MADE".equals(correction.getCorrectionType()) ? 0 : 1));
        List<FiscalProperty> corrInfo = List.of(
                simple(1177, correction.getDescription()),
                simple(
                        1178,
                        LocalDate.parse(correction.getDocumentDate())
                                .atStartOfDay()
                                .atZone(ZoneId.systemDefault())
                                .format(RFC_1123_DATE_TIME)
                ),
                simple(1179, correction.getDocumentNumber())
        );
        tags.add(array(1174, corrInfo));

        data.setFiscprops(tags);
        data.setTax(correction.getVatType().getCode());
        Map<String, Object> request = new HashMap<>();
        request.put("document", doc);
        return request;
    }

    /**
     * Processes a list of tags related to payments.
     *
     * @param order order
     * @return a list of tags
     */
    private List<FiscalProperty> processPaymentTags(Order order) {
        // check total
        List<FiscalProperty> payments = order.getPayments().stream()
                .map(payment -> simple(PaymentType.valueOf(payment.getPaymentType()).getTag(), payment.getAmount()))
                .collect(Collectors.toList());

        val fromInternet = payments.stream()
                .filter(tag -> tag.getTag() == PaymentType.CREDIT_CARD.getTag())
                .findFirst()
                .map(t -> 1)
                .orElse(0);
        payments.add(simple(1125, fromInternet));
        return payments;
    }

    /**
     * Makes fiscal data prototype using order payments.
     *
     * @param order order
     * @return fiscal data
     */
    private FiscalData prepareFiscalData(Order order) {
        FiscalData data = new FiscalData();
        final var paymentType = order.getPayments().stream()
                .findFirst()
                .map(Order.Payment::getPaymentType)
                .map(PaymentType::valueOf)
                .orElse(PaymentType.CASH);

        data.setMoneyType(paymentType.getCode());
        data.setType(SaleChargeGeneral.valueOf(order.getSaleCharge()).getCode());
        data.setSum(0);
        return data;
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
    public StatusResult closeArchive() {
        getRestTemplate().getForObject(makeUrl("closefs.json?print=1"), String.class);
        return status();
    }

    @Override
    public StatusResult cancelCheck() {
        throw new UnsupportedOperationException("cancelCheck");
    }

    @Override
    public StatusResult status() {
        String responseStr = getRestTemplate().getForObject(makeUrl("cashboxstatus.json"), String.class);
        final var result = prepareStatus();
        JsonNode response;
        try {
            response = mapper.readTree(responseStr);

            final var status = ofNullable(response.get("cashboxStatus"));
            result.setErrorCode(0);
            result.setCurrentDocNumber(
                    status.map(x -> x.path("fsStatus").path("lastDocNumber"))
                            .filter(JsonNode::isInt)
                            .map(JsonNode::asInt)
                            .orElse(null));

            result.setCurrentSession(
                    status.map(x -> x.path("cycleNumber"))
                            .filter(JsonNode::isInt)
                            .map(JsonNode::asInt)
                            .orElse(null));

            final Optional<OffsetDateTime> timestamp = status.map(x -> x.get("dt").asText())
                    .map(x -> OffsetDateTime.parse(x, RFC_1123_DATE_TIME));

            result.setFrDateTime(timestamp.map(OffsetDateTime::toLocalDateTime).orElse(LocalDateTime.MIN));
            result.setOnline(true);
            final String inn = status.map(x -> x.path("userInn")).filter(node -> !node.isMissingNode())
                    .map(JsonNode::asText).orElse(null);

            result.setInn(inn);
            final String regNumber = status.map(x -> x.get("regNumber").asText()).orElse(null);
            result.setRegNumber(regNumber);
            final int taxVariant = status.map(x -> x.path("taxes")).filter(JsonNode::isInt).map(JsonNode::asInt).orElse(0);
            result.setStorageNumber(status.map(x -> x.path("fsStatus").path("fsNumber"))
                    .filter(JsonNode::isTextual)
                    .map(JsonNode::asText)
                    .filter(s -> !s.isBlank())
                    .orElse(null));

            boolean isOpen = status.map(x -> x.path("fsStatus").path("cycleIsOpen"))
                    .map(x -> x.isInt() && x.asInt() != 0).orElse(false);

            final Optional<OffsetDateTime> opened = status.map(x -> x.path("cycleOpened"))
                    .filter(JsonNode::isTextual)
                    .map(x -> OffsetDateTime.parse(x.asText(), RFC_1123_DATE_TIME));

            result.setModeFR(statusMode(isOpen, timestamp, opened));
            result.setSubModeFR(0);

            String serialNumber = status.map(x -> x.path("serial"))
                    .filter(JsonNode::isTextual)
                    .map(JsonNode::asText)
                    .orElse(null);
            result.setSerialNumber(serialNumber);
            result.setStatusMessage(ofNullable(response.path("message")).map(JsonNode::asText).orElse(""));
            result.setStatus(response);
            this.lastStatus = new RegInfo(inn, taxVariant, regNumber, serialNumber);
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
    public SelectResult selectDoc(String documentNumber) {
        String responseStr = getRestTemplate().getForObject(makeUrl("fiscaldoc.json?number=" + documentNumber + "&print=1"), String.class);

        SelectResult selectResult = new SelectResult();

        try {
            JsonNode response = mapper.readTree(responseStr);
            final var propsArr = response.path("document").path("data").path("fiscprops").iterator();

            final var codes = new HashSet<>(Arrays.asList(1018, 1037, 1013, 1041, 1040));
            final var values = new HashMap<Integer, String>();
            Optional<ZonedDateTime> regDate = Optional.empty();
            while (propsArr.hasNext()) {
                final var current = propsArr.next();
                final int tag = current.get("tag").asInt();
                if (1012 == tag) {
                    regDate = Optional.of(
                            OffsetDateTime.parse(current.get("value").asText()
                                    , RFC_1123_DATE_TIME
                            ).toZonedDateTime()
                    );
                    continue;
                }
                if (codes.contains(tag)) {
                    values.put(tag, current.get("value").asText().trim());
                }
            }

            if (codes.stream()
                    .map(values::get)
                    .map(Optional::ofNullable)
                    .anyMatch(Optional::isEmpty) || regDate.isEmpty()) {
                throw new FrwsException(
                        String.format(
                                "There is missed one or several required attributes for DOCUMENT_NUMBER=%s."
                                , documentNumber
                        )
                );
            }
            final var status = new SelectResult.Document()
                    .setDocDate(regDate.get())
                    .setTaxNumber(values.get(1018))
                    .setRegNumber(values.get(1037))
                    .setSerialNumber(values.get(1013))
                    .setStorageNumber(values.get(1041))
                    .setDocNumber(values.get(1040))
                    .setPayload(response.path("document"));
            return selectResult.setDocument(status);
        } catch (Exception e) {
            log.error("Error parsing the response: {} | {}", responseStr, e.getMessage());
            if (e instanceof FrwsException) {
                throw (FrwsException) e;
            } else {
                throw new FrwsException(e);
            }
        }
    }

    @Override
    public String fiscalize(Map<String, Object> data) {
        data.put("sessionId", RANDOM.nextInt());
        ResponseEntity<String> response = restTemplate.postForEntity(makeUrl("fiscalize.json"),
                new HttpEntity<>(data, generateHttpHeaders()),
                String.class);
        return response.getBody();
    }

    @Override
    public String selectDocAsIs(String documentId) {
        return getRestTemplate().getForObject(makeUrl("fiscaldoc.json?number=" + documentId + "&print=1"), String.class);
    }
}

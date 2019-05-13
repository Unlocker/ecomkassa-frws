package com.thepointmoscow.frws.qkkm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.Order;
import com.thepointmoscow.frws.RegistrationResult;
import com.thepointmoscow.frws.StatusResult;
import com.thepointmoscow.frws.qkkm.requests.*;
import com.thepointmoscow.frws.qkkm.responses.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.info.BuildProperties;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.time.*;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static com.thepointmoscow.frws.qkkm.requests.OpenCheckRequest.RETURN_SALE_TYPE;
import static com.thepointmoscow.frws.qkkm.requests.OpenCheckRequest.SALE_TYPE;
import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * A fiscal gateway based on QKKM server.
 */
@Slf4j
@Accessors(chain = true)
public class QkkmFiscalGateway implements FiscalGateway {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String VAT_18_PCT = "VAT_18PCT";
    private static final String VAT_10_PCT = "VAT_10PCT";
    private static final String VAT_0_PCT = "VAT_0PCT";
    /**
     * The maximal number of status retries.
     */
    private static final int MAX_STATUS_RETRIES = 10;
    /**
     * Timeout in millis to wait a response.
     */
    private static final int SOCKET_TIMEOUT_MILLIS = 10000;
    /**
     * Code for an electronic check attribute.
     */
    private static final String ELECTRONIC_ATTR_CODE = "1008";
    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private int port;

    /**
     * App version source.
     */
    private final BuildProperties buildProperties;

    public QkkmFiscalGateway(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    /**
     * Executes a text command with a fiscal registrar.
     *
     * @param command command
     * @return text response
     * @throws IOException possibly IO exception
     */
    private synchronized String executeCommand(String command) throws IOException {
        try (Socket socket = new Socket(host, port)) {
            socket.getOutputStream().write(command.getBytes(CHARSET));
            byte buf[] = new byte[32 * 1024];
            socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
            int len = socket.getInputStream().read(buf);
            String response = new String(buf, 0, len, CHARSET);
            log.info("SENT >>> {}; RECEIVED <<< {}", command, response);
            return response;
        }
    }

    /**
     * Executes command with a fiscal registrar.
     *
     * @param <RESP>       response type
     * @param request      request object
     * @param responseType response type
     * @param normalCodes  a set of codes are intended to be normal
     * @return response object
     * @throws IOException possibly IO exception
     */
    private <RESP extends QkkmResponse> RESP executeCommand(
            QkkmRequest request, Class<RESP> responseType,
            Set<Integer> normalCodes) throws IOException, QkkmException {
        XmlMapper mapper = new XmlMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        RESP resp;
        do {
            String raw = executeCommand(mapper.writeValueAsString(request));
            resp = mapper.readValue(raw, responseType);
        } while (resp.getError().getId() == 80);
        final int errorId = resp.getError().getId();
        if (errorId != 0 && !normalCodes.contains(errorId))
            throw new QkkmException(resp.getError().getText(), errorId);
        return resp;
    }

    @Override
    public RegistrationResult register(Order order, Long issueID, boolean openSession) {
        try {
            if (openSession) {
                executeCommand(new OpenSessionRequest(), QkkmResponse.class, Collections.emptySet());
            }
            final boolean isSaleReturn = order.getSaleCharge().equals("SALE_RETURN");
            executeCommand(
                    new OpenCheckRequest().setOpenCheck(
                            new OpenCheckRequest.OpenCheck()
                                    .setType(isSaleReturn ? RETURN_SALE_TYPE : SALE_TYPE)
                                    .setOperator(order.getCashier().toString())
                    ), QkkmResponse.class, Collections.emptySet());
            for (Order.Item item : order.getItems()) {
                val sale = new Sale()
                        .setText(item.getName())
                        .setAmount(item.getAmount())
                        .setPrice(item.getPrice())
                        .setTax1(Objects.equals(VAT_18_PCT, item.getVatType()) ? 1 : 0)
                        .setTax2(Objects.equals(VAT_10_PCT, item.getVatType()) ? 1 : 0)
                        .setTax3(0) // VAT 20% is not applicable
                        .setTax4(Objects.equals(VAT_0_PCT, item.getVatType()) ? 1 : 0)
                        .setGroup("0");
                final QkkmRequest request = isSaleReturn
                        ? new ReturnSaleRequest().setSale(sale)
                        : new SaleRequest().setSale(sale);
                executeCommand(request, QkkmResponse.class, Collections.emptySet());
            }

            if (order.getIsElectronic()) {
                executeCommand(new SetTlvRequest().setSetTlv(
                        new SetTlvRequest.SetTlv()
                                .setType(ELECTRONIC_ATTR_CODE)
                                .setData(order.getCustomer().getId())
                                .setLen(order.getCustomer().getId().length())
                        ), QkkmResponse.class,
                        Collections.singleton(12) // added code 12 as normal operation exit
                );
            }

            long[] payments = new long[]{0, 0, 0, 0};
            for (Order.Payment pmt : order.getPayments()) {
                switch (pmt.getPaymentType()) {
                    case "CASH":
                        payments[0] += pmt.getAmount();
                        break;
                    case "CREDIT_CARD":
                        payments[1] += pmt.getAmount();
                        break;
                }
            }

            executeCommand(new CloseCheckRequest().setOpenCheck(
                    new CloseCheckRequest.CloseCheck()
                            .setSummaCash(payments[0])
                            .setSumma2(payments[1])
                            .setSumma3(payments[2])
                            .setSumma4(payments[3])
                            .setTax1(order.getItems().stream().map(Order.Item::getVatType)
                                    .anyMatch(x -> Objects.equals(VAT_18_PCT, x)) ? 1 : 0)
                            .setTax2(order.getItems().stream().map(Order.Item::getVatType)
                                    .anyMatch(x -> Objects.equals(VAT_10_PCT, x)) ? 1 : 0)
                            .setTax3(0)
                            .setTax4(order.getItems().stream().map(Order.Item::getVatType)
                                    .anyMatch(x -> Objects.equals(VAT_0_PCT, x)) ? 1 : 0)
            ), QkkmResponse.class, Collections.emptySet());

            String docId = executeCommand(new LastFdIdRequest(), LastFdIdResponse.class, Collections.emptySet()).getResponse().getId();
            String sign = executeCommand(new FiscalMarkRequest().setCommand(
                    new FiscalMarkRequest.FiscalMark().setId(docId)
            ), FiscalMarkResponse.class, Collections.emptySet()).getResponse().getId();

            StatusResult status;
            int i = 0;
            do {
                status = status();
                if (++i > MAX_STATUS_RETRIES) {
                    log.warn("The maximal number ({}) of status retries used. Trying to go next", MAX_STATUS_RETRIES);
                    break;
                }
                Thread.sleep(500);
            } while (status.getModeFR() != 2);

            GetNumSaleCheckResponse.NumSaleCheck checkInfo = executeCommand(
                    new GetNumSaleCheckRequest(), GetNumSaleCheckResponse.class, Collections.emptySet()).getResponse();

            return new RegistrationResult().apply(status).setCurrentSession(checkInfo.getSession()).setRegistration(
                    new RegistrationResult.Registration()
                            .setSessionCheck(checkInfo.getNumCheck())
                            .setDocNo(docId)
                            .setIssueID(issueID.toString())
                            .setRegDate(ZonedDateTime.of(status.getFrDateTime(), ZoneId.of(order.getFirm().getTimezone())))
                            .setSignature(sign)
            );
        } catch (QkkmException e) {
            log.error("An error occurred while registering.", e);
            return new RegistrationResult()
                    .setErrorCode(e.getErrorCode())
                    .setStatusMessage(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while registering.", e);
            return new RegistrationResult()
                    .setErrorCode(-1)
                    .setStatusMessage(e.getMessage());
        }
    }

    /**
     * Opens a session.
     *
     * @return status
     */
    @Override
    public StatusResult openSession() {
        try {
            executeCommand(new OpenSessionRequest(), QkkmResponse.class, Collections.emptySet());
            return status();
        } catch (QkkmException e) {
            log.error("An error occurred while opening a session.", e);
            return new StatusResult()
                    .setAppVersion(buildProperties.getVersion())
                    .setErrorCode(e.getErrorCode())
                    .setStatusMessage(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while opening a session.", e);
            return new StatusResult()
                    .setAppVersion(buildProperties.getVersion())
                    .setErrorCode(-1)
                    .setStatusMessage(e.getMessage());
        }
    }

    @Override
    public StatusResult closeSession() {
        try {
            executeCommand(new ZReportRequest(), QkkmResponse.class, Collections.emptySet());
            return status();
        } catch (QkkmException e) {
            log.error("An error occurred while closing a session.", e);
            return new StatusResult()
                    .setAppVersion(buildProperties.getVersion())
                    .setErrorCode(e.getErrorCode())
                    .setStatusMessage(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while closing a session.", e);
            return new StatusResult()
                    .setAppVersion(buildProperties.getVersion())
                    .setErrorCode(-1)
                    .setStatusMessage(e.getMessage());
        }

    }


    /**
     * Cancels a check.
     *
     * @return status
     */
    @Override
    public StatusResult cancelCheck() {
        try {
            executeCommand(new CancelCheckRequest(), QkkmResponse.class, Collections.emptySet());
            return status();
        } catch (QkkmException e) {
            log.error("An error occurred while canceling a check.", e);
            return new StatusResult()
                    .setAppVersion(buildProperties.getVersion())
                    .setErrorCode(e.getErrorCode())
                    .setStatusMessage(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while canceling a check.", e);
            return new StatusResult()
                    .setAppVersion(buildProperties.getVersion())
                    .setErrorCode(-1)
                    .setStatusMessage(e.getMessage());
        }
    }

    @Override
    public StatusResult status() {
        StatusResult result = new StatusResult().setAppVersion(buildProperties.getVersion());
        try {
            DeviceStatusResponse dsr = executeCommand(new DeviceStatusRequest(), DeviceStatusResponse.class, Collections.emptySet());
            if (!Objects.equals(dsr.getError().getId(), 0)) {
                return result
                        .setStatusMessage(dsr.getError().getText())
                        .setOnline(false)
                        .setErrorCode(dsr.getError().getId());
            }
            DeviceStatusResponse.DeviceStatus ds = dsr.getStatus();
            result.setOnline("1".equals(ds.getIsOnline()))
                    .setStatusMessage(ds.getStatusMessageHTML())
                    .setCurrentDocNumber(ds.getCurrentDocNumber())
                    .setCurrentSession(ds.getNumberLastClousedSession() + 1)
                    .setErrorCode(ds.getDeviceErrorCode())
                    .setInn(ds.getInn())
                    .setSerialNumber(ds.getSerialNumber())
                    .setModeFR(ds.getModeFR())
                    .setSubModeFR(ds.getSubModeFR())
                    .setFrDateTime(LocalDateTime.of(
                            LocalDate.parse(ds.getDateFR(), ofPattern("yyyy.MM.dd")),
                            LocalTime.parse(ds.getTimeFR(), ofPattern("HH:mm:ss"))
                    ));
        } catch (Exception e) {
            log.error("Error while fetching a status of the fiscal registrar", e);
            result.setErrorCode(-1);
            result.setStatusMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public StatusResult continuePrint() {
        try {
            executeCommand(new ContinuePrintRequest(), QkkmResponse.class, Collections.emptySet());
            return status();
        } catch (QkkmException e) {
            log.error("An error occurred while continuing printing.", e);
            return new StatusResult()
                    .setAppVersion(buildProperties.getVersion())
                    .setErrorCode(e.getErrorCode())
                    .setStatusMessage(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while continuing printing.", e);
            return new StatusResult()
                    .setAppVersion(buildProperties.getVersion())
                    .setErrorCode(-1)
                    .setStatusMessage(e.getMessage());
        }

    }
}

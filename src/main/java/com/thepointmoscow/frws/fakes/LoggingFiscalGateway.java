package com.thepointmoscow.frws.fakes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Fiscal gateway that sends to log received requests.
 */
@Slf4j
public class LoggingFiscalGateway implements FiscalGateway {

    /**
     * Default status.
     */
    private final StatusResult status;

    public LoggingFiscalGateway(BuildProperties buildProperties) {
        this.status = new StatusResult()
                .setOnline(true)
                .setErrorCode(0)
                .setFrDateTime(LocalDateTime.of(2012, 12, 11, 20, 3, 20))
                .setInn("7705293503")
                .setSerialNumber("0030070002059014")
                .setCurrentDocNumber(194)
                .setCurrentSession(21)
                .setModeFR((byte) 2)
                .setSubModeFR((byte) 0)
                .setStatusMessage("OK")
                .setAppVersion(buildProperties.getVersion());
    }

    @Override
    public RegistrationResult register(Order order, Long issueID, boolean openSession) {
        log.info("Registration request received. ISSUE_ID={}; OPEN_SESSION={}; ORDER={}", issueID, openSession, order);
        return new RegistrationResult().setRegistration(
                new RegistrationResult.Registration()
                        .setDocNo("6725")
                        .setSignature("3625795987")
                        .setRegDate(ZonedDateTime.of(status.getFrDateTime(), ZoneId.of(order.getFirm().getTimezone())))
                        .setIssueID(issueID.toString())
        ).apply(status);
    }

    @Override
    public StatusResult openSession() {
        log.info("Session opening request received.");
        return status;
    }

    @Override
    public StatusResult closeSession() {
        log.info("Session closing request received.");
        return status;
    }

    @Override
    public StatusResult cancelCheck() {
        log.info("Check canceling request received.");
        return status;
    }

    @Override
    public StatusResult status() {
        log.info("Status retrieval request received.");
        return status;
    }

    @Override
    public SelectResult selectDoc(String documentNumber) {
        log.info("Select document request received");
        SelectResult.Document document = new SelectResult.Document()
                .setTaxNumber("7725225244")
                .setRegNumber("1693666568053977")
                .setSerialNumber("16999987")
                .setStorageNumber("9999078900003063")
                .setDocNumber("12");
        return new SelectResult().setDocument(document);
    }

    @Override
    public String fiscalize(Map<String, Object> data) {
        log.info("Status retrieval request received.");
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(status);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}

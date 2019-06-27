package com.thepointmoscow.frws;

import java.time.ZonedDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RegistrationResult extends StatusResult {

    public RegistrationResult() {
        super("REGISTRATION");
    }

    private Registration registration;

    public RegistrationResult apply(StatusResult sr) {
        setOnline(sr.isOnline());
        setErrorCode(sr.getErrorCode());
        setCurrentDocNumber(sr.getCurrentDocNumber());
        setCurrentSession(sr.getCurrentSession());
        setFrDateTime(sr.getFrDateTime());
        setInn(sr.getInn());
        setModeFR(sr.getModeFR());
        setSubModeFR(sr.getSubModeFR());
        setSerialNumber(sr.getSerialNumber());
        setStatusMessage(sr.getStatusMessage());
        setAppVersion(sr.getAppVersion());
        setStatus(sr.getStatus());
        return this;
    }

    @Data
    @Accessors(chain = true)
    public static class Registration {
        private String issueID;
        private String signature;
        private String docNo;
        private ZonedDateTime regDate;
        private int sessionCheck;
    }
}

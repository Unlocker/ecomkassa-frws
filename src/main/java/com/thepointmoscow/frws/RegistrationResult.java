package com.thepointmoscow.frws;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RegistrationResult extends StatusResult {

    public RegistrationResult() {
        super("REGISTRATION");
    }

    private Registration registration;

    public RegistrationResult apply(StatusResult sr) {
        setOnline(sr.isOnline())
                .setErrorCode(sr.getErrorCode())
                .setCurrentDocNumber(sr.getCurrentDocNumber())
                .setCurrentSession(sr.getCurrentSession())
                .setFrDateTime(sr.getFrDateTime())
                .setInn(sr.getInn())
                .setModeFR(sr.getModeFR())
                .setSubModeFR(sr.getSubModeFR())
                .setSerialNumber(sr.getSerialNumber())
                .setStatusMessage(sr.getStatusMessage())
                .setAppVersion(sr.getAppVersion())
                .setStatus(sr.getStatus());
        return this;
    }

    @Override
    public RegistrationResult setOnline(boolean isOnline) {
        super.setOnline(isOnline);
        return this;
    }

    @Override
    public RegistrationResult setErrorCode(int errorCode) {
        super.setErrorCode(errorCode);
        return this;
    }

    @Override
    public RegistrationResult setFrDateTime(LocalDateTime frDateTime) {
        super.setFrDateTime(frDateTime);
        return this;
    }

    @Override
    public RegistrationResult setInn(String inn) {
        super.setInn(inn);
        return this;
    }

    @Override
    public RegistrationResult setSerialNumber(String serialNumber) {
        super.setSerialNumber(serialNumber);
        return this;
    }

    @Override
    public RegistrationResult setCurrentDocNumber(int currentDocNumber) {
        super.setCurrentDocNumber(currentDocNumber);
        return this;
    }

    @Override
    public RegistrationResult setCurrentSession(int currentSession) {
        super.setCurrentSession(currentSession);
        return this;
    }

    @Override
    public RegistrationResult setModeFR(int modeFR) {
        super.setModeFR(modeFR);
        return this;
    }

    @Override
    public RegistrationResult setSubModeFR(int subModeFR) {
        super.setSubModeFR(subModeFR);
        return this;
    }

    @Override
    public RegistrationResult setStatusMessage(String statusMessage) {
        super.setStatusMessage(statusMessage);
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

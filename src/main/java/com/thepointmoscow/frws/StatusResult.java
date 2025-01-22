package com.thepointmoscow.frws;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class StatusResult {

    public StatusResult() {
        this("STATUS");
    }

    protected StatusResult(String type) {
        this.type = type;
    }

    protected final String type;
    private boolean isOnline;
    private LocalDateTime frDateTime;
    private String inn;
    private String serialNumber;
    private String regNumber;
    private String storageNumber;
    private Integer currentDocNumber;
    private Integer currentSession;
    private int modeFR;
    private int subModeFR;
    private int errorCode;
    private String statusMessage;
    private String appVersion;
    private JsonNode status;

    /**
     * Checks against the session opening.
     *
     * @return is session need to open
     */
    public boolean isSessionClosed() {
        return 3 == modeFR;
    }

    /**
     * Checks is device registered.
     */
    public boolean isRegistered() {
        return currentDocNumber != null;
    }

    /**
     * Checks is device contains a storage.
     */
    public boolean isStorageAttached() {
        return !StringUtils.isEmptyOrWhitespace(storageNumber);
    }
}

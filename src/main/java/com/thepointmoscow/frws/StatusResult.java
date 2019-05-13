package com.thepointmoscow.frws;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

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
    private int errorCode;
    private LocalDateTime frDateTime;
    private String inn;
    private String serialNumber;
    private int currentDocNumber;
    private int currentSession;
    private int modeFR;
    private int subModeFR;
    private String statusMessage;
    private String appVersion;
    private JsonNode status;

    /**
     * Checks against the session opening.
     *
     * @return is session need to open
     */
    public boolean isSessionClosed() {
        return 3 == getModeFR();
    }
}

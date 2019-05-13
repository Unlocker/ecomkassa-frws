package com.thepointmoscow.frws.qkkm.responses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceStatusResponse extends QkkmResponse {
    @JacksonXmlProperty(localName = "getDeviceStatus")
    private DeviceStatus status;

    @Data
    public static class DeviceStatus {
        @JacksonXmlProperty(isAttribute = true)
        private String isOnline;
        @JacksonXmlProperty(isAttribute = true)
        private int deviceErrorCode;
        @JacksonXmlProperty(isAttribute = true)
        private String dateFR;
        @JacksonXmlProperty(isAttribute = true)
        private String timeFR;
        @JacksonXmlProperty(isAttribute = true)
        private String inn;
        @JacksonXmlProperty(isAttribute = true)
        private String serialNumber;
        @JacksonXmlProperty(isAttribute = true)
        private int currentDocNumber;
        @JacksonXmlProperty(isAttribute = true)
        private int numberLastClousedSession;
        @JacksonXmlProperty(isAttribute = true)
        private byte modeFR;
        @JacksonXmlProperty(isAttribute = true)
        private byte subModeFR;
        @JacksonXmlProperty(isAttribute = true)
        private String statusMessageHTML;
    }
}

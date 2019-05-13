package com.thepointmoscow.frws.qkkm.responses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LastFdIdResponse extends QkkmResponse {
    @JacksonXmlProperty(localName = "GetLastFdId")
    private LastFdId response;

    @Data
    public class LastFdId {
        @JacksonXmlProperty(isAttribute = true)
        private String id;
    }
}

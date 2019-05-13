package com.thepointmoscow.frws.qkkm.responses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetNumSaleCheckResponse extends QkkmResponse {
    @JacksonXmlProperty(localName = "GetNumSaleCheck")
    private NumSaleCheck response;

    @Data
    public class NumSaleCheck {
        @JacksonXmlProperty(isAttribute = true, localName = "session")
        private int session;
        @JacksonXmlProperty(isAttribute = true, localName = "value")
        private int numCheck;
    }
}

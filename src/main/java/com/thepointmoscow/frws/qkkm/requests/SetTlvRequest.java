package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SetTlvRequest extends QkkmRequest {

    @JacksonXmlProperty(localName = "SetTLV")
    private SetTlv setTlv;

    @Data
    @Accessors(chain = true)
    public static class SetTlv {
        @JacksonXmlProperty(isAttribute = true)
        private String type;
        @JacksonXmlProperty(isAttribute = true)
        private int len;
        @JacksonXmlProperty(isAttribute = true)
        private String data;
    }
}

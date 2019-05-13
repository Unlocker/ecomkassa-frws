package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OpenCheckRequest extends QkkmRequest {
    public static final String SALE_TYPE = "Sale";
    public static final String RETURN_SALE_TYPE = "ReturnSale";

    @JacksonXmlProperty(localName = "OpenCheck")
    private OpenCheck openCheck;

    @Data
    @Accessors(chain = true)
    public static class OpenCheck {
        @JacksonXmlProperty(isAttribute = true)
        private String type;
        @JacksonXmlProperty(isAttribute = true)
        private String operator;
    }
}

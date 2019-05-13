package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class FiscalMarkRequest extends QkkmRequest {
    @JacksonXmlProperty(localName = "GetFiskalMarkById")
    private FiscalMark command;

    @Data
    @Accessors(chain = true)
    public static class FiscalMark {
        @JacksonXmlProperty(isAttribute = true)
        private String id;
    }
}

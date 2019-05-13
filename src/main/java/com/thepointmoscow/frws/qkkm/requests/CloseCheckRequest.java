package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class CloseCheckRequest extends QkkmRequest {

    @JacksonXmlProperty(localName = "CloseCheck")
    private CloseCheck openCheck;

    @Data
    @Accessors(chain = true)
    public static class CloseCheck {
        @JacksonXmlProperty(isAttribute = true, localName = "Text")
        private String text;
        @JacksonXmlProperty(isAttribute = true, localName = "SummaCash")
        private long summaCash;
        @JacksonXmlProperty(isAttribute = true, localName = "Summa2")
        private long summa2;
        @JacksonXmlProperty(isAttribute = true, localName = "Summa3")
        private long summa3;
        @JacksonXmlProperty(isAttribute = true, localName = "Summa4")
        private long summa4;
        @JacksonXmlProperty(isAttribute = true, localName = "Discount")
        private long discount;
        @JacksonXmlProperty(isAttribute = true, localName = "Tax1")
        private int tax1;
        @JacksonXmlProperty(isAttribute = true, localName = "Tax2")
        private int tax2;
        @JacksonXmlProperty(isAttribute = true, localName = "Tax3")
        private int tax3;
        @JacksonXmlProperty(isAttribute = true, localName = "Tax4")
        private int tax4;
    }
}

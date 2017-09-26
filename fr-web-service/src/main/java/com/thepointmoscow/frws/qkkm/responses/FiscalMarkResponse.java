package com.thepointmoscow.frws.qkkm.responses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FiscalMarkResponse extends QkkmResponse {
    @JacksonXmlProperty(localName = "GetFiskalMarkById")
    private LastFdId response;

    @Data
    public class LastFdId {
        @JacksonXmlProperty(isAttribute = true, localName = "fiskalMark")
        private String id;
    }
}

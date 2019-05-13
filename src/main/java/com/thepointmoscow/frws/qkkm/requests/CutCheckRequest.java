package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
class CutCheckRequest extends QkkmRequest {
    @JacksonXmlProperty(localName = "CutCheck")
    private String command = null;
}

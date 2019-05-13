package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CancelCheckRequest extends QkkmRequest {
    @JacksonXmlProperty(localName = "ChancelCheck")
    private String command = null;
}

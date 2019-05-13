package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class XReportRequest extends QkkmRequest {
    @JacksonXmlProperty(localName = "XReport")
    private String command = null;
}

package com.thepointmoscow.frws.qkkm.responses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class ErrorMessage {
    @JacksonXmlProperty(isAttribute = true)
    private int id;
    @JacksonXmlProperty(isAttribute = true)
    private String text;
}

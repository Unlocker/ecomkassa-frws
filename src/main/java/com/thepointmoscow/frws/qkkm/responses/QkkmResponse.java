package com.thepointmoscow.frws.qkkm.responses;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JacksonXmlRootElement(localName = "ControlProtocol")
public class QkkmResponse {
    @JacksonXmlProperty(isAttribute = true, localName = "messageType")
    private final String messageType = "answer";
    @JacksonXmlProperty(localName = "error")
    private ErrorMessage error;
}


package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JacksonXmlRootElement(localName = "ControlProtocol")
public abstract class QkkmRequest {
    @JacksonXmlProperty(isAttribute = true, localName = "messageType")
    private final String messageType = "request";
}

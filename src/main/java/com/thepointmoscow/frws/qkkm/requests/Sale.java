package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Sale structure.
 */
@Data
@Accessors(chain = true)
public class Sale {
    @JacksonXmlProperty(isAttribute = true, localName = "Text")
    private String text;
    @JacksonXmlProperty(isAttribute = true, localName = "Amount")
    private long amount;
    @JacksonXmlProperty(isAttribute = true, localName = "Price")
    private long price;
    @JacksonXmlProperty(isAttribute = true, localName = "Group")
    private String group;
    @JacksonXmlProperty(isAttribute = true, localName = "Tax1")
    private int tax1;
    @JacksonXmlProperty(isAttribute = true, localName = "Tax2")
    private int tax2;
    @JacksonXmlProperty(isAttribute = true, localName = "Tax3")
    private int tax3;
    @JacksonXmlProperty(isAttribute = true, localName = "Tax4")
    private int tax4;
}

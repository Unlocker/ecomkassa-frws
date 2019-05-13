package com.thepointmoscow.frws.qkkm.requests;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Gets the number of voucher inside a session.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetNumSaleCheckRequest extends QkkmRequest {
    @JacksonXmlProperty(localName = "GetNumSaleCheck")
    private String command = null;
}

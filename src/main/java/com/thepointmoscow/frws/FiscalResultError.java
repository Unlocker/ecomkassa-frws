package com.thepointmoscow.frws;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FiscalResultError {
    private final int errorCode;
    private final String statusMessage;
}

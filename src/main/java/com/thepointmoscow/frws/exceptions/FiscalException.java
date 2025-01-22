package com.thepointmoscow.frws.exceptions;

import com.thepointmoscow.frws.FiscalResultError;
import lombok.Getter;

@Getter
public class FiscalException extends FrwsException {
    private final FiscalResultError fiscalResultError;

    public FiscalException(FiscalResultError fiscalResultError) {
        this.fiscalResultError = fiscalResultError;
    }
}

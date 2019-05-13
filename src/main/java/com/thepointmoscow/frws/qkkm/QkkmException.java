package com.thepointmoscow.frws.qkkm;

import lombok.Getter;

public class QkkmException extends Exception {

    public QkkmException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    @Getter
    private final int errorCode;
}

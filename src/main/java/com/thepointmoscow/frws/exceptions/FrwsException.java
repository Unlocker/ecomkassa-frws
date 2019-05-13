package com.thepointmoscow.frws.exceptions;

@SuppressWarnings("unused")
public class FrwsException extends RuntimeException {
    public FrwsException() {
    }

    public FrwsException(String s) {
        super(s);
    }

    public FrwsException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FrwsException(Throwable throwable) {
        super(throwable);
    }

    public FrwsException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}

package com.thepointmoscow.frws.exceptions;

@SuppressWarnings("unused")
public class ServerIsBusyException extends FrwsException {
    public ServerIsBusyException() {
    }

    public ServerIsBusyException(String s) {
        super(s);
    }

    public ServerIsBusyException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ServerIsBusyException(Throwable throwable) {
        super(throwable);
    }

    public ServerIsBusyException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}

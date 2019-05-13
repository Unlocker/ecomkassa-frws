package com.thepointmoscow.frws;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentMethod {
    FULL_PREPAYMENT(1),
    PREPAYMENT(2),
    ADVANCE(3),
    FULL_PAYMENT(4),
    PARTIAL_PAYMENT(5),
    CREDIT(6),
    CREDIT_PAYMENT(7);

    @Getter
    private final int code;

    public int getFfdTag() {
        return 1214;
    }
}

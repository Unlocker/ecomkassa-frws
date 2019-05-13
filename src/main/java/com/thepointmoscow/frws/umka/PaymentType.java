package com.thepointmoscow.frws.umka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PaymentType {
    CASH(1, 1031),
    CREDIT_CARD(2, 1081),
    PRE_PAID(3, 1215),
    POST_PAID(4, 1216),
    COUNTER_OFFER(5, 1217);

    @Getter
    private final int code;

    @Getter
    private final int tag;
}

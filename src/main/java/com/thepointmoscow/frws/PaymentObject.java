package com.thepointmoscow.frws;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentObject {
    COMMODITY(1),
    EXCISE(2),
    JOB(3),
    SERVICE(4),
    GAMBLING_BET(5),
    GAMBLING_PRIZE(6),
    LOTTERY(7),
    LOTTERY_PRIZE(8),
    INTELLECTUAL_ACTIVITY(9),
    PAYMENT(10),
    AGENT_COMMISSION(11),
    COMPOSITE(12),
    ANOTHER(13);

    @Getter
    private final int code;

    public int getFfdTag() {
        return 1212;
    }
}

package com.thepointmoscow.frws;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaxVariant {
    GENERAL(1),
    SIMPLIFIED_INCOME(2),
    SIMPLIFIED_IN_OUT(4),
    SINGLE_INCOME_TAX(8),
    SINGLE_AGRICULTURE_TAX(16),
    PATENT(32);

    private final int ffdCode;
}

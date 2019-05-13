package com.thepointmoscow.frws.umka;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum for a mapping "document.data.type"
 */
@AllArgsConstructor
public enum SaleChargeGeneral {
    SALE(1),
    SALE_RETURN(2),
    CHARGE(4),
    CHARGE_RETURN(5),
    SALE_CORRECTION(7),
    CHARGE_CORRECTION(9);

    @Getter
    private final int code;
}

package com.thepointmoscow.frws.umka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SaleCharge {
    SALE(1),
    SALE_RETURN(2),
    CHARGE(3),
    CHARGE_RETURN(4),
    SALE_CORRECTION(1) {
        @Override
        public boolean isCorrection() {
            return true;
        }
    },
    CHARGE_CORRECTION(3) {
        @Override
        public boolean isCorrection() {
            return true;
        }
    };

    @Getter
    private final int code;

    public boolean isCorrection() {
        return false;
    }

}

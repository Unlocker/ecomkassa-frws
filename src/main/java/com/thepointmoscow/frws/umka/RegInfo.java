package com.thepointmoscow.frws.umka;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = { "regNumber" }) class RegInfo {
    private final String inn;
    private final int taxVariant;
    private final String regNumber;
}

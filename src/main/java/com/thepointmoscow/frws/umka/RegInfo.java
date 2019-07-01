package com.thepointmoscow.frws.umka;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

@AllArgsConstructor
@Value
@EqualsAndHashCode(of = { "regNumber" }) class RegInfo {
    private final String inn;
    private final int taxVariant;
    private final String regNumber;
    private final String serialNumber;
}

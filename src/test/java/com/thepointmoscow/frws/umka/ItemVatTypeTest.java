package com.thepointmoscow.frws.umka;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemVatTypeTest {

    @Test void shouldGet18PctTax() {
        // GIVEN
        // WHEN
        final var result = ItemVatType.valueOf("VAT_18PCT");
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(1);
    }

}
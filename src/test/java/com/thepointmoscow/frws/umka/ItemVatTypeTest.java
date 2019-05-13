package com.thepointmoscow.frws.umka;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemVatTypeTest {

    @Test void shouldGet18PctTax() {
        // GIVEN
        // WHEN
        val result = ItemVatType.valueOf("VAT_18PCT");
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(1);
    }

}
package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.Order;
import com.thepointmoscow.frws.RegistrationResult;
import com.thepointmoscow.frws.UtilityConfig;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.BuildProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for an Umka fiscal gateway.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UtilityConfig.class, WebTestConfig.class})
@Disabled("Integration tests")
class UmkaFiscalGatewayIT {

    private static final String TEST_HOST = "95.31.13.249";
    private static final int TEST_PORT = 8088;
    private static final String APP_VERSION = "1.2.3-test";

    @Autowired
    @Qualifier("umka")
    private RestTemplate restTemplate;

    private UmkaFiscalGateway sut;

    @BeforeEach
    void setup() {
        BuildProperties props = Mockito.mock(BuildProperties.class);
        Mockito.when(props.getVersion()).thenReturn(APP_VERSION);
        this.sut = new UmkaFiscalGateway(TEST_HOST, TEST_PORT, props, new ObjectMapper(), restTemplate);
    }

    @Test
    void shouldReturnStatus() {
        // GIVEN // WHEN
        val result = sut.status();
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualToIgnoringCase("status");
        assertThat(result.getErrorCode()).isEqualTo(0);
        assertThat(result.getModeFR()).isEqualTo(2);
        assertThat(result.getSerialNumber()).isNotEmpty();
        assertThat(result.getCurrentDocNumber()).isNotEqualTo(0);
        assertThat(result.getCurrentSession()).isGreaterThanOrEqualTo(0);
        assertThat(result.getAppVersion()).isEqualTo(APP_VERSION);
        assertThat(result.getFrDateTime()).isNotNull().isNotEqualTo(LocalDateTime.MIN);
        assertThat(result.isOnline()).isTrue();
    }

    @Test
    void shouldRegisterOrderWithCashPayment() {
        // GIVEN
        Order order = new Order().set_id(1L).setOrderType("CASH_VOUCHER").setStatus("PAID").setSaleCharge("SALE");
        order.setFirm(new Order.Firm().setTimezone("Europe/Moscow"));
        order.setCashier(new Order.Cashier().setFirstName("Имя").setLastName("Фамилия"));
        order.setCustomer(new Order.Customer().setEmail("customer@example.com"));
        order.setItems(Collections.singletonList(
                new Order.Item().setName("Тапочки для тараканов").setAmount(1000L).setPrice(1L)
                        .setVatType("VAT_18PCT")));
        order.setPayments(Collections.singletonList(new Order.Payment().setAmount(1L).setPaymentType("CASH")));
        Random rnd = new Random();
        // WHEN
        val result = sut.register(order, rnd.nextLong(), false);
        // THEN
        assertThat(result).isNotNull();
        final RegistrationResult.Registration registration = result.getRegistration();
        assertThat(registration).isNotNull();
        assertThat(registration.getSignature()).isNotEmpty();
        assertThat(registration.getRegDate()).isNotNull();
        assertThat(registration.getSessionCheck()).isNotEqualTo(0);
        assertThat(registration.getDocNo()).isNotEmpty();
    }

    @Test
    void shouldRegisterOrderWithCreditCardPayment() {
        // GIVEN
        Order order = new Order().set_id(1L).setOrderType("CASH_VOUCHER").setStatus("PAID").setSaleCharge("SALE");
        order.setFirm(new Order.Firm().setTimezone("Europe/Moscow"));
        order.setCashier(new Order.Cashier().setFirstName("Имя").setLastName("Фамилия"));
        order.setCustomer(new Order.Customer().setEmail("customer@example.com"));
        order.setItems(Collections.singletonList(
                new Order.Item().setName("Тапочки для тараканов").setAmount(1000L).setPrice(1L)
                        .setVatType("VAT_18PCT")));
        order.setPayments(Collections.singletonList(new Order.Payment().setAmount(1L).setPaymentType("CREDIT_CARD")));
        Random rnd = new Random();
        // WHEN
        val result = sut.register(order, rnd.nextLong(), false);
        // THEN
        assertThat(result).isNotNull();
        final RegistrationResult.Registration registration = result.getRegistration();
        assertThat(registration).isNotNull();
        assertThat(registration.getSignature()).isNotEmpty();
        assertThat(registration.getRegDate()).isNotNull();
        assertThat(registration.getSessionCheck()).isNotEqualTo(0);
        assertThat(registration.getDocNo()).isNotEmpty();
    }

}
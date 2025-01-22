package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.FiscalResultError;
import com.thepointmoscow.frws.Order;
import com.thepointmoscow.frws.TaxVariant;
import com.thepointmoscow.frws.UtilityConfig;
import com.thepointmoscow.frws.exceptions.FiscalException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UtilityConfig.class})
class UmkaFiscalGatewayTestError {

    private static final String GET_STATUS_URL = "http://TEST_HOST:54321/cashboxstatus.json";
    private static final String POST_REGISTER_URL = "http://TEST_HOST:54321/fiscalcheck.json";
    private static final String GET_OPEN_URL = "http://TEST_HOST:54321/cycleopen.json?print=1";
    private static final String GET_CLOSE_URL = "http://TEST_HOST:54321/cycleclose.json?print=1";
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    private UmkaFiscalGateway umkaFiscalGateway;

    @BeforeEach
    private void setup() {
        BuildProperties props = Mockito.mock(BuildProperties.class);
        Mockito.when(props.getVersion()).thenReturn("test.app.version");
        server = MockRestServiceServer.bindTo(restTemplate).build();
        this.umkaFiscalGateway = new UmkaFiscalGateway("TEST_HOST", 54321, props, mapper, restTemplate);
    }

    private String getBodyFromFile(String path) throws IOException {
        final InputStream resource = getClass().getResourceAsStream(path);
        StringWriter writer = new StringWriter();
        String encoding = StandardCharsets.UTF_8.name();
        IOUtils.copy(resource, writer, encoding);
        return writer.toString();
    }

    @Test
    void registerWithBadRequest() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/fiscal-error.json");

        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(body, MediaType.TEXT_PLAIN));

        this.server.expect(requestTo(POST_REGISTER_URL))
                .andRespond(withBadRequest()
                        .body(body)
                        .contentType(MediaType.TEXT_PLAIN)
                );

        Order order = generateTemplateOrder();
        Random rnd = new Random();

        // WHEN
        FiscalResultError fiscalResultError = null;
        try {
            umkaFiscalGateway.register(order, rnd.nextLong(), false);
        } catch (FiscalException e) {
            fiscalResultError = e.getFiscalResultError();
        }

        // THEN
        assertThat(fiscalResultError).isNotNull();
        assertThat(fiscalResultError.getErrorCode()).isEqualTo(102);
        assertThat(fiscalResultError.getStatusMessage()).isEqualTo("Ошибка транспортного соединения ФН");
    }

    @Test
    void registerWithServerErrorShouldThrowException() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/fiscal-error.json");

        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(body, MediaType.TEXT_PLAIN));

        this.server.expect(requestTo(POST_REGISTER_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(body)
                        .contentType(MediaType.TEXT_PLAIN)
                );

        Order order = generateTemplateOrder();
        Random rnd = new Random();

        // THEN
        // WHEN
        Assertions.assertThrows(FiscalException.class, () -> umkaFiscalGateway.register(order, rnd.nextLong(), false));
    }

    @Test
    void openSessionWithServerErrorShouldThrowException() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/fiscal-error.json");

        this.server.expect(requestTo(GET_OPEN_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(body)
                        .contentType(MediaType.TEXT_PLAIN)
                );

        // THEN
        // WHEN
        Assertions.assertThrows(FiscalException.class, () -> umkaFiscalGateway.openSession());
    }

    @Test
    void closeSessionWithServerErrorShouldThrowException() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/fiscal-error.json");

        this.server.expect(requestTo(GET_CLOSE_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(body)
                        .contentType(MediaType.TEXT_PLAIN)
                );
        // THEN
        // WHEN
        Assertions.assertThrows(FiscalException.class, () -> umkaFiscalGateway.closeSession());
    }

    private Order generateTemplateOrder() {
        Order order = new Order().set_id(1L).setOrderType("CASH_VOUCHER").setStatus("PAID").setSaleCharge("SALE");
        order.setFirm(
                new Order.Firm()
                        .setTimezone("Europe/Moscow")
                        .setAddress("дер. Пупыркино, д. 32")
                        .setTaxVariant(TaxVariant.GENERAL)
        );
        order.setCashier(new Order.Cashier().setFirstName("Имя").setLastName("Фамилия"));
        order.setCustomer(new Order.Customer().setEmail("customer@example.com"));
        order.setItems(Collections.singletonList(
                new Order.Item().setName("Тапочки для тараканов").setAmount(1000L).setPrice(1L)
                        .setVatType("VAT_18PCT")));
        order.setPayments(Collections.singletonList(new Order.Payment().setAmount(1L).setPaymentType("CASH")));
        return order;
    }
}

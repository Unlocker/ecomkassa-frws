package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.Order;
import com.thepointmoscow.frws.TaxVariant;
import com.thepointmoscow.frws.UtilityConfig;
import com.thepointmoscow.frws.exceptions.FiscalException;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UtilityConfig.class})
class UmkaFiscalGatewayTest {

    private static final String GET_STATUS_URL = "http://TEST_HOST:54321/cashboxstatus.json";
    private static final String GET_SELECT_URL = "http://TEST_HOST:54321/fiscaldoc.json?number=12&print=1";
    private static final String POST_REGISTER_URL = "http://TEST_HOST:54321/fiscalcheck.json";
    private static final MediaType CONTENT_TYPE = MediaType.valueOf("text/plain;charset=UTF-8");

    private MockRestServiceServer server;
    private static final Random RANDOM = new Random();

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    private UmkaFiscalGateway sut;

    private BuildProperties props;

    @BeforeEach
    void setup() {
        props = Mockito.mock(BuildProperties.class);
        Mockito.when(props.getVersion()).thenReturn("test.app.version");
        server = MockRestServiceServer.bindTo(restTemplate).build();
        this.sut = new UmkaFiscalGateway("TEST_HOST", 54321, props, mapper, restTemplate);
    }

    private String getBodyFromFile(String path) throws IOException {
        final InputStream resource = getClass().getResourceAsStream(path);
        StringWriter writer = new StringWriter();
        String encoding = StandardCharsets.UTF_8.name();
        IOUtils.copy(resource, writer, encoding);
        return writer.toString();
    }

    @Test
    void shouldGetExpiredStatus() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/expired-session.json");

        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(body, CONTENT_TYPE));
        // WHEN
        final var res = sut.status();
        // THEN
        assertThat(res).isNotNull();
        assertThat(res.isOnline()).isTrue();
        assertThat(res.getModeFR()).isEqualTo(UmkaFiscalGateway.STATUS_EXPIRED_SESSION);
        assertThat(res.isSessionClosed()).isTrue();
    }

    @Test
    void shouldGetOpenedStatus() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/open-session.json");

        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(body, CONTENT_TYPE));
        // WHEN
        final var res = sut.status();
        // THEN
        assertThat(res).isNotNull();
        assertThat(res.isOnline()).isTrue();
        assertThat(res.getModeFR()).isEqualTo(UmkaFiscalGateway.STATUS_OPEN_SESSION);
        assertThat(res.isSessionClosed()).isFalse();
    }

    @Test
    void shouldGetClosedStatus() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/closed-session.json");

        this.server.expect(requestTo(GET_STATUS_URL)).andRespond(withSuccess(body, CONTENT_TYPE));
        // WHEN
        final var res = sut.status();
        // THEN
        assertThat(res).isNotNull();
        assertThat(res.isOnline()).isTrue();
        assertThat(res.getModeFR()).isEqualTo(UmkaFiscalGateway.STATUS_CLOSED_SESSION);
        assertThat(res.isSessionClosed()).isFalse();
    }

    @Test
    void shouldGetValidDocument() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/fiscaldoc.json");

        this.server.expect(requestTo(GET_SELECT_URL)).andRespond(withSuccess(body, CONTENT_TYPE));
        // WHEN
        final var res = sut.selectDoc("12");
        // THEN
        assertThat(res).isNotNull();
        assertThat(res.getDocument().getTaxNumber()).isEqualTo("7725225244");
        assertThat(res.getDocument().getRegNumber()).isEqualTo("1693666568053977");
        assertThat(res.getDocument().getSerialNumber()).isEqualTo("16999987");
        assertThat(res.getDocument().getStorageNumber()).isEqualTo("9999078900003063");
        assertThat(res.getDocument().getDocNumber()).isEqualTo("12");
    }

    @Test
    void shouldRegister() throws IOException {
        // GIVEN
        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(getBodyFromFile("/com/thepointmoscow/frws/umka/open-session.json"), CONTENT_TYPE));

        this.server.expect(requestTo(POST_REGISTER_URL))
                .andRespond(withSuccess(getBodyFromFile("/com/thepointmoscow/frws/umka/fiscalcheck.json"), CONTENT_TYPE));
        // WHEN
        final var res = sut.register(generateOrder(), 1L, false);

        // THEN
        assertThat(res).isNotNull();
        assertThat(res.getRegistration()).isNotNull();
        assertThat(res.getRegistration().getIssueID()).isEqualTo("1");
        assertThat(res.getRegistration().getSignature()).isEqualTo("7725225244");
        assertThat(res.getRegistration().getDocNo()).isEqualTo("45");
        assertThat(res.getType()).isEqualTo("REGISTRATION");
        assertThat(res.isOnline()).isTrue();
        assertThat(res.getInn()).isEqualTo("7725225244");
        assertThat(res.getCurrentDocNumber()).isEqualTo(45);
        assertThat(res.getCurrentSession()).isEqualTo(12);
        assertThat(res.getModeFR()).isEqualTo(2);
        assertThat(res.getSubModeFR()).isEqualTo(0);
        assertThat(res.getErrorCode()).isEqualTo(0);
        assertThat(res.getStatusMessage()).isNull();
        assertThat(res.getAppVersion()).isEqualTo(props.getVersion());
        assertThat(res.getStatus()).isNull();
        assertThat(res.isSessionClosed()).isFalse();
    }

    @Test
    void shouldMakeCorrection() throws IOException {
        // GIVEN
        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(getBodyFromFile("/com/thepointmoscow/frws/umka/open-session.json"), CONTENT_TYPE));

        this.server.expect(requestTo(POST_REGISTER_URL))
                .andRespond(withSuccess(getBodyFromFile("/com/thepointmoscow/frws/umka/fiscalcheck.json"), CONTENT_TYPE));
        // WHEN
        final var res = sut.register(generateCorrection(), 1L, false);

        // THEN
        assertThat(res).isNotNull();
        assertThat(res.getRegistration()).isNotNull();
        assertThat(res.getRegistration().getIssueID()).isEqualTo("1");
        assertThat(res.getRegistration().getSignature()).isEqualTo("7725225244");
        assertThat(res.getRegistration().getDocNo()).isEqualTo("45");
        assertThat(res.getType()).isEqualTo("REGISTRATION");
        assertThat(res.isOnline()).isTrue();
        assertThat(res.getInn()).isEqualTo("7725225244");
        assertThat(res.getCurrentDocNumber()).isEqualTo(45);
        assertThat(res.getCurrentSession()).isEqualTo(12);
        assertThat(res.getModeFR()).isEqualTo(2);
        assertThat(res.getSubModeFR()).isEqualTo(0);
        assertThat(res.getErrorCode()).isEqualTo(0);
        assertThat(res.getStatusMessage()).isNull();
        assertThat(res.getAppVersion()).isEqualTo(props.getVersion());
        assertThat(res.getStatus()).isNull();
        assertThat(res.isSessionClosed()).isFalse();
    }

    @Test
    void shouldParseNotRegistered() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/not-registered.json");
        this.server.expect(requestTo(GET_STATUS_URL)).andRespond(withSuccess(body, CONTENT_TYPE));
        // WHEN
        val res = sut.status();
        // THEN
        assertThat(res.getCurrentDocNumber()).isNull();
        assertThat(res.getCurrentSession()).isEqualTo(0);
        assertThat(res.isRegistered()).isFalse();
        assertThat(res.isStorageAttached()).isTrue();
    }

    @Test
    void shouldParseNoStorage() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/no-storage.json");
        this.server.expect(requestTo(GET_STATUS_URL)).andRespond(withSuccess(body, CONTENT_TYPE));
        // WHEN
        val res = sut.status();
        // THEN
        assertThat(res.getCurrentDocNumber()).isNull();
        assertThat(res.getCurrentSession()).isEqualTo(0);
        assertThat(res.isRegistered()).isFalse();
        assertThat(res.isStorageAttached()).isFalse();
    }

    @Test
    void shouldParseTestStatus() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/status-test.json");
        this.server.expect(requestTo(GET_STATUS_URL)).andRespond(withSuccess(body, CONTENT_TYPE));
        // WHEN
        val res = sut.status();
        // THEN
        assertThat(res.getCurrentDocNumber()).isEqualTo(1993);
        assertThat(res.getCurrentSession()).isEqualTo(17);
        assertThat(res.isRegistered()).isTrue();
        assertThat(res.isStorageAttached()).isTrue();
    }

    @Test
    void shouldThrowFiscalError() throws IOException {
        // GIVEN
        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(getBodyFromFile("/com/thepointmoscow/frws/umka/open-session.json"), CONTENT_TYPE));

        this.server.expect(requestTo(POST_REGISTER_URL))
                .andRespond(withSuccess(getBodyFromFile("/com/thepointmoscow/frws/umka/fiscal-error.json"), CONTENT_TYPE));
        // WHEN
        assertThatThrownBy(() -> sut.register(generateOrder(), 1L, false))
                .isInstanceOf(FiscalException.class)
                .extracting(ex -> ((FiscalException) ex).getFiscalResultError())
                .hasFieldOrPropertyWithValue("errorCode", 102)
                .hasFieldOrPropertyWithValue("statusMessage", "Ошибка транспортного соединения ФН")
        ;
        // THEN
    }

    @Test
    void shouldMakeCorrectionOrder() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/open-session.json");
        this.server.expect(requestTo(GET_STATUS_URL)).andRespond(withSuccess(body, CONTENT_TYPE));


        var o = new Order()
                .setSaleCharge("SALE_CORRECTION")
                .setFirm(
                        new Order.Firm()
                                .setTaxVariant(TaxVariant.PATENT)
                                .setAddress("https://osobaya-territoria.com")
                                .setTaxIdentityNumber("561207964904")
                )
                .setCorrection(
                        new Order.Correction()
                                .setCorrectionType("SELF_MADE")
                                .setVatType(ItemVatType.VAT_NONE)
                                .setDocumentDate("2025-04-03")
                )
                .setPayments(
                        List.of(
                                new Order.Payment()
                                        .setPaymentType("CREDIT_CARD")
                                        .setAmount(699000L)
                        )
                );
        // WHEN
        var result = sut.correctionOrder(o, RANDOM.nextLong());
        // THEN
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.get("document")).isInstanceOf(FiscalDoc.class);
        var fiscalDoc = (FiscalDoc) result.get("document");
        List<FiscalProperty> fiscprops = fiscalDoc.getData().getFiscprops();
        Assertions.assertThat(fiscprops).isNotEmpty();
        Assertions.assertThat(fiscprops.stream().filter(p -> p.getTag() == 1055).findFirst())
                .hasValue(new FiscalProperty().setTag(1055).setValue(32))
        ;
    }

    private static Order generateOrder() {
        return new Order()
                .setSaleCharge("SALE")
                .setFirm(
                        new Order.Firm()
                                .setTaxVariant(TaxVariant.GENERAL)
                                .setAddress("г. Тараканов")
                                .setTaxIdentityNumber("1234567890")
                );
    }


    private static Order generateCorrection() {
        return generateOrder()
                .setSaleCharge("SALE_CORRECTION")
                .setCorrection(
                        new Order.Correction()
                                .setCorrectionType("SELF_MADE")
                                .setDescription("описание")
                                .setDocumentDate("2024-03-25")
                                .setVatType(ItemVatType.VAT_10PCT)
                );
    }


}

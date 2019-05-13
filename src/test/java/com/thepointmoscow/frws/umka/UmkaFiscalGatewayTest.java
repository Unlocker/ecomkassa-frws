package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.UtilityConfig;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { UtilityConfig.class, WebTestConfig.class }) class UmkaFiscalGatewayTest {

    private static final String GET_STATUS_URL = "http://TEST_HOST:54321/cashboxstatus.json";
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("umka")
    private RestTemplate restTemplate;

    private UmkaFiscalGateway sut;

    @BeforeEach
    private void setup() {
        BuildProperties props = Mockito.mock(BuildProperties.class);
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

    @Test void shouldGetExpiredStatus() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/expired-session.json");

        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(body, MediaType.TEXT_PLAIN));
        // WHEN
        val res = sut.status();
        // THEN
        assertThat(res).isNotNull();
        assertThat(res.isOnline()).isTrue();
        assertThat(res.getModeFR()).isEqualTo(UmkaFiscalGateway.STATUS_EXPIRED_SESSION);
    }

    @Test void shouldGetOpenedStatus() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/open-session.json");

        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(body, MediaType.TEXT_PLAIN));
        // WHEN
        val res = sut.status();
        // THEN
        assertThat(res).isNotNull();
        assertThat(res.isOnline()).isTrue();
        assertThat(res.getModeFR()).isEqualTo(UmkaFiscalGateway.STATUS_OPEN_SESSION);
    }

    @Test void shouldGetClosedStatus() throws IOException {
        // GIVEN
        final String body = getBodyFromFile("/com/thepointmoscow/frws/umka/closed-session.json");

        this.server.expect(requestTo(GET_STATUS_URL))
                .andRespond(withSuccess(body, MediaType.TEXT_PLAIN));
        // WHEN
        val res = sut.status();
        // THEN
        assertThat(res).isNotNull();
        assertThat(res.isOnline()).isTrue();
        assertThat(res.getModeFR()).isEqualTo(UmkaFiscalGateway.STATUS_CLOSED_SESSION);
    }

}

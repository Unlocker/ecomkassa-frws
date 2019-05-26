package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.BackendGateway;
import com.thepointmoscow.frws.BackendReportErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebTestConfig {
    @Bean
    RestTemplateBuilder builder() {
        return new RestTemplateBuilder();
    }

    @Bean
    BackendReportErrorHandler backendReportErrorHandler() {
        return new BackendReportErrorHandler(new ObjectMapper());
    }

    @Bean
    @Qualifier("umka")
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            ClientHttpRequestFactory factory,
            ClientHttpRequestInterceptor interceptor) {

        return builder.basicAuthorization("1", "1").errorHandler(backendReportErrorHandler()).build();
    }
}

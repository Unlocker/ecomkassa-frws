package com.thepointmoscow.frws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@Slf4j
public class UtilityConfig {
    private static final String UMKA_DEFAULT_LOGIN = "1";
    private static final String UMKA_DEFAULT_PASSWORD = "1";

    @Bean
    public ResponseErrorHandler backendReportErrorHandler(ObjectMapper objectMapper) {
        return new BackendReportErrorHandler(objectMapper);
    }

    @Bean
    public RestTemplate restTemplate(ResponseErrorHandler backendReportErrorHandler) {
        return new RestTemplateBuilder()
                .requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
                .basicAuthentication(UMKA_DEFAULT_LOGIN, UMKA_DEFAULT_PASSWORD)
                .additionalInterceptors(new RequestLoggingInterceptor())
                .errorHandler(backendReportErrorHandler)
                .build();
    }

    @Bean
    public RestTemplate backendRestTemplate() {
        return new RestTemplateBuilder()
                .requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
                .additionalInterceptors(new RequestLoggingInterceptor())
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .indentOutput(false)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    @Bean("ccms")
    public List<String> ccms(@Value("#{'${backend.server.ccmID}'.split(',')}") List<String> ccmID) {
        return ccmID;
    }

    @Bean
    @Scope
    public ScheduledExecutorService taskExecutor() {
        return Executors.newScheduledThreadPool(1);
    }

}

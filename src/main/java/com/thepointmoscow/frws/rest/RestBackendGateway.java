package com.thepointmoscow.frws.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Component
@Slf4j
public class RestBackendGateway implements BackendGateway {

    private final RestTemplate restTemplate;

    // this is trick made for correct dates serialization.
    // need to make mapper included in message converters
    private final ObjectMapper objectMapper;

    @Value("${backend.server.url}")
    private String rootUrl;

    @Autowired
    public RestBackendGateway(RestTemplate backendRestTemplate, ObjectMapper objectMapper) {
        this.restTemplate = backendRestTemplate;
        this.objectMapper = objectMapper;
    }

    private final Supplier<HttpHeaders> HEADERS_SUPPLIER = () -> {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    };

    @Override
    @SneakyThrows
    public BackendCommand status(String ccmID, StatusResult statusResult) {
        HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(statusResult), HEADERS_SUPPLIER.get());
        ResponseEntity<BackendCommand> result = restTemplate.postForEntity(
                rootUrl + "/api/qkkm/status?ccmID={ccmID}"
                , requestEntity
                , BackendCommand.class
                , ccmID
        );
        log.info("Sent a status. RQ={}, RS={}", statusResult, result);
        return result.getBody().setCcmID(ccmID);
    }

    @Override
    @SneakyThrows
    public BackendCommand register(String ccmID, RegistrationResult registration) {
        HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(registration), HEADERS_SUPPLIER.get());
        ResponseEntity<BackendCommand> result = restTemplate.postForEntity(
                rootUrl + "/api/qkkm/registered?ccmID={ccmID}&issueID={issueID}"
                , requestEntity
                , BackendCommand.class
                , ccmID
                , registration.getRegistration().getIssueID()
        );
        log.info("Sent a registration. RQ={}, RS={}", registration, result);
        return result.getBody().setCcmID(ccmID);
    }

    @Override
    @SneakyThrows
    public BackendCommand error(String ccmID, Long issueID, FiscalResultError resultError) {
        HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(resultError), HEADERS_SUPPLIER.get());
        ResponseEntity<BackendCommand> result = restTemplate.postForEntity(
                rootUrl + "/api/qkkm/registered?ccmID={ccmID}&issueID={issueID}"
                , requestEntity
                , BackendCommand.class
                , ccmID
                , issueID
        );
        log.info("Sent a error. RQ={}, RS={}", resultError, result);
        return result.getBody().setCcmID(ccmID);
    }

    @Override
    @SneakyThrows
    public BackendCommand selectDoc(String ccmID, Long issueID, SelectResult select) {
        HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(select), HEADERS_SUPPLIER.get());
        ResponseEntity<BackendCommand> result = restTemplate.postForEntity(
                rootUrl + "/api/qkkm/select?ccmID={ccmID}&issueID={issueID}"
                , requestEntity
                , BackendCommand.class
                , ccmID
                , issueID
        );
        log.info("Sent a error. RQ={}, RS={}", select, result);
        return result.getBody().setCcmID(ccmID);
    }
}

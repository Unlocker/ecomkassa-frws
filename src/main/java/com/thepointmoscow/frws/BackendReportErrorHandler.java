package com.thepointmoscow.frws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.exceptions.FiscalException;
import com.thepointmoscow.frws.exceptions.FrwsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@Component
@Slf4j
public class BackendReportErrorHandler implements ResponseErrorHandler {
    private final ObjectMapper mapper;

    @Autowired
    public BackendReportErrorHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return httpResponse.getStatusCode().series() == CLIENT_ERROR
                || httpResponse.getStatusCode().series() == SERVER_ERROR;
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) {
        throw new FiscalException(makeFiscalResultErrorFromExceptionBody(httpResponse));
    }

    private FiscalResultError makeFiscalResultErrorFromExceptionBody(ClientHttpResponse httpResponse) {
        try {
            JsonNode response = mapper.readTree(httpResponse.getBody());
            final var resultDescription = response.path("document").path("message").path("resultDescription").textValue();
            final var result = response.path("document").path("result").intValue();
            return new FiscalResultError(result, resultDescription);
        } catch (IOException e) {
            log.error("Error parsing the response: {} ", e.getMessage());
            throw new FrwsException(e);
        }
    }
}
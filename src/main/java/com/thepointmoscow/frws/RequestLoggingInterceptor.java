package com.thepointmoscow.frws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request
            , byte[] body
            , ClientHttpRequestExecution execution
    ) throws IOException {

        try {
            ClientHttpResponse response = execution.execute(request, body);
            if (log.isDebugEnabled() && response.getBody() instanceof ByteArrayInputStream) {
                ByteArrayInputStream byteArrBodyInputStream = (ByteArrayInputStream) response.getBody();
                log.debug(
                        "request_method: {}, request_URI: {}, request_headers: {}, request_body: {}, "
                                + "response_status: {}, response_headers: {}, response_body: {}"
                        , request.getMethod()
                        , request.getURI()
                        , request.getHeaders()
                        , new String(body, CHARSET)
                        , response.getStatusCode()
                        , response.getHeaders()
                        , new String(byteArrBodyInputStream.readAllBytes(), CHARSET)
                );
                // explicitly rewinds a buffer after a log entry printed
                byteArrBodyInputStream.reset();
            }
            return response;
        } catch (Exception e) {
            log.warn(
                    "failed request_method: {}, request_URI: {}, request_headers: {}, request_body: {}, reason: {}"
                    , request.getMethod()
                    , request.getURI()
                    , request.getHeaders()
                    , new String(body, CHARSET)
                    , e.getMessage()
            );
            throw e;
        }

    }
}

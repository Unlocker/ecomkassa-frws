package com.thepointmoscow.frws;

import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        ClientHttpResponse response = execution.execute(request, body);
        if (log.isDebugEnabled()) {
            log.debug("request_method: {}, request_URI: {}, request_headers: {}, request_body: {}, " +
                            "response_status: {}, response_headers: {}, response_body: {}",
                    request.getMethod(), request.getURI(), request.getHeaders(), new String(body, CHARSET),
                    response.getStatusCode(), response.getHeaders(),
                    new String(ByteStreams.toByteArray(response.getBody()), CHARSET)
            );
        }
        return response;
    }
}

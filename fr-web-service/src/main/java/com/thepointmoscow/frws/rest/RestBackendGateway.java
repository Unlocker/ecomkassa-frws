package com.thepointmoscow.frws.rest;

import com.thepointmoscow.frws.BackendCommand;
import com.thepointmoscow.frws.BackendGateway;
import com.thepointmoscow.frws.RegistrationResult;
import com.thepointmoscow.frws.StatusResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class RestBackendGateway implements BackendGateway {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${backend.server.url}")
    private String rootUrl;
    @Value("${backend.server.username}")
    private String username;
    @Value("${backend.server.password}")
    private String password;
    @Value("${backend.server.ccmID}")
    private String ccmID;

    @Override
    public BackendCommand status(StatusResult statusResult) {
        ResponseEntity<BackendCommand> result = restTemplate.postForEntity(
                rootUrl + "/api/qkkm/status?ccmID={ccmID}", statusResult, BackendCommand.class, ccmID);
        log.info("Sent a status. RQ={}, RS={}", statusResult, result);
        return result.getBody();
    }

    @Override
    public BackendCommand register(RegistrationResult registration) {
        ResponseEntity<BackendCommand> result = restTemplate.postForEntity(
                rootUrl + "/api/qkkm/registered?ccmID={ccmID}&issueID={issueID}", registration,
                BackendCommand.class, ccmID, registration.getRegistration().getIssueID());
        log.info("Sent a registration. RQ={}, RS={}", registration, result);
        return result.getBody();
    }
}

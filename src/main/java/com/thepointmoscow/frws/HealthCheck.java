package com.thepointmoscow.frws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthCheck implements HealthIndicator {

    @Autowired
    private FiscalGateway fiscalGateway;

    @Override
    public Health health() {
        StatusResult status = fiscalGateway.status();
        if (status.getErrorCode() != 0) {
            return Health.down().withDetail("errorMessage", status.getStatusMessage()).build();
        }
        return Health.up().build();
    }
}

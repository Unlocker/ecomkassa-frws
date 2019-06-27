package com.thepointmoscow.frws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thepointmoscow.frws.fakes.LoggingFiscalGateway;
import com.thepointmoscow.frws.umka.UmkaFiscalGateway;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class AppConfig {

    @Bean
    public FiscalGateway fiscalGateway(ObjectMapper mapper, BuildProperties buildProperties,
            RestTemplate restTemplate) {

        try {
            switch (FiscalServerType.valueOf(fgType)) {
            case mock:
                return new LoggingFiscalGateway(buildProperties);
            case umka:
                return new UmkaFiscalGateway(fgHost, fgPort, buildProperties, mapper, restTemplate);
            default:
                throw new IllegalArgumentException(getFgType());
            }
        } catch (IllegalArgumentException e) {
            log.error("'fiscal.server.type' property value is unknown: {}. "
                            + "Intended to use some of 'mock', 'qkkm' or 'umka'",
                    fgType);
            throw e;
        }
    }

    @Getter
    @Setter
    @Value("${fiscal.server.host}")
    private String fgHost;
    @Getter
    @Setter
    @Value("${fiscal.server.port}")
    private int fgPort;
    @Getter
    @Setter
    @Value("${fiscal.server.type}")
    private String fgType;

}

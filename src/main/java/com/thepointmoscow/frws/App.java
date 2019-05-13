package com.thepointmoscow.frws;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ComponentScan(basePackages = "com.thepointmoscow.frws")
@Slf4j
public class App implements CommandLineRunner {

    @Getter
    private final BackendGateway backend;
    @Getter
    private final FiscalGateway fiscal;
    @Value("${task.delay:30}")
    @Getter
    private Long executionDelay;

    private final List<String> ccms;

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }

    private final ScheduledExecutorService executor;

    @Autowired
    public App(ScheduledExecutorService executor, BackendGateway backend, FiscalGateway fiscal,
            @Qualifier("ccms") List<String> ccms) {
        this.executor = executor;
        this.backend = backend;
        this.fiscal = fiscal;
        this.ccms = ccms;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(String... args) {
        executor.submit(new FetchTask(backend, fiscal, this::callback, ccms));
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Shutdown raised an error.", e);
            Thread.currentThread().interrupt();
        }
    }

    private void callback(Runnable task, Boolean hasHits) {
        if (hasHits) {
            executor.submit(task);
        } else {
            executor.schedule(task, getExecutionDelay(), TimeUnit.SECONDS);
        }
    }
}

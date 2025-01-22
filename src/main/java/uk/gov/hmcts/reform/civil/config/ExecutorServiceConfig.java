package uk.gov.hmcts.reform.civil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Profile("!functional & !local")
public class ExecutorServiceConfig {

    @Value("${azure.servicebus.threads}")
    private int concurrentSessions;

    @Bean("ccdCaseEventExecutorService")
    public ExecutorService createCcdCaseEventExecutorService() {
        return Executors.newFixedThreadPool(concurrentSessions);
    }

}

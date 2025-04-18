package uk.gov.hmcts.reform.civil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorServiceConfiguration {

    @Value("${azure.service-bus.ccd-events-topic.threads}")
    private int concurrentSessions;

    @Bean("ccdCaseEventExecutorService")
    public ExecutorService createCcdCaseEventExecutorService() {
        return Executors.newFixedThreadPool(concurrentSessions);
    }

}

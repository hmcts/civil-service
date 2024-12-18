package uk.gov.hmcts.reform.civil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@Profile("!functional & !local")
public class ExecutorServiceConfig {

    @Value("${azure.servicebus.threads}")
    private int concurrentSessions;

    @Value("${scheduledExecutors.messageProcessing.threadPoolSize}")
    private int processingThreadPoolSize;

    @Value("${scheduledExecutors.messageReadiness.threadPoolSize}")
    private int readinessThreadPoolSize;

    @Bean("ccdCaseEventExecutorService")
    public ExecutorService createCcdCaseEventExecutorService() {
        return Executors.newFixedThreadPool(concurrentSessions);
    }

    @Bean("deadLetterQueueExecutorService")
    public ExecutorService createDeadLetterQueueExecutorService() {
        return Executors.newFixedThreadPool(concurrentSessions);
    }

    @Bean("ccdEventExecutorService")
    public ExecutorService createccdEventExecutorService() {
        return Executors.newFixedThreadPool(concurrentSessions);
    }

    @Bean("databaseMessageExecutorService")
    public ScheduledExecutorService createDatabaseMessageExecutorService() {
        return Executors.newScheduledThreadPool(processingThreadPoolSize);
    }

    @Bean("messageReadinessExecutorService")
    public ScheduledExecutorService createMessageReadinessExecutorService() {
        return Executors.newScheduledThreadPool(readinessThreadPoolSize);
    }
}

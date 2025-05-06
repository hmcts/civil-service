package uk.gov.hmcts.reform.civil.service.servicebus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExceptionRecordProcessorExecutor {

    @Value("${scheduledExecutors.messageProcessing.pollIntervalMilliSeconds}")
    private int pollInterval;

    private final ScheduledExecutorService databaseMessageExecutorService;
    private final ExceptionRecordConsumer exceptionRecordConsumer;

    @PostConstruct
    public void start() {
        log.info("Starting Database message executor");
        databaseMessageExecutorService.scheduleWithFixedDelay(
            exceptionRecordConsumer,
            5000,
            pollInterval,
            TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down Database message executor");
        databaseMessageExecutorService.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!databaseMessageExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                databaseMessageExecutorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!databaseMessageExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            databaseMessageExecutorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        log.info("Shut down Database message executor");
    }

}

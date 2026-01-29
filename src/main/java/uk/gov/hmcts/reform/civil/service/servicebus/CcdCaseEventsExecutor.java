package uk.gov.hmcts.reform.civil.service.servicebus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.client.CcdCaseEventsConsumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
@Component
@ConditionalOnProperty("azure.service-bus.ccd-events-topic.enabled")
@RequiredArgsConstructor
public class CcdCaseEventsExecutor {

    @Value("${azure.service-bus.ccd-events-topic.threads}")
    private int concurrentSessions;

    private final CcdCaseEventsConsumer serviceBusTask;
    private final ExecutorService ccdCaseEventExecutorService;

    @PostConstruct
    public void start() {
        log.info("Starting CCD case events executor");
        IntStream.range(0, concurrentSessions).forEach(
            task -> ccdCaseEventExecutorService.execute(serviceBusTask));
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down CCD case events executor");
        serviceBusTask.stop();
        ccdCaseEventExecutorService.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!ccdCaseEventExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                ccdCaseEventExecutorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!ccdCaseEventExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            ccdCaseEventExecutorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        log.info("Shut down CCD case events executor");
    }
}

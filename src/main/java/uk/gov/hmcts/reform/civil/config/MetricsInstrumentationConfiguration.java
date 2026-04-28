package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MetricsInstrumentationConfiguration {

    private final TelemetryClient telemetryClient;
    private final ThreadPoolTaskExecutor asyncHandlerExecutor;
    private final AtomicLong asyncHandlerRejectedCount;
    private final PoolingHttpClientConnectionManager connectionManager4;

    private final AtomicLong lastCompletedCount = new AtomicLong(0);

    @Scheduled(fixedRateString = "${metrics.report.rate:300000}")
    public void reportMetrics() {
        try {
            reportAsyncExecutorMetrics();
            reportHttpClientPoolMetrics();
        } catch (Exception e) {
            log.error("Error reporting metrics", e);
        }
    }

    private void reportAsyncExecutorMetrics() {
        if (asyncHandlerExecutor != null) {
            int activeCount = asyncHandlerExecutor.getActiveCount();
            int queuedCount = asyncHandlerExecutor.getThreadPoolExecutor().getQueue().size();
            int poolSize = asyncHandlerExecutor.getPoolSize();
            long totalCompleted = asyncHandlerExecutor.getThreadPoolExecutor().getCompletedTaskCount();
            long completedThisWindow = totalCompleted - lastCompletedCount.getAndSet(totalCompleted);

            telemetryClient.trackMetric("executor.active", activeCount);
            telemetryClient.trackMetric("executor.queued", queuedCount);
            telemetryClient.trackMetric("executor.pool_size", poolSize);
            telemetryClient.trackMetric("executor.completed", completedThisWindow);
            if (asyncHandlerRejectedCount != null) {
                telemetryClient.trackMetric("executor.rejected", asyncHandlerRejectedCount.get());
            }
        }
    }

    private void reportHttpClientPoolMetrics() {
        if (connectionManager4 != null) {
            org.apache.http.pool.PoolStats stats = connectionManager4.getTotalStats();
            reportPoolStats(stats.getLeased(), stats.getAvailable(), stats.getPending(), stats.getMax());
        }
    }

    private void reportPoolStats(int leased, int available, int pending, int max) {
        telemetryClient.trackMetric("httpclient.pool.leased", leased);
        telemetryClient.trackMetric("httpclient.pool.available", available);
        telemetryClient.trackMetric("httpclient.pool.pending", pending);
        telemetryClient.trackMetric("httpclient.pool.max", max);
    }
}

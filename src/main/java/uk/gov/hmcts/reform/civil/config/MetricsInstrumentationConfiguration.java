package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.pool.PoolStats;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MetricsInstrumentationConfiguration {

    private final TelemetryClient telemetryClient;
    private final ThreadPoolTaskExecutor asyncHandlerExecutor;
    private final PoolingHttpClientConnectionManager connectionManager5;
    private final org.apache.http.impl.conn.PoolingHttpClientConnectionManager connectionManager4;

    @Scheduled(fixedRate = 60000)
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
            long completedCount = asyncHandlerExecutor.getThreadPoolExecutor().getCompletedTaskCount();
            int maxPoolSize = asyncHandlerExecutor.getMaxPoolSize();
            int queueCapacity = asyncHandlerExecutor.getThreadPoolExecutor().getQueue().remainingCapacity()
                + queuedCount;

            // Raw metrics
            telemetryClient.trackMetric("executor.active", activeCount);
            telemetryClient.trackMetric("executor.queued", queuedCount);
            telemetryClient.trackMetric("executor.pool_size", poolSize);
            telemetryClient.trackMetric("executor.completed", (double) completedCount);

            // Calculated metrics
            if (maxPoolSize > 0) {
                telemetryClient.trackMetric("executor.saturation.percent", (double) (activeCount * 100) / maxPoolSize);
            }
            if (queueCapacity > 0) {
                telemetryClient.trackMetric("executor.queue.percent_full", (double) (queuedCount * 100) / queueCapacity);
                telemetryClient.trackMetric("executor.queue.remaining_capacity", (double) (queueCapacity - queuedCount));
            }

            // Health signals
            boolean isNearSaturation = maxPoolSize > 0 && activeCount >= (maxPoolSize * 0.8);
            boolean isQueueNearFull = queueCapacity > 0 && queuedCount >= (queueCapacity * 0.8);

            if (isNearSaturation) {
                Map<String, String> properties = new HashMap<>();
                properties.put("active", String.valueOf(activeCount));
                properties.put("max", String.valueOf(maxPoolSize));
                properties.put("near_saturation", "true");
                telemetryClient.trackEvent("executor.near_saturation", properties, null);
            }
            if (isQueueNearFull) {
                Map<String, String> properties = new HashMap<>();
                properties.put("queued", String.valueOf(queuedCount));
                properties.put("capacity", String.valueOf(queueCapacity));
                properties.put("near_full", "true");
                telemetryClient.trackEvent("executor.queue_near_full", properties, null);
            }
        }
    }

    private void reportHttpClientPoolMetrics() {
        // HttpClient 5 (RestTemplate)
        if (connectionManager5 != null) {
            PoolStats stats = connectionManager5.getTotalStats();
            reportPoolStats("httpclient5", stats.getLeased(), stats.getAvailable(), stats.getPending(), stats.getMax());
        }

        // HttpClient 4 (Feign)
        if (connectionManager4 != null) {
            org.apache.http.pool.PoolStats stats = connectionManager4.getTotalStats();
            reportPoolStats("httpclient4", stats.getLeased(), stats.getAvailable(), stats.getPending(), stats.getMax());
        }
    }

    private void reportPoolStats(String prefix, int leased, int available, int pending, int max) {
        telemetryClient.trackMetric(prefix + ".pool.leased", (double) leased);
        telemetryClient.trackMetric(prefix + ".pool.available", (double) available);
        telemetryClient.trackMetric(prefix + ".pool.pending", (double) pending);
        telemetryClient.trackMetric(prefix + ".pool.max", (double) max);

        // Calculated metrics
        if (max > 0) {
            telemetryClient.trackMetric(prefix + ".pool.utilization.percent", (double) (leased * 100) / max);
        }
        if (leased > 0) {
            telemetryClient.trackMetric(prefix + ".pool.waiting_ratio", (double) (pending * 100) / leased);
        }
        telemetryClient.trackMetric(prefix + ".pool.exhaustion_risk", (available == 0 && pending > 0) ? 100.0 : 0.0);

        // Health signals
        boolean isPoolExhausted = available == 0 && pending > 0;
        boolean isPoolHighlyUtilized = max > 0 && leased >= (max * 0.8);

        if (isPoolExhausted || isPoolHighlyUtilized) {
            Map<String, String> properties = new HashMap<>();
            properties.put("leased", String.valueOf(leased));
            properties.put("max", String.valueOf(max));
            properties.put("pending", String.valueOf(pending));
            properties.put("utilization", String.valueOf(max > 0 ? (leased * 100) / max : 0));
            properties.put("exhausted", String.valueOf(isPoolExhausted));
            properties.put("highly_utilized", String.valueOf(isPoolHighlyUtilized));
            telemetryClient.trackEvent(prefix + ".pool_health", properties, null);
        }
    }
}

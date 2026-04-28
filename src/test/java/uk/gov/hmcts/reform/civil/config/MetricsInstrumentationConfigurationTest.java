package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.pool.PoolStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsInstrumentationConfigurationTest {

    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private ThreadPoolTaskExecutor asyncHandlerExecutor;
    @Mock
    private PoolingHttpClientConnectionManager connectionManager5;
    @Mock
    private org.apache.http.impl.conn.PoolingHttpClientConnectionManager connectionManager4;
    @Mock
    private AtomicLong asyncHandlerRejectedCount;

    private MetricsInstrumentationConfiguration metricsConfiguration;

    @BeforeEach
    void setUp() {
        metricsConfiguration = new MetricsInstrumentationConfiguration(
            telemetryClient,
            asyncHandlerExecutor,
            asyncHandlerRejectedCount,
            connectionManager5,
            connectionManager4
        );
    }

    @Test
    @SuppressWarnings({"unchecked", "java:S6068"})
    void shouldReportAsyncExecutorMetrics_WhenExecutorIsNotNull() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, asyncHandlerExecutor, asyncHandlerRejectedCount, null, null);
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        BlockingQueue<Runnable> mockQueue = mock(BlockingQueue.class);
        when(asyncHandlerExecutor.getActiveCount()).thenReturn(50);
        when(asyncHandlerExecutor.getPoolSize()).thenReturn(60);
        when(asyncHandlerExecutor.getThreadPoolExecutor()).thenReturn(mockExecutor);
        when(mockExecutor.getQueue()).thenReturn(mockQueue);
        when(mockQueue.size()).thenReturn(20);
        when(mockExecutor.getCompletedTaskCount()).thenReturn(1000L);
        when(asyncHandlerRejectedCount.get()).thenReturn(5L);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient).trackMetric(eq("executor.active"), eq(50.0));
        verify(telemetryClient).trackMetric(eq("executor.queued"), eq(20.0));
        verify(telemetryClient).trackMetric(eq("executor.pool_size"), eq(60.0));
        verify(telemetryClient).trackMetric(eq("executor.completed"), eq(1000.0));
        verify(telemetryClient).trackMetric(eq("executor.rejected"), eq(5.0));
    }

    @Test
    @SuppressWarnings({"unchecked", "java:S6068"})
    void shouldReportHttpClient5PoolMetrics() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, null, null, connectionManager5, null);
        PoolStats stats = mock(PoolStats.class);
        when(stats.getLeased()).thenReturn(10);
        when(stats.getAvailable()).thenReturn(5);
        when(stats.getPending()).thenReturn(2);
        when(stats.getMax()).thenReturn(25);
        when(connectionManager5.getTotalStats()).thenReturn(stats);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient).trackMetric(eq("httpclient5.pool.leased"), eq(10.0));
        verify(telemetryClient).trackMetric(eq("httpclient5.pool.available"), eq(5.0));
        verify(telemetryClient).trackMetric(eq("httpclient5.pool.pending"), eq(2.0));
        verify(telemetryClient).trackMetric(eq("httpclient5.pool.max"), eq(25.0));
    }

    @Test
    @SuppressWarnings({"unchecked", "java:S6068"})
    void shouldReportHttpClient4PoolMetrics() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, null, null, null, connectionManager4);
        org.apache.http.pool.PoolStats stats = mock(org.apache.http.pool.PoolStats.class);
        when(stats.getLeased()).thenReturn(10);
        when(stats.getAvailable()).thenReturn(5);
        when(stats.getPending()).thenReturn(2);
        when(stats.getMax()).thenReturn(25);
        when(connectionManager4.getTotalStats()).thenReturn(stats);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient).trackMetric(eq("httpclient4.pool.leased"), eq(10.0));
        verify(telemetryClient).trackMetric(eq("httpclient4.pool.available"), eq(5.0));
        verify(telemetryClient).trackMetric(eq("httpclient4.pool.pending"), eq(2.0));
        verify(telemetryClient).trackMetric(eq("httpclient4.pool.max"), eq(25.0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleZeroValuesGracefully_ToAvoidDivisionByZero() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, asyncHandlerExecutor, asyncHandlerRejectedCount, connectionManager5, null);
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        BlockingQueue<Runnable> mockQueue = mock(BlockingQueue.class);
        when(asyncHandlerExecutor.getThreadPoolExecutor()).thenReturn(mockExecutor);
        when(mockExecutor.getQueue()).thenReturn(mockQueue);
        when(mockQueue.size()).thenReturn(0);
        when(asyncHandlerRejectedCount.get()).thenReturn(0L);

        PoolStats stats = mock(PoolStats.class);
        when(stats.getLeased()).thenReturn(0);
        when(stats.getAvailable()).thenReturn(0);
        when(stats.getPending()).thenReturn(0);
        when(stats.getMax()).thenReturn(0);
        when(connectionManager5.getTotalStats()).thenReturn(stats);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient, atLeastOnce()).trackMetric(anyString(), anyDouble());
    }

    @Test
    void shouldHandleExceptionDuringMetricsReporting() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, asyncHandlerExecutor, null, null, null);
        when(asyncHandlerExecutor.getActiveCount()).thenThrow(new RuntimeException("executor error"));

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient, never()).trackMetric(anyString(), anyDouble());
    }

    @Test
    void shouldHandleNullBeansGracefully() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, null, null, null, null);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient, never()).trackMetric(anyString(), anyDouble());
    }
}

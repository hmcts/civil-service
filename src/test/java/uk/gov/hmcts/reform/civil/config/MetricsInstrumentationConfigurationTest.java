package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.pool.PoolStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Captor
    private ArgumentCaptor<Map<String, String>> propertiesCaptor;

    private MetricsInstrumentationConfiguration metricsConfiguration;

    @BeforeEach
    void setUp() {
        metricsConfiguration = new MetricsInstrumentationConfiguration(
            telemetryClient,
            asyncHandlerExecutor,
            connectionManager5,
            connectionManager4
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReportAsyncExecutorMetrics_WhenExecutorIsNotNull() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, asyncHandlerExecutor, null, null);
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        BlockingQueue<Runnable> mockQueue = mock(BlockingQueue.class);
        when(asyncHandlerExecutor.getActiveCount()).thenReturn(50);
        when(asyncHandlerExecutor.getPoolSize()).thenReturn(60);
        when(asyncHandlerExecutor.getMaxPoolSize()).thenReturn(100);
        when(asyncHandlerExecutor.getThreadPoolExecutor()).thenReturn(mockExecutor);
        when(mockExecutor.getQueue()).thenReturn(mockQueue);
        when(mockQueue.size()).thenReturn(20);
        when(mockQueue.remainingCapacity()).thenReturn(80);
        when(mockExecutor.getCompletedTaskCount()).thenReturn(1000L);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient).trackMetric(eq("executor.active"), eq(50.0));
        verify(telemetryClient).trackMetric(eq("executor.queued"), eq(20.0));
        verify(telemetryClient).trackMetric(eq("executor.pool_size"), eq(60.0));
        verify(telemetryClient).trackMetric(eq("executor.completed"), eq(1000.0));
        verify(telemetryClient).trackMetric(eq("executor.saturation.percent"), eq(50.0));
        verify(telemetryClient).trackMetric(eq("executor.queue.percent_full"), eq(20.0));
        verify(telemetryClient).trackMetric(eq("executor.queue.remaining_capacity"), eq(80.0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldTrackExecutorEvents_WhenNearSaturation() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, asyncHandlerExecutor, null, null);
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        BlockingQueue<Runnable> mockQueue = mock(BlockingQueue.class);
        when(asyncHandlerExecutor.getActiveCount()).thenReturn(80); // 80% of 100
        when(asyncHandlerExecutor.getMaxPoolSize()).thenReturn(100);
        when(asyncHandlerExecutor.getThreadPoolExecutor()).thenReturn(mockExecutor);
        when(mockExecutor.getQueue()).thenReturn(mockQueue);
        when(mockQueue.size()).thenReturn(800);
        when(mockQueue.remainingCapacity()).thenReturn(200); // 80% of 1000
        when(asyncHandlerExecutor.getPoolSize()).thenReturn(80);
        when(mockExecutor.getCompletedTaskCount()).thenReturn(100L);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient).trackEvent(eq("executor.near_saturation"), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue()).containsEntry("active", "80");
        assertThat(propertiesCaptor.getValue()).containsEntry("max", "100");

        verify(telemetryClient).trackEvent(eq("executor.queue_near_full"), propertiesCaptor.capture(), isNull());
        assertThat(propertiesCaptor.getValue()).containsEntry("queued", "800");
        assertThat(propertiesCaptor.getValue()).containsEntry("capacity", "1000");
    }

    @Test
    void shouldReportHttpClient5PoolMetrics() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, null, connectionManager5, null);
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
        verify(telemetryClient).trackMetric(eq("httpclient5.pool.utilization.percent"), eq(40.0));
        verify(telemetryClient).trackMetric(eq("httpclient5.pool.waiting_ratio"), eq(20.0));
    }

    @Test
    void shouldReportHttpClient4PoolMetrics() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, null, null, connectionManager4);
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
        verify(telemetryClient).trackMetric(eq("httpclient4.pool.utilization.percent"), eq(40.0));
        verify(telemetryClient).trackMetric(eq("httpclient4.pool.waiting_ratio"), eq(20.0));
    }

    @Test
    void shouldTrackPoolHealthEvent_WhenExhausted() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, null, connectionManager5, null);
        PoolStats stats = mock(PoolStats.class);
        when(stats.getLeased()).thenReturn(25);
        when(stats.getAvailable()).thenReturn(0);
        when(stats.getPending()).thenReturn(5);
        when(stats.getMax()).thenReturn(25);
        when(connectionManager5.getTotalStats()).thenReturn(stats);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient).trackEvent(eq("httpclient5.pool_health"), propertiesCaptor.capture(), isNull());
        Map<String, String> properties = propertiesCaptor.getValue();
        assertThat(properties).containsEntry("exhausted", "true");
        assertThat(properties).containsEntry("highly_utilized", "true");
        assertThat(properties).containsEntry("pending", "5");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleZeroValuesGracefully_ToAvoidDivisionByZero() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, asyncHandlerExecutor, connectionManager5, null);
        when(asyncHandlerExecutor.getMaxPoolSize()).thenReturn(0);
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        BlockingQueue<Runnable> mockQueue = mock(BlockingQueue.class);
        when(asyncHandlerExecutor.getThreadPoolExecutor()).thenReturn(mockExecutor);
        when(mockExecutor.getQueue()).thenReturn(mockQueue);
        when(mockQueue.size()).thenReturn(0);
        when(mockQueue.remainingCapacity()).thenReturn(0);

        PoolStats stats = mock(PoolStats.class);
        when(stats.getLeased()).thenReturn(0);
        when(stats.getAvailable()).thenReturn(0);
        when(stats.getPending()).thenReturn(0);
        when(stats.getMax()).thenReturn(0);
        when(connectionManager5.getTotalStats()).thenReturn(stats);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        // Should not throw ArithmeticException
        verify(telemetryClient, atLeastOnce()).trackMetric(anyString(), anyDouble());
        verify(telemetryClient, never()).trackMetric(eq("executor.saturation.percent"), anyDouble());
        verify(telemetryClient, never()).trackMetric(eq("executor.queue.percent_full"), anyDouble());
        verify(telemetryClient, never()).trackMetric(eq("httpclient5.pool.utilization.percent"), anyDouble());
        verify(telemetryClient, never()).trackMetric(eq("httpclient5.pool.waiting_ratio"), anyDouble());
    }

    @Test
    void shouldHandleNullBeansGracefully() {
        // Given
        metricsConfiguration = new MetricsInstrumentationConfiguration(telemetryClient, null, null, null);

        // When
        metricsConfiguration.reportMetrics();

        // Then
        verify(telemetryClient, never()).trackMetric(anyString(), anyDouble());
        verify(telemetryClient, never()).trackEvent(anyString(), anyMap(), isNull());
    }
}

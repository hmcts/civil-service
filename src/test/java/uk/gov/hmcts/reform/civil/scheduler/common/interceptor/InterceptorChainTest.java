package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InterceptorChainTest {

    @Test
    void shouldExecuteInterceptorsInOrderAndThenFinalTask() {
        List<String> executionOrder = new ArrayList<>();
        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");
        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        SchedulerInterceptor<String> interceptor1 = (ctx, chain) -> {
            executionOrder.add("interceptor1");
            chain.next(ctx);
        };
        SchedulerInterceptor<String> interceptor2 = (ctx, chain) -> {
            executionOrder.add("interceptor2");
            chain.next(ctx);
        };

        InterceptorChain<String> chain = new InterceptorChain<>(
            List.of(interceptor1, interceptor2),
            ctx -> taskExecuted.set(true)
        );
        chain.next(context);

        assertThat(executionOrder).containsExactly("interceptor1", "interceptor2");
        assertThat(taskExecuted.get()).isTrue();
        assertThat(chain.wasTaskExecuted()).isTrue();
    }

    @Test
    void shouldAbortExecution_whenInterceptorDoesNotCallNext() {
        List<String> executionOrder = new ArrayList<>();
        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");
        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        SchedulerInterceptor<String> interceptor1 = (ctx, chain) -> {
            executionOrder.add("interceptor1");
            // next(ctx) is not called
        };
        SchedulerInterceptor<String> interceptor2 = (ctx, chain) -> {
            executionOrder.add("interceptor2");
            chain.next(ctx);
        };

        InterceptorChain<String> chain = new InterceptorChain<>(
            List.of(interceptor1, interceptor2),
            ctx -> taskExecuted.set(true)
        );
        chain.next(context);

        assertThat(executionOrder).containsExactly("interceptor1");
        assertThat(taskExecuted.get()).isFalse();
        assertThat(chain.wasTaskExecuted()).isFalse();
    }

    @Test
    void shouldAbortExecution_whenInterceptorThrowsException() {
        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");
        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        SchedulerInterceptor<String> interceptor1 = (ctx, chain) -> {
            throw new TaskAbortedException("Aborted");
        };

        InterceptorChain<String> chain = new InterceptorChain<>(
            List.of(interceptor1),
            ctx -> taskExecuted.set(true)
        );

        try {
            chain.next(context);
        } catch (TaskAbortedException e) {
            assertThat(e.getReason()).isEqualTo("Aborted");
        }

        assertThat(taskExecuted.get()).isFalse();
        assertThat(chain.wasTaskExecuted()).isFalse();
    }

    @Test
    void shouldRecordMetricsForInterceptorsAndFinalTask() {
        StopWatch interceptor1StopWatch = mock(StopWatch.class);
        StopWatch finalTaskStopWatch = mock(StopWatch.class);

        // Simulate Interceptor1 taking 60ms total, and FinalTask taking 20ms total.
        // Exclusive time for Interceptor1 should be 60 - 20 = 40ms.
        when(interceptor1StopWatch.getTotalTimeNanos()).thenReturn(60_000_000L);
        when(finalTaskStopWatch.getTotalTimeNanos()).thenReturn(20_000_000L);

        Function<String, StopWatch> factory = name -> {
            if ("Interceptor1".equals(name)) {
                return interceptor1StopWatch;
            }
            if ("FinalTask".equals(name)) {
                return finalTaskStopWatch;
            }
            return new StopWatch(name);
        };

        class Interceptor1 implements SchedulerInterceptor<String> {
            @Override
            public void accept(InterceptorContext<String> ctx, InterceptorChain<String> chain) {
                chain.next(ctx);
            }
        }

        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");

        InterceptorChain<String> chain = new InterceptorChain<>(
            List.of(new Interceptor1()),
            ctx -> {
                // do nothing
            },
            factory
        );
        chain.next(context);

        assertThat(context.getMetrics()).containsKey("Interceptor1");
        assertThat(context.getMetrics().get("Interceptor1")).isEqualTo(40);
        assertThat(context.getMetrics()).containsKey("FinalTask");
        assertThat(context.getMetrics().get("FinalTask")).isEqualTo(20);
    }
}

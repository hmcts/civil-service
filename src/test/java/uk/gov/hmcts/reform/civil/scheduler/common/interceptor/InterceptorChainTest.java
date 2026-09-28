package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;

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
        // Simulate Interceptor1 taking 60ms total, and FinalTask taking 20ms total.
        // Exclusive time for Interceptor1 should be 60 - 20 = 40ms.
        Queue<Long> nanoTimes = new LinkedList<>(List.of(
            0L,
            5_000_000L,
            25_000_000L,
            60_000_000L
        ));
        LongSupplier nanoTimeSupplier = nanoTimes::poll;

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
            nanoTimeSupplier
        );
        chain.next(context);

        assertThat(context.getMetrics()).containsEntry("Interceptor1", 40L);
        assertThat(context.getMetrics()).containsEntry("FinalTask", 20L);
    }
}

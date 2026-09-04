package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
}

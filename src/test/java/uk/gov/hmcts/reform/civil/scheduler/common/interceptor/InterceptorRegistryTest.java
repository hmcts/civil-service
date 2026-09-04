package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptorRegistryTest {

    @Test
    void shouldBuildChainWithSupportedInterceptorsSortedByOrder() {
        List<String> executionOrder = new ArrayList<>();

        SchedulerInterceptor<String> interceptor1 = new TestInterceptor("interceptor1", 2, "scheduler1", executionOrder);
        SchedulerInterceptor<String> interceptor2 = new TestInterceptor("interceptor2", 1, "scheduler1", executionOrder);
        SchedulerInterceptor<String> interceptor3 = new TestInterceptor("interceptor3", 0, "scheduler2", executionOrder);

        InterceptorRegistry registry = new InterceptorRegistry(List.of(interceptor1, interceptor2, interceptor3));

        ScheduledTask<String, String> task = new ScheduledTask<>() {

            @Override
            public void accept(String s) {
                executionOrder.add("task");
            }

            @Override
            public String getItemId(String s) {
                return s;
            }
        };

        InterceptorChain<String> chain = registry.buildChain("scheduler1", task);
        chain.next(new InterceptorContext<>("scheduler1", "item"));

        assertThat(executionOrder).containsExactly("interceptor2", "interceptor1", "task");
        assertThat(chain.wasTaskExecuted()).isTrue();
    }

    private static class TestInterceptor implements SchedulerInterceptor<String> {
        private final String name;
        private final int order;
        private final String supportedScheduler;
        private final List<String> executionOrder;

        TestInterceptor(String name, int order, String supportedScheduler, List<String> executionOrder) {
            this.name = name;
            this.order = order;
            this.supportedScheduler = supportedScheduler;
            this.executionOrder = executionOrder;
        }

        @Override
        public void accept(InterceptorContext<String> context, InterceptorChain<String> chain) {
            executionOrder.add(name);
            chain.next(context);
        }

        @Override
        public boolean supports(String schedulerName) {
            return schedulerName.equals(supportedScheduler);
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}

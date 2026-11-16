package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptorChainFactoryTest {

    @Test
    void shouldSortInterceptorsByOrder() {
        List<String> executionOrder = new ArrayList<>();
        SchedulerInterceptor<String> interceptor1 = new TestInterceptor("interceptor1", 2, executionOrder);
        SchedulerInterceptor<String> interceptor2 = new TestInterceptor("interceptor2", 1, executionOrder);
        SchedulerInterceptor<String> interceptor3 = new TestInterceptor("interceptor3", 0, executionOrder);

        InterceptorChainFactory factory = new InterceptorChainFactory();
        List<SchedulerInterceptor<String>> sorted = factory.sortInterceptors(List.of(interceptor1, interceptor2, interceptor3));

        assertThat(sorted).containsExactly(interceptor3, interceptor2, interceptor1);
    }

    @Test
    void shouldBuildChainWithSortedInterceptors() {
        List<String> executionOrder = new ArrayList<>();
        SchedulerInterceptor<String> interceptor1 = new TestInterceptor("interceptor1", 2, executionOrder);
        SchedulerInterceptor<String> interceptor2 = new TestInterceptor("interceptor2", 1, executionOrder);

        InterceptorChainFactory factory = new InterceptorChainFactory();
        List<SchedulerInterceptor<String>> sorted = List.of(interceptor2, interceptor1);

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

        InterceptorChain<String> chain = factory.buildChain(task, sorted);
        chain.next(new InterceptorContext<>("scheduler1", "item"));

        assertThat(executionOrder).containsExactly("interceptor2", "interceptor1", "task");
        assertThat(chain.wasTaskExecuted()).isTrue();
    }

    private static class TestInterceptor implements SchedulerInterceptor<String> {
        private final String name;
        private final int order;
        private final List<String> executionOrder;

        TestInterceptor(String name, int order, List<String> executionOrder) {
            this.name = name;
            this.order = order;
            this.executionOrder = executionOrder;
        }

        @Override
        public void accept(InterceptorContext<String> context, InterceptorChain<String> chain) {
            executionOrder.add(name);
            chain.next(context);
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}

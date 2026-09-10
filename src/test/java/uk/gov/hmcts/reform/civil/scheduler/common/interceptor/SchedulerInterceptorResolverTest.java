package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerInterceptorResolverTest {

    private List<SchedulerInterceptor<?>> allInterceptors;
    private SchedulerInterceptorResolver resolver;

    @BeforeEach
    void setUp() {
        allInterceptors = new ArrayList<>();
        resolver = new SchedulerInterceptorResolver(allInterceptors);
    }

    @Test
    void shouldIncludeDefaultInterceptors_whenCompatible() {
        // Given
        SchedulerInterceptor<CaseDetails> compatibleInterceptor = interceptorFor(CaseDetails.class).order(1).build();
        SchedulerInterceptor<String> incompatibleInterceptor = interceptorFor(String.class).order(1).build();
        allInterceptors.add(compatibleInterceptor);
        allInterceptors.add(incompatibleInterceptor);

        ScheduledTask<CaseDetails, Long> task = new CaseDetailsTask();

        ScheduledTaskConfiguration<CaseDetails, Long> config = ScheduledTaskConfiguration.<CaseDetails, Long>builder()
            .scheduledTask(task)
            .build();

        // When
        List<SchedulerInterceptor<CaseDetails>> resolved = resolver.resolveInterceptors(config);

        // Then
        assertThat(resolved).containsExactly(compatibleInterceptor);
    }

    @Test
    void shouldBypassDefaultInterceptors_whenUseDefaultInterceptorsIsFalse() {
        // Given
        SchedulerInterceptor<CaseDetails> compatibleInterceptor = interceptorFor(CaseDetails.class).order(1).build();
        allInterceptors.add(compatibleInterceptor);

        ScheduledTask<CaseDetails, Long> task = new CaseDetailsTask();

        ScheduledTaskConfiguration<CaseDetails, Long> config = ScheduledTaskConfiguration.<CaseDetails, Long>builder()
            .scheduledTask(task)
            .useDefaultInterceptors(false)
            .build();

        // When
        List<SchedulerInterceptor<CaseDetails>> resolved = resolver.resolveInterceptors(config);

        // Then
        assertThat(resolved).isEmpty();
    }

    @Test
    void shouldMergeInterceptors() {
        // Given
        SchedulerInterceptor<CaseDetails> defaultInterceptor = interceptorFor(CaseDetails.class).order(10).build();
        SchedulerInterceptor<CaseDetails> specificInterceptor = interceptorFor(CaseDetails.class).order(5).build();
        allInterceptors.add(defaultInterceptor);

        ScheduledTask<CaseDetails, Long> task = new CaseDetailsTask();

        ScheduledTaskConfiguration<CaseDetails, Long> config = ScheduledTaskConfiguration.<CaseDetails, Long>builder()
            .scheduledTask(task)
            .interceptors(List.of(specificInterceptor))
            .build();

        // When
        List<SchedulerInterceptor<CaseDetails>> resolved = resolver.resolveInterceptors(config);

        // Then
        assertThat(resolved).containsExactly(defaultInterceptor, specificInterceptor);
    }

    private <T> InterceptorBuilder<T> interceptorFor(Class<T> type) {
        return new InterceptorBuilder<>(type);
    }

    private static class InterceptorBuilder<T> {
        private int order = 0;
        private final Class<T> type;

        public InterceptorBuilder(Class<T> type) {
            this.type = type;
        }

        public InterceptorBuilder<T> order(int order) {
            this.order = order;
            return this;
        }

        @SuppressWarnings("unchecked")
        public SchedulerInterceptor<T> build() {
            if (type == CaseDetails.class) {
                return (SchedulerInterceptor<T>) new CaseDetailsInterceptor(order);
            }
            if (type == String.class) {
                return (SchedulerInterceptor<T>) new StringInterceptor(order);
            }
            throw new IllegalArgumentException("Unsupported type: " + type.getName());
        }
    }

    private static class CaseDetailsInterceptor implements SchedulerInterceptor<CaseDetails> {
        private final int order;

        public CaseDetailsInterceptor(int order) {
            this.order = order;
        }

        @Override
        public void accept(InterceptorContext<CaseDetails> context, InterceptorChain<CaseDetails> chain) {
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    private static class StringInterceptor implements SchedulerInterceptor<String> {
        private final int order;

        public StringInterceptor(int order) {
            this.order = order;
        }

        @Override
        public void accept(InterceptorContext<String> context, InterceptorChain<String> chain) {
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    private static class CaseDetailsTask implements ScheduledTask<CaseDetails, Long> {
        @Override
        public void accept(CaseDetails item) {
        }

        @Override
        public Long getItemId(CaseDetails item) {
            return item.getId();
        }
    }
}

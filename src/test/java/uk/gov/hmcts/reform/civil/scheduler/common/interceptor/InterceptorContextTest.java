package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptorContextTest {

    @Test
    void shouldStoreAndRetrieveAttributes() {
        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");
        String attributeValue = "value";
        AttributeKey<String> key = AttributeKey.of("testAttribute", String.class);

        context.setAttribute(key, attributeValue);
        Optional<String> retrievedValue = context.getAttribute(key);

        assertThat(retrievedValue).isPresent().contains(attributeValue);
    }

    @Test
    void shouldReturnEmptyOptional_whenAttributeDoesNotExist() {
        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");
        AttributeKey<String> key = AttributeKey.of("nonExistent", String.class);

        Optional<String> retrievedValue = context.getAttribute(key);

        assertThat(retrievedValue).isEmpty();
    }

    @Test
    void shouldReturnSchedulerNameAndItem() {
        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");

        assertThat(context.getSchedulerName()).isEqualTo("testScheduler");
        assertThat(context.getItem()).isEqualTo("item");
    }
}

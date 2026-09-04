package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptorContextTest {

    @Test
    void shouldStoreAndRetrieveAttributes() {
        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");
        String attributeValue = "value";

        context.setAttribute("testAttribute", attributeValue);
        Optional<String> retrievedValue = context.getAttribute("testAttribute", String.class);

        assertThat(retrievedValue).isPresent().contains(attributeValue);
    }

    @Test
    void shouldReturnEmptyOptional_whenAttributeDoesNotExist() {
        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");

        Optional<String> retrievedValue = context.getAttribute("nonExistent", String.class);

        assertThat(retrievedValue).isEmpty();
    }

    @Test
    void shouldReturnSchedulerNameAndItem() {
        InterceptorContext<String> context = new InterceptorContext<>("testScheduler", "item");

        assertThat(context.getSchedulerName()).isEqualTo("testScheduler");
        assertThat(context.getItem()).isEqualTo("item");
    }
}

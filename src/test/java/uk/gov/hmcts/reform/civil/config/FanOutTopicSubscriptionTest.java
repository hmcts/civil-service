package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FanOutTopicSubscriptionTest {

    private TopicSubscription first;
    private TopicSubscription second;
    private FanOutTopicSubscription subscription;

    @BeforeEach
    void setUp() {
        first = mock(TopicSubscription.class);
        second = mock(TopicSubscription.class);
        subscription = new FanOutTopicSubscription(List.of(first, second));
    }

    @Test
    void shouldCloseEveryDelegate() {
        subscription.close();

        verify(first).close();
        verify(second).close();
    }

    /**
     * Closing only up to the first failure would leave a topic still being served by a thread the
     * caller believes it has unsubscribed.
     */
    @Test
    void shouldCloseTheRemainingDelegatesEvenWhenOneFails() {
        doThrow(new IllegalStateException("first failed")).when(first).close();

        assertThatThrownBy(() -> subscription.close())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("first failed");

        verify(second).close();
    }

    @Test
    void shouldReportTheFirstFailureWhenSeveralDelegatesFail() {
        doThrow(new IllegalStateException("first failed")).when(first).close();
        doThrow(new IllegalStateException("second failed")).when(second).close();

        assertThatThrownBy(() -> subscription.close())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("first failed");
    }

    /**
     * Every delegate was built from the same definition, so the getters answer from the first and
     * must not consult the others.
     */
    @Test
    void shouldAnswerGettersFromTheFirstDelegateOnly() {
        ExternalTaskHandler handler = mock(ExternalTaskHandler.class);
        when(first.getTopicName()).thenReturn("processCaseEvent");
        when(first.getLockDuration()).thenReturn(1980000L);
        when(first.getExternalTaskHandler()).thenReturn(handler);
        when(first.getVariableNames()).thenReturn(List.of("a"));
        when(first.isLocalVariables()).thenReturn(true);
        when(first.getBusinessKey()).thenReturn("bk");
        when(first.getProcessDefinitionId()).thenReturn("pdi");
        when(first.getProcessDefinitionIdIn()).thenReturn(List.of("pdi1"));
        when(first.getProcessDefinitionKey()).thenReturn("pdk");
        when(first.getProcessDefinitionKeyIn()).thenReturn(List.of("pdk1"));
        when(first.getProcessDefinitionVersionTag()).thenReturn("tag");
        when(first.getProcessVariables()).thenReturn(Map.of("k", "v"));
        when(first.isWithoutTenantId()).thenReturn(true);
        when(first.getTenantIdIn()).thenReturn(List.of("t1"));
        when(first.isIncludeExtensionProperties()).thenReturn(true);

        assertThat(subscription.getTopicName()).isEqualTo("processCaseEvent");
        assertThat(subscription.getLockDuration()).isEqualTo(1980000L);
        assertThat(subscription.getExternalTaskHandler()).isSameAs(handler);
        assertThat(subscription.getVariableNames()).containsExactly("a");
        assertThat(subscription.isLocalVariables()).isTrue();
        assertThat(subscription.getBusinessKey()).isEqualTo("bk");
        assertThat(subscription.getProcessDefinitionId()).isEqualTo("pdi");
        assertThat(subscription.getProcessDefinitionIdIn()).containsExactly("pdi1");
        assertThat(subscription.getProcessDefinitionKey()).isEqualTo("pdk");
        assertThat(subscription.getProcessDefinitionKeyIn()).containsExactly("pdk1");
        assertThat(subscription.getProcessDefinitionVersionTag()).isEqualTo("tag");
        assertThat(subscription.getProcessVariables()).containsEntry("k", "v");
        assertThat(subscription.isWithoutTenantId()).isTrue();
        assertThat(subscription.getTenantIdIn()).containsExactly("t1");
        assertThat(subscription.isIncludeExtensionProperties()).isTrue();

        verifyNoInteractions(second);
    }

    @Test
    void shouldRejectNoDelegates() {
        assertThatThrownBy(() -> new FanOutTopicSubscription(List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least one delegate");
    }
}

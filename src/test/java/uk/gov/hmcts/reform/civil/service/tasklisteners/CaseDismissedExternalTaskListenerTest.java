package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.tasks.ClaimDismissedHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseDismissedExternalTaskListenerTest {

    private ExternalTaskClient client;
    private TopicSubscriptionBuilder subscriptionBuilder;
    private ClaimDismissedHandler handler;

    @BeforeEach
    void setUp() {
        client = mock(ExternalTaskClient.class);
        subscriptionBuilder = mock(TopicSubscriptionBuilder.class);
        handler = mock(ClaimDismissedHandler.class);
        TopicSubscription topicSubscription = mock(TopicSubscription.class);

        when(client.subscribe("CLAIM_DISMISSED_DEADLINE")).thenReturn(subscriptionBuilder);
        when(subscriptionBuilder.handler(handler)).thenReturn(subscriptionBuilder);
        when(subscriptionBuilder.open()).thenReturn(topicSubscription);
    }

    @Test
    void shouldSubscribeToClaimDismissedDeadlineTopicOnCreation() {
        new CaseDismissedExternalTaskListener(handler, client);

        verify(client).subscribe("CLAIM_DISMISSED_DEADLINE");
        verify(subscriptionBuilder).handler(handler);
        verify(subscriptionBuilder).open();
    }
}

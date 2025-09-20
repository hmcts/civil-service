package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.tasks.IncidentRetryEventHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncidentRetryTaskListenerTest {

    private ExternalTaskClient client;
    private TopicSubscriptionBuilder subscriptionBuilder;
    private IncidentRetryEventHandler eventHandler;

    @BeforeEach
    void setUp() {
        client = mock(ExternalTaskClient.class);
        subscriptionBuilder = mock(TopicSubscriptionBuilder.class);
        eventHandler = mock(IncidentRetryEventHandler.class);
        TopicSubscription topicSubscription = mock(TopicSubscription.class);

        // Mock the fluent API
        when(client.subscribe("INCIDENT_RETRY_EVENT")).thenReturn(subscriptionBuilder);
        when(subscriptionBuilder.handler(eventHandler)).thenReturn(subscriptionBuilder);
        when(subscriptionBuilder.open()).thenReturn(topicSubscription);
    }

    @Test
    void shouldSubscribeToIncidentRetryTopicOnCreation() {
        // Act
        new IncidentRetryTaskListener(eventHandler, client);

        // Assert
        verify(client).subscribe("INCIDENT_RETRY_EVENT");
        verify(subscriptionBuilder).handler(eventHandler);
        verify(subscriptionBuilder).open();
    }
}

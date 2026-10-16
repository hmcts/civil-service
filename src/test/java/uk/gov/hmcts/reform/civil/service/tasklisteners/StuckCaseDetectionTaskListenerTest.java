package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.tasks.StuckCaseDetectionEventHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StuckCaseDetectionTaskListenerTest {

    private ExternalTaskClient client;
    private TopicSubscriptionBuilder subscriptionBuilder;
    private StuckCaseDetectionEventHandler eventHandler;

    @BeforeEach
    void setUp() {
        client = mock(ExternalTaskClient.class);
        subscriptionBuilder = mock(TopicSubscriptionBuilder.class);
        eventHandler = mock(StuckCaseDetectionEventHandler.class);
        TopicSubscription topicSubscription = mock(TopicSubscription.class);

        // Mock the fluent API
        when(client.subscribe("STUCK_CASE_DETECTION_EVENT")).thenReturn(subscriptionBuilder);
        when(subscriptionBuilder.handler(eventHandler)).thenReturn(subscriptionBuilder);
        when(subscriptionBuilder.open()).thenReturn(topicSubscription);
    }

    @Test
    void shouldSubscribeToStuckCaseDetectionTopicOnCreation() {
        // Act
        new StuckCaseDetectionTaskListener(eventHandler, client);

        // Assert
        verify(client).subscribe("STUCK_CASE_DETECTION_EVENT");
        verify(subscriptionBuilder).handler(eventHandler);
        verify(subscriptionBuilder).open();
    }
}

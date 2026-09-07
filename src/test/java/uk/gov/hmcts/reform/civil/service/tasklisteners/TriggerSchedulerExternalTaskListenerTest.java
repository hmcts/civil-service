package uk.gov.hmcts.reform.civil.service.tasklisteners;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.tasks.TriggerSchedulerExternalTaskHandler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriggerSchedulerExternalTaskListenerTest {

    private static final String TOPIC = "TRIGGER_SCHEDULER";

    @Mock
    private TriggerSchedulerExternalTaskHandler handler;

    @Mock
    private ExternalTaskClient client;

    @Mock
    private TopicSubscriptionBuilder subscriptionBuilder;

    @Test
    void shouldSubscribeToTriggerSchedulerTopicOnCreation() {
        when(client.subscribe(TOPIC)).thenReturn(subscriptionBuilder);
        when(subscriptionBuilder.handler(handler)).thenReturn(subscriptionBuilder);

        new TriggerSchedulerExternalTaskListener(handler, client);

        verify(client).subscribe(TOPIC);
        verify(subscriptionBuilder).handler(handler);
        verify(subscriptionBuilder).open();
    }
}

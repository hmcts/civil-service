package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@ExtendWith(SpringExtension.class)
public class ProcessGaCaseEventTaskHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Spy
    private EventProperties eventProperties = configuredEventProperties();

    @Spy
    private ExternalTaskCompletionService externalTaskCompletionService = new ExternalTaskCompletionService();

    @InjectMocks
    private ProcessGaCaseEventTaskHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
    }

    @Test
    void shouldEmitProcessGaCaseEvent_whenInvoked() {
        handler.execute(mockTask, externalTaskService);
        verify(externalTaskService).complete(mockTask, null);
    }

    private static EventProperties configuredEventProperties() {
        EventProperties properties = new EventProperties();
        properties.setRetryCount(3);
        return properties;
    }

}

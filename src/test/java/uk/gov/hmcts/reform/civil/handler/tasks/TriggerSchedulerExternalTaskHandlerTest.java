package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerRegistry;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriggerSchedulerExternalTaskHandlerTest {

    private static final String TEST_SCHEDULER_NAME = "testScheduler";
    private static final String SCHEDULER_NAME_VARIABLE = "schedulerName";

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private SchedulerRegistry schedulerRegistry;

    @Spy
    private EventProperties eventProperties = configuredEventProperties();

    @Spy
    private ExternalTaskCompletionService externalTaskCompletionService = new ExternalTaskCompletionService();

    @InjectMocks
    private TriggerSchedulerExternalTaskHandler handler;

    private static EventProperties configuredEventProperties() {
        EventProperties properties = new EventProperties();
        properties.setRetryCount(3);
        return properties;
    }

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getVariable(SCHEDULER_NAME_VARIABLE)).thenReturn(TEST_SCHEDULER_NAME);
    }

    @Test
    void shouldTriggerScheduler_andReturnTrueVariable() {
        when(schedulerRegistry.runScheduler(TEST_SCHEDULER_NAME)).thenReturn(true);

        handler.execute(mockTask, externalTaskService);

        verify(schedulerRegistry).runScheduler(TEST_SCHEDULER_NAME);
        ArgumentCaptor<VariableMap> variableMapCaptor = ArgumentCaptor.forClass(VariableMap.class);
        verify(externalTaskService).complete(eq(mockTask), variableMapCaptor.capture());

        VariableMap variables = variableMapCaptor.getValue();
        assertThat(variables.get("schedulerFound")).isEqualTo(true);
    }

    @Test
    void shouldTriggerScheduler_andReturnFalseVariable_whenSchedulerNotFound() {
        when(schedulerRegistry.runScheduler(TEST_SCHEDULER_NAME)).thenReturn(false);

        handler.execute(mockTask, externalTaskService);

        verify(schedulerRegistry).runScheduler(TEST_SCHEDULER_NAME);
        ArgumentCaptor<VariableMap> variableMapCaptor = ArgumentCaptor.forClass(VariableMap.class);
        verify(externalTaskService).complete(eq(mockTask), variableMapCaptor.capture());

        VariableMap variables = variableMapCaptor.getValue();
        assertThat(variables.get("schedulerFound")).isEqualTo(false);
    }
}

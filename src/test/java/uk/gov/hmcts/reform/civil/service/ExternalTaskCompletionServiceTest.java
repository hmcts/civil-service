package uk.gov.hmcts.reform.civil.service;

import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.RestException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalTaskCompletionServiceTest {

    @Mock
    private BaseExternalTaskHandler handler;
    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private ExternalTaskData data;

    @InjectMocks
    private ExternalTaskCompletionService service;

    private static final String TOPIC_NAME = "topic";
    private static final String PROCESS_INSTANCE_ID = "processInstanceId";

    @BeforeEach
    void setUp() {
        when(externalTask.getTopicName()).thenReturn(TOPIC_NAME);
        when(externalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
    }

    @Nested
    class CompleteTask {

        @Test
        void shouldCompleteTask_whenNoExceptionThrown() {
            VariableMap variables = Variables.createVariables();
            when(handler.getVariableMap(data)).thenReturn(variables);

            service.completeTask(handler, externalTask, externalTaskService, data);

            verify(externalTaskService).complete(externalTask, variables);
        }

        @Test
        void shouldCompleteTask_whenVariablesAreNull() {
            when(handler.getVariableMap(data)).thenReturn(null);

            service.completeTask(handler, externalTask, externalTaskService, data);

            verify(externalTaskService).complete(externalTask, null);
        }

        @Test
        void shouldNotThrow_whenNotFoundExceptionIsThrown() {
            VariableMap variables = Variables.createVariables();
            when(handler.getVariableMap(data)).thenReturn(variables);
            doThrow(new NotFoundException("Not Found", new RestException("", "", 404))).when(externalTaskService).complete(externalTask, variables);

            service.completeTask(handler, externalTask, externalTaskService, data);

            verify(externalTaskService).complete(externalTask, variables);
        }

        @Test
        void shouldThrowCompleteTaskException_whenGenericExceptionIsThrown() {
            VariableMap variables = Variables.createVariables();
            when(handler.getVariableMap(data)).thenReturn(variables);
            RuntimeException cause = new RuntimeException("Generic error");
            doThrow(cause).when(externalTaskService).complete(externalTask, variables);

            CompleteTaskException exception = assertThrows(CompleteTaskException.class, () ->
                service.completeTask(handler, externalTask, externalTaskService, data));
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    class Recover {

        @Test
        void shouldCallHandleFailureNotRetryable_whenRecoverIsCalled() {
            CompleteTaskException exception = new CompleteTaskException(new RuntimeException("Error"));

            service.recover(exception, handler, externalTask, externalTaskService, data);

            verify(handler).handleFailureNotRetryable(externalTask, externalTaskService, exception);
        }
    }
}

package uk.gov.hmcts.reform.civil.handler.tasks;

import feign.FeignException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseExternalTaskHandlerTest {

    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private ExternalTaskCompletionService externalTaskCompletionService;

    private TestBaseExternalTaskHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestBaseExternalTaskHandler();
        handler.externalTaskCompletionService = externalTaskCompletionService;
    }

    private static class TestBaseExternalTaskHandler extends BaseExternalTaskHandler {
        private Exception exceptionToThrow;

        @Override
        protected ExternalTaskData handleTask(ExternalTask externalTask) {
            if (exceptionToThrow != null) {
                if (exceptionToThrow instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(exceptionToThrow);
            }
            return null;
        }

        public void setExceptionToThrow(Exception exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }
    }

    @Nested
    class Execute {

        @Test
        void shouldCompleteTask_whenHandleTaskSucceeds() {
            handler.execute(externalTask, externalTaskService);

            verify(externalTaskCompletionService).completeTask(eq(handler), eq(externalTask), eq(externalTaskService), any());
        }

        @Test
        void shouldSimulateFinalCompletionFailure_whenHandleTaskSucceeds() {
            // This test demonstrates how to simulate the @Recover behavior in unit tests
            doAnswer(invocation -> {
                handler.handleFailureNotRetryable(invocation.getArgument(1), invocation.getArgument(2),
                                                  new CompleteTaskException(new RuntimeException("final error")));
                return null;
            }).when(externalTaskCompletionService).completeTask(any(), any(), any(), any());

            handler.execute(externalTask, externalTaskService);

            // Verify that handleFailure was called with 0 retries (as handled by handleFailureNotRetryable)
            verify(externalTaskService).handleFailure(eq(externalTask), eq("java.lang.RuntimeException: final error"),
                                                      anyString(), eq(0), eq(1000L));
        }

        @Test
        void shouldHandleNotRetryableExceptionFromCompleteTask_whenHandleTaskSucceeds() {
            NotRetryableException exception = new NotRetryableException("complete error");
            doThrow(exception).when(externalTaskCompletionService).completeTask(any(), any(), any(), any());

            handler.execute(externalTask, externalTaskService);

            // Verify that handleFailure was called with 0 retries (as handled by handleFailureNotRetryable)
            verify(externalTaskService).handleFailure(eq(externalTask), eq("complete error"), anyString(), eq(0), eq(1000L));
        }

        @Test
        void shouldHandleBpmnError_whenBpmnErrorThrown() {
            BpmnError bpmnError = new BpmnError("errorCode", "message");
            handler.setExceptionToThrow(bpmnError);

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleBpmnError(externalTask, "errorCode");
            verify(externalTaskService, never()).complete(any(), any());
        }

        @Test
        void shouldHandleNotRetryableException_whenNotRetryableExceptionThrown() {
            NotRetryableException exception = new NotRetryableException("message");
            handler.setExceptionToThrow(exception);

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), eq("message"), anyString(), eq(0), eq(1000L));
            verify(externalTaskService, never()).complete(any(), any());
        }

        @Test
        void shouldHandleGenericException_whenExceptionThrown() {
            RuntimeException exception = new RuntimeException("generic error");
            handler.setExceptionToThrow(exception);
            when(externalTask.getRetries()).thenReturn(3);

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), eq("generic error"), anyString(), eq(2), anyLong());
            verify(externalTaskService, never()).complete(any(), any());
        }
    }

    @Nested
    class GetVariableMap {

        @Test
        void shouldReturnNull_whenDataHasVariables() {
            // Base implementation always returns null unless overridden
            VariableMap variables = Variables.createVariables().putValue("key", "value");
            ExternalTaskData data = new ExternalTaskData().setVariables(variables);

            VariableMap result = handler.getVariableMap(data);

            assertThat(result).isNull();
        }

        @Test
        void shouldReturnNull_whenDataIsNull() {
            assertThat(handler.getVariableMap(null)).isNull();
        }
    }

    @Nested
    class IsEventAlreadyProcessed {

        @Test
        void shouldReturnTrue_whenEventAlreadyProcessed() {
            String processInstanceId = "proc-id";
            String activityId = "act-id";
            BusinessProcess businessProcess = new BusinessProcess()
                .setProcessInstanceId(processInstanceId)
                .setActivityId(activityId);
            when(externalTask.getProcessInstanceId()).thenReturn(processInstanceId);
            when(externalTask.getActivityId()).thenReturn(activityId);

            assertThat(handler.isEventAlreadyProcessed(externalTask, businessProcess)).isTrue();
        }

        @Test
        void shouldReturnFalse_whenBusinessProcessIsNull() {
            assertThat(handler.isEventAlreadyProcessed(externalTask, null)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenProcessInstanceIdDiffers() {
            BusinessProcess businessProcess = new BusinessProcess()
                .setProcessInstanceId("other-id")
                .setActivityId("act-id");
            when(externalTask.getProcessInstanceId()).thenReturn("proc-id");

            assertThat(handler.isEventAlreadyProcessed(externalTask, businessProcess)).isFalse();
        }

        @Test
        void shouldReturnFalse_whenActivityIdDiffers() {
            String processInstanceId = "proc-id";
            BusinessProcess businessProcess = new BusinessProcess()
                .setProcessInstanceId(processInstanceId)
                .setActivityId("other-act");
            when(externalTask.getProcessInstanceId()).thenReturn(processInstanceId);
            when(externalTask.getActivityId()).thenReturn("act-id");

            assertThat(handler.isEventAlreadyProcessed(externalTask, businessProcess)).isFalse();
        }
    }

    @Nested
    class HandleFailure {

        @Test
        void shouldHandleFailureWithMaxAttempts_whenRetriesIsNull() {
            Exception e = new RuntimeException("error");
            when(externalTask.getRetries()).thenReturn(null);

            handler.handleFailure(externalTask, externalTaskService, e);

            // maxAttempts is 3, so remainingRetries should be 3-1 = 2
            verify(externalTaskService).handleFailure(eq(externalTask), eq("error"), anyString(), eq(2), anyLong());
        }

        @Test
        void shouldHandleFailureWithDecrementedRetries_whenRetriesIsNotNull() {
            Exception e = new RuntimeException("error");
            when(externalTask.getRetries()).thenReturn(5);

            handler.handleFailure(externalTask, externalTaskService, e);

            verify(externalTaskService).handleFailure(eq(externalTask), eq("error"), anyString(), eq(4), anyLong());
        }
    }

    @Nested
    class HandleFailureNotRetryable {

        @Test
        void shouldCallHandleFailure_withZeroRetries() {
            Exception e = new RuntimeException("error");
            handler.handleFailureNotRetryable(externalTask, externalTaskService, e);

            verify(externalTaskService).handleFailure(eq(externalTask), eq("error"), anyString(), eq(0), eq(1000L));
        }

        @Test
        void shouldReturnFeignExceptionContent_whenExceptionIsFeignException() {
            FeignException feignException = mock(FeignException.class);
            when(feignException.contentUTF8()).thenReturn("feign error body");

            handler.handleFailureNotRetryable(externalTask, externalTaskService, feignException);

            verify(externalTaskService).handleFailure(eq(externalTask), any(), eq("feign error body"), eq(0), eq(1000L));
        }
    }

    @Nested
    class CalculateEffectiveDelay {

        @Test
        void shouldReturnZero_whenTotalFoundIsSmall() {
            assertThat(handler.calculateEffectiveDelay(25, 1000, 100)).isZero();
        }

        @Test
        void shouldReturnCalculatedDelay_whenTotalFoundIsLarge() {
            assertThat(handler.calculateEffectiveDelay(50, 1000, 100)).isEqualTo(16);
        }

        @Test
        void shouldReturnDelay_whenMaxDelayIsGreater() {
            assertThat(handler.calculateEffectiveDelay(50, 10000, 100)).isEqualTo(100);
        }
    }

    @Nested
    class Throttle {

        @Test
        void shouldHandleInterruptedException() {
            Thread.currentThread().interrupt();
            handler.throttle(100);
            assertThat(Thread.interrupted()).isTrue();
        }
    }
}

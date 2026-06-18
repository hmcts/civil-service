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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseExternalTaskHandlerTest {

    private static final long LOCK_DURATION = 2000L;
    private static final int RETRY_COUNT = 3;
    private static final int BACKOFF_DELAY = 500;
    private static final int DISPATCH_DELAY = 200;

    @Mock
    private ExternalTask externalTask;

    @Mock
    private ExternalTaskService externalTaskService;

    private TestBaseExternalTaskHandler handler;

    @BeforeEach
    void setUp() {
        EventProperties eventProperties = new EventProperties();
        eventProperties.setLockDuration(LOCK_DURATION);
        eventProperties.setRetryCount(RETRY_COUNT);
        eventProperties.setBackoffDelay(BACKOFF_DELAY);
        eventProperties.setDispatchDelay(DISPATCH_DELAY);
        handler = new TestBaseExternalTaskHandler(eventProperties);
    }

    @Nested
    class Execute {

        @Test
        void shouldCompleteTask_whenHandleTaskSucceeds() {
            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).complete(eq(externalTask), any());
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
    class CompleteTask {

        @Test
        void shouldThrowCompleteTaskException_whenExternalTaskServiceFails() {
            doThrow(new RuntimeException("failed")).when(externalTaskService).complete(any(), any());

            assertThrows(CompleteTaskException.class, () ->
                handler.completeTask(externalTask, externalTaskService, null));
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
    class Recover {

        @Test
        void shouldHandleFailureNotRetryable_whenRecoverCalled() {
            String errorMessage = "msg";
            CompleteTaskException exception = new CompleteTaskException(new RuntimeException(errorMessage));
            handler.recover(exception, externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(eq(externalTask), eq("java.lang.RuntimeException: " + errorMessage), anyString(), eq(0), eq(1000L));
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
    class GetStackTrace {

        @Test
        void shouldReturnFeignExceptionContent_whenExceptionIsFeignException() {
            FeignException feignException = mock(FeignException.class);
            when(feignException.contentUTF8()).thenReturn("feign error body");

            handler.handleFailureNotRetryable(externalTask, externalTaskService, feignException);

            verify(externalTaskService).handleFailure(eq(externalTask), any(), eq("feign error body"), anyInt(), anyLong());
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

    @Nested
    class HelperMethods {

        @Test
        void shouldReturnNullVariableMapByDefault() {
            VariableMap variables = Variables.createVariables().putValue("key", "value");
            ExternalTaskData data = new ExternalTaskData().setVariables(variables);

            assertThat(handler.getVariableMap(data)).isNull();
            assertThat(handler.getVariableMap(null)).isNull();
        }

        @Test
        void shouldIdentifyAlreadyProcessedEvent() {
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
        void shouldReturnFalseWhenBusinessProcessDoesNotMatch() {
            BusinessProcess businessProcess = new BusinessProcess()
                .setProcessInstanceId("other-id")
                .setActivityId("other-act");

            when(externalTask.getProcessInstanceId()).thenReturn("proc-id");

            assertThat(handler.isEventAlreadyProcessed(externalTask, null)).isFalse();
            assertThat(handler.isEventAlreadyProcessed(externalTask, businessProcess)).isFalse();
        }

        @Test
        void shouldUseConfiguredBackoffWhenHandlingTaskFailure() {
            when(externalTask.getRetries()).thenReturn(2);

            handler.handleFailure(externalTask, externalTaskService, new RuntimeException("boom"));

            verify(externalTaskService).handleFailure(
                eq(externalTask),
                eq("boom"),
                anyString(),
                eq(1),
                eq(456L)
            );
        }

        @Test
        void totalRetriesShouldNotExceedLockDuration() {

            when(externalTask.getRetries()).thenReturn(3, 2, 1);

            handler.handleFailure(externalTask, externalTaskService, new RuntimeException("boom"));
            handler.handleFailure(externalTask, externalTaskService, new RuntimeException("boom"));
            handler.handleFailure(externalTask, externalTaskService, new RuntimeException("boom"));

            ArgumentCaptor<Long> retryTimeoutCaptor = ArgumentCaptor.forClass(Long.class);

            verify(externalTaskService, times(3)).handleFailure(
                eq(externalTask),
                eq("boom"),
                anyString(),
                anyInt(),
                retryTimeoutCaptor.capture()
            );

            long totalRetryTimeout = retryTimeoutCaptor.getAllValues()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

            assertThat(totalRetryTimeout).isLessThanOrEqualTo((long) (LOCK_DURATION * 0.8));
        }

    }

    private static class TestBaseExternalTaskHandler extends BaseExternalTaskHandler {
        private ExternalTaskData externalTaskData;
        private Exception exceptionToThrow;

        protected TestBaseExternalTaskHandler(EventProperties eventProperties) {
            super(eventProperties);
        }

        @Override
        protected ExternalTaskData handleTask(ExternalTask externalTask) {
            if (exceptionToThrow != null) {
                if (exceptionToThrow instanceof RuntimeException) {
                    throw (RuntimeException) exceptionToThrow;
                }
                throw new RuntimeException(exceptionToThrow);
            }
            return externalTaskData;
        }

        public void setExceptionToThrow(Exception exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }
    }
}

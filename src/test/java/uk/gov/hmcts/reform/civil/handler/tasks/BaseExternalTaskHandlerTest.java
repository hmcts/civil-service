package uk.gov.hmcts.reform.civil.handler.tasks;

import feign.FeignException;
import feign.Request;
import org.camunda.community.rest.exception.RemoteProcessEngineException;
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
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
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
    private ExternalTaskCompletionService externalTaskCompletionService;

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
        handler = new TestBaseExternalTaskHandler(externalTaskCompletionService, eventProperties);
    }

    @Nested
    class ExecuteHandleTaskLine {

        @Test
        void shouldPassHandleTaskResultToCompletionService_whenHandleTaskSucceeds() {
            ExternalTaskData externalTaskData = new ExternalTaskData();
            handler.returnExternalTaskData(externalTaskData);

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskCompletionService).completeTask(handler, externalTask, externalTaskService, externalTaskData);
        }

        @Test
        void shouldHandleBpmnError_whenHandleTaskThrowsBpmnError() {
            handler.throwBpmnError(new BpmnError("ABORT", "message"));

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleBpmnError(externalTask, "ABORT");
            verify(externalTaskCompletionService, never()).completeTask(any(), any(), any(), any());
        }

        @Test
        void shouldCallHandleTaskFailureNotRetryable_whenHandleTaskThrowsNotRetryableException() {
            handler.throwNotRetryable(new NotRetryableException("bad-data"));

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(
                eq(externalTask),
                eq("bad-data"),
                anyString(),
                eq(0),
                eq(1000L)
            );
            verify(externalTaskCompletionService, never()).completeTask(any(), any(), any(), any());
        }

        @Test
        void shouldCallHandleTaskFailure_whenHandleTaskThrowsRetryableException() {
            when(externalTask.getRetries()).thenReturn(null);
            handler.throwRetryable(new RuntimeException("boom"));

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(
                eq(externalTask),
                eq("boom"),
                anyString(),
                eq(2),
                eq(228L)
            );
            verify(externalTaskCompletionService, never()).completeTask(any(), any(), any(), any());
        }

        @Test
        void shouldCallHandleTaskFailureNotRetryable_whenHandleTaskThrowsRemoteProcessEngineClientError() {
            handler.throwRetryable(new RemoteProcessEngineException(
                "REST-CLIENT-001 Error during remote Camunda engine invocation of DocmosisApiClient#createDocument(DocmosisRequest): Bad Request",
                new FeignException.BadRequest(
                    "Bad request",
                    Request.create(Request.HttpMethod.GET, "url", java.util.Map.of(), null, null, null),
                    null,
                    null
                )
            ));

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(
                eq(externalTask),
                contains("Bad Request"),
                anyString(),
                eq(0),
                eq(1000L)
            );
            verify(externalTaskCompletionService, never()).completeTask(any(), any(), any(), any());
        }

        @Test
        void shouldCallHandleTaskFailure_whenHandleTaskThrowsRemoteProcessEngineServerError() {
            when(externalTask.getRetries()).thenReturn(null);
            handler.throwRetryable(new RemoteProcessEngineException(
                "REST-CLIENT-001 Error during remote Camunda engine invocation: Bad Gateway",
                new FeignException.BadGateway(
                    "Bad gateway",
                    Request.create(Request.HttpMethod.GET, "url", java.util.Map.of(), null, null, null),
                    null,
                    null
                )
            ));

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(
                eq(externalTask),
                contains("Bad Gateway"),
                anyString(),
                eq(2),
                eq(228L)
            );
            verify(externalTaskCompletionService, never()).completeTask(any(), any(), any(), any());
        }
    }

    @Nested
    class ExecuteCompleteTaskLine {

        @Test
        void shouldCallHandleTaskFailureNotRetryable_whenCompleteTaskThrowsNotRetryableException() {
            doThrow(new NotRetryableException("complete error"))
                .when(externalTaskCompletionService).completeTask(any(), any(), any(), any());

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(
                eq(externalTask),
                eq("complete error"),
                anyString(),
                eq(0),
                eq(1000L)
            );
        }

        @Test
        void shouldCallHandleTaskFailure_whenCompleteTaskThrowsRetryableException() {
            when(externalTask.getRetries()).thenReturn(null);
            doThrow(new CompleteTaskException(new RuntimeException("complete boom")))
                .when(externalTaskCompletionService).completeTask(any(), any(), any(), any());

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(
                eq(externalTask),
                contains("complete boom"),
                anyString(),
                eq(2),
                eq(228L)
            );
        }

        @Test
        void shouldAllowCompletionServiceRecoveryToUseHandleTaskFailureNotRetryable() {
            doAnswer(invocation -> {
                handler.handleTaskFailureNotRetryable(
                    invocation.getArgument(1),
                    invocation.getArgument(2),
                    new CompleteTaskException(new RuntimeException("final error"))
                );
                return null;
            }).when(externalTaskCompletionService).completeTask(any(), any(), any(), any());

            handler.execute(externalTask, externalTaskService);

            verify(externalTaskService).handleFailure(
                eq(externalTask),
                contains("final error"),
                anyString(),
                eq(0),
                eq(1000L)
            );
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

            handler.handleTaskFailure(externalTask, externalTaskService, new RuntimeException("boom"));

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

            handler.handleTaskFailure(externalTask, externalTaskService, new RuntimeException("boom"));
            handler.handleTaskFailure(externalTask, externalTaskService, new RuntimeException("boom"));
            handler.handleTaskFailure(externalTask, externalTaskService, new RuntimeException("boom"));

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

        @Test
        void shouldReturnFeignBodyWhenHandlingNotRetryableFailure() {
            FeignException feignException = mock(FeignException.class);
            when(feignException.contentUTF8()).thenReturn("feign error body");

            handler.handleTaskFailureNotRetryable(externalTask, externalTaskService, feignException);

            verify(externalTaskService).handleFailure(
                eq(externalTask),
                isNull(),
                eq("feign error body"),
                eq(0),
                eq(1000L)
            );
        }
    }

    private static final class TestBaseExternalTaskHandler extends BaseExternalTaskHandler {
        private ExternalTaskData nextExternalTaskData = new ExternalTaskData();
        private RuntimeException retryableException;
        private NotRetryableException notRetryableException;
        private BpmnError bpmnError;

        private TestBaseExternalTaskHandler(
            ExternalTaskCompletionService externalTaskCompletionService,
            EventProperties eventProperties
        ) {
            super(externalTaskCompletionService, eventProperties);
        }

        private void returnExternalTaskData(ExternalTaskData externalTaskData) {
            this.nextExternalTaskData = externalTaskData;
        }

        private void throwRetryable(RuntimeException exception) {
            this.retryableException = exception;
        }

        private void throwNotRetryable(NotRetryableException exception) {
            this.notRetryableException = exception;
        }

        private void throwBpmnError(BpmnError error) {
            this.bpmnError = error;
        }

        @Override
        protected ExternalTaskData handleTask(ExternalTask externalTask) {
            if (bpmnError != null) {
                throw bpmnError;
            }
            if (notRetryableException != null) {
                throw notRetryableException;
            }
            if (retryableException != null) {
                throw retryableException;
            }
            return nextExternalTaskData;
        }
    }
}

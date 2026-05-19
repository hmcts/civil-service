package uk.gov.hmcts.reform.civil.service;

import org.camunda.bpm.client.exception.BadRequestException;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.RestException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(
    classes = ExternalTaskCompletionServiceIT.TestConfiguration.class,
    properties = {
        "external-task-completion.retry.max-attempts=3",
        "external-task-completion.retry.delay=1",
        "external-task-completion.retry.multiplier=1"
    }
)
@SuppressWarnings({"java:S5960", "java:S6813"})
class ExternalTaskCompletionServiceIT {

    private static final String TOPIC = "topic";
    private static final String PROCESS_INSTANCE_ID = "process-instance-id";

    @Configuration
    @EnableRetry
    static class TestConfiguration {
        @Bean
        ExternalTaskCompletionService externalTaskCompletionService() {
            return new ExternalTaskCompletionService();
        }

        @Bean
        EventProperties eventProperties() {
            EventProperties properties = new EventProperties();
            properties.setRetryCount(3);
            return properties;
        }

        @Bean
        TestExternalTaskHandler testExternalTaskHandler(
            ExternalTaskCompletionService externalTaskCompletionService,
            EventProperties eventProperties
        ) {
            return new TestExternalTaskHandler(externalTaskCompletionService, eventProperties);
        }
    }

    @Autowired
    private ExternalTaskCompletionService externalTaskCompletionService;

    @Autowired
    private TestExternalTaskHandler testExternalTaskHandler;

    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;

    @BeforeEach
    void setUp() {
        externalTask = mock(ExternalTask.class);
        externalTaskService = mock(ExternalTaskService.class);
        when(externalTask.getTopicName()).thenReturn(TOPIC);
        when(externalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        testExternalTaskHandler.reset();
    }

    @Nested
    class HandleTaskCases {

        @Test
        void shouldCallHandleTaskFailureWhenHandleTaskThrowsRetryableException() {
            when(externalTask.getRetries()).thenReturn(null);
            testExternalTaskHandler.throwRetryable(new RuntimeException("boom"));

            assertThatCode(() -> testExternalTaskHandler.execute(externalTask, externalTaskService))
                .doesNotThrowAnyException();

            verify(externalTaskService, never()).complete(externalTask, null);
            verify(externalTaskService).handleFailure(
                eq(externalTask),
                eq("boom"),
                anyString(),
                eq(2),
                eq(0L)
            );
        }

        @Test
        void shouldCallHandleTaskFailureNotRetryableWhenHandleTaskThrowsNotRetryableException() {
            testExternalTaskHandler.throwNotRetryable(new NotRetryableException("bad-data"));

            assertThatCode(() -> testExternalTaskHandler.execute(externalTask, externalTaskService))
                .doesNotThrowAnyException();

            verify(externalTaskService, never()).complete(externalTask, null);
            verify(externalTaskService).handleFailure(
                eq(externalTask),
                eq("bad-data"),
                anyString(),
                eq(0),
                eq(1000L)
            );
        }

        @Test
        void shouldHandleBpmnErrorWhenHandleTaskThrowsBpmnError() {
            testExternalTaskHandler.throwBpmnError(new BpmnError("ABORT"));

            assertThatCode(() -> testExternalTaskHandler.execute(externalTask, externalTaskService))
                .doesNotThrowAnyException();

            verify(externalTaskService).handleBpmnError(externalTask, "ABORT");
            verify(externalTaskService, never()).complete(externalTask, null);
            verify(externalTaskService, never()).handleFailure(
                eq(externalTask),
                anyString(),
                anyString(),
                eq(0),
                eq(1000L)
            );
        }
    }

    @Nested
    class CompleteTaskCases {

        @Test
        void shouldRetryCompletionOnTransientFailureAndEventuallySucceed() {
            when(externalTask.getRetries()).thenReturn(null);
            doThrow(new RuntimeException("boom-1"))
                .doThrow(new RuntimeException("boom-2"))
                .doNothing()
                .when(externalTaskService).complete(externalTask, null);

            assertThat(AopUtils.isAopProxy(externalTaskCompletionService)).isTrue();

            assertThatCode(() -> testExternalTaskHandler.execute(externalTask, externalTaskService))
                .doesNotThrowAnyException();

            verify(externalTaskService, times(3)).complete(externalTask, null);
            verify(externalTaskService, never()).handleFailure(
                eq(externalTask),
                anyString(),
                anyString(),
                eq(0),
                eq(1000L)
            );
        }

        @Test
        void shouldRecoverAfterRetriesExhaustedWithoutTriggeringGenericHandlerFailure() {
            when(externalTask.getRetries()).thenReturn(null);
            doThrow(new RuntimeException("boom"))
                .when(externalTaskService).complete(externalTask, null);

            assertThatCode(() -> testExternalTaskHandler.execute(externalTask, externalTaskService))
                .doesNotThrowAnyException();

            verify(externalTaskService, times(3)).complete(externalTask, null);
            verify(externalTaskService).handleFailure(
                eq(externalTask),
                contains("boom"),
                anyString(),
                eq(0),
                eq(1000L)
            );
            verify(externalTaskService, never()).handleFailure(
                eq(externalTask),
                anyString(),
                anyString(),
                eq(2),
                eq(0L)
            );
        }

        @Test
        void shouldIgnoreNotFoundExceptionWithoutRetrying() {
            doThrow(new NotFoundException("Not Found", new RestException("", "", 404)))
                .when(externalTaskService).complete(externalTask, null);

            assertThatCode(() -> testExternalTaskHandler.execute(externalTask, externalTaskService))
                .doesNotThrowAnyException();

            verify(externalTaskService).complete(externalTask, null);
            verify(externalTaskService, never()).handleFailure(
                eq(externalTask),
                anyString(),
                anyString(),
                eq(0),
                eq(1000L)
            );
        }

        @Test
        void shouldCallHandleTaskFailureNotRetryableWhenCompleteTaskThrowsNotRetryableException() {
            doThrow(new BadRequestException("Bad Request", new RestException("", "", 400)))
                .when(externalTaskService).complete(externalTask, null);

            assertThatCode(() -> testExternalTaskHandler.execute(externalTask, externalTaskService))
                .doesNotThrowAnyException();

            verify(externalTaskService).complete(externalTask, null);
            verify(externalTaskService).handleFailure(
                eq(externalTask),
                eq("Bad Request"),
                anyString(),
                eq(0),
                eq(1000L)
            );
            verify(externalTaskService, never()).handleFailure(
                eq(externalTask),
                anyString(),
                anyString(),
                eq(2),
                eq(0L)
            );
        }
    }

    static class TestExternalTaskHandler extends BaseExternalTaskHandler {
        private ExternalTaskData nextExternalTaskData;
        private RuntimeException retryableException;
        private NotRetryableException notRetryableException;
        private BpmnError bpmnError;

        TestExternalTaskHandler(
            ExternalTaskCompletionService externalTaskCompletionService,
            EventProperties eventProperties
        ) {
            super(externalTaskCompletionService, eventProperties);
            reset();
        }

        void reset() {
            nextExternalTaskData = new ExternalTaskData();
            retryableException = null;
            notRetryableException = null;
            bpmnError = null;
        }

        void throwRetryable(RuntimeException exception) {
            this.retryableException = exception;
        }

        void throwNotRetryable(NotRetryableException exception) {
            this.notRetryableException = exception;
        }

        void throwBpmnError(BpmnError error) {
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

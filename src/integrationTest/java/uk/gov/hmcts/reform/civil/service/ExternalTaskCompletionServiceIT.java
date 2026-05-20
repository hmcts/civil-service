package uk.gov.hmcts.reform.civil.service;

import org.camunda.bpm.client.exception.BadRequestException;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.RestException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

    public static final String TOPIC = "topic";
    public static final String PROCESS_INSTANCE_ID = "process-instance-id";

    @Configuration
    @EnableRetry
    static class TestConfiguration {
        @Bean
        ExternalTaskCompletionService externalTaskCompletionService() {
            return new ExternalTaskCompletionService();
        }

        @Bean
        TestExternalTaskHandler testExternalTaskHandler() {
            return new TestExternalTaskHandler();
        }
    }

    private BaseExternalTaskHandler handler;
    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;
    private ExternalTaskData data;

    @Autowired
    private ExternalTaskCompletionService service;

    @Autowired
    private TestExternalTaskHandler testExternalTaskHandler;

    @BeforeEach
    void setUp() {
        handler = mock(BaseExternalTaskHandler.class);
        externalTask = mock(ExternalTask.class);
        externalTaskService = mock(ExternalTaskService.class);
        data = mock(ExternalTaskData.class);
    }

    @Test
    void shouldRetryCompletionOnTransientFailureAndEventuallySucceed() {
        when(externalTask.getTopicName()).thenReturn(TOPIC);
        when(externalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        when(handler.getVariableMap(data)).thenReturn(null);

        doThrow(new RuntimeException("boom-1"))
            .doThrow(new RuntimeException("boom-2"))
            .doNothing()
            .when(externalTaskService).complete(externalTask, null);

        service.completeTask(handler, externalTask, externalTaskService, data);

        assertThat(AopUtils.isAopProxy(service)).isTrue();
        verify(externalTaskService, times(3)).complete(externalTask, null);
        verify(handler, never()).handleFailureNotRetryable(any(), any(), any());
    }

    @Test
    void shouldRecoverAfterMaxAttemptsAreExhausted() {
        when(externalTask.getTopicName()).thenReturn(TOPIC);
        when(externalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        when(handler.getVariableMap(data)).thenReturn(null);

        doThrow(new RuntimeException("boom"))
            .when(externalTaskService).complete(externalTask, null);

        service.completeTask(handler, externalTask, externalTaskService, data);

        verify(externalTaskService, times(3)).complete(externalTask, null);
        verify(handler).handleFailureNotRetryable(eq(externalTask), eq(externalTaskService), any(CompleteTaskException.class));
    }

    @Test
    void shouldIgnoreNotFoundExceptionWithoutRetrying() {
        when(externalTask.getTopicName()).thenReturn(TOPIC);
        when(externalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        when(handler.getVariableMap(data)).thenReturn(null);

        doThrow(new NotFoundException("Not Found", new RestException("", "", 404)))
            .when(externalTaskService).complete(externalTask, null);

        assertThatCode(() -> service.completeTask(handler, externalTask, externalTaskService, data))
            .doesNotThrowAnyException();

        verify(externalTaskService).complete(externalTask, null);
        verify(handler, never()).handleFailureNotRetryable(any(), any(), any());
    }

    @Test
    void shouldThrowNotRetryableExceptionForBadRequestWithoutRetrying() {
        when(externalTask.getTopicName()).thenReturn(TOPIC);
        when(externalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        when(handler.getVariableMap(data)).thenReturn(null);

        doThrow(new BadRequestException("Bad Request", new RestException("", "", 400)))
            .when(externalTaskService).complete(externalTask, null);

        assertThatThrownBy(() -> service.completeTask(handler, externalTask, externalTaskService, data))
            .isInstanceOf(NotRetryableException.class)
            .hasMessage("Bad Request");

        verify(externalTaskService).complete(externalTask, null);
        verify(handler, never()).handleFailureNotRetryable(any(), any(), any());
    }

    @Test
    void shouldRecoverAfterRetriesExhaustedWithoutTriggeringGenericHandlerFailure() {
        when(externalTask.getTopicName()).thenReturn(TOPIC);
        when(externalTask.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
        when(externalTask.getRetries()).thenReturn(null);

        doThrow(new RuntimeException("boom"))
            .when(externalTaskService).complete(externalTask, null);

        assertThatCode(() -> testExternalTaskHandler.execute(externalTask, externalTaskService))
            .doesNotThrowAnyException();

        verify(externalTaskService, times(3)).complete(externalTask, null);
        // Failure without retry
        verify(externalTaskService).handleFailure(
            eq(externalTask),
            contains("boom"),
            anyString(),
            eq(0),
            eq(1000L)
        );
        // Failure with retry - generic
        verify(externalTaskService, never()).handleFailure(
            eq(externalTask),
            anyString(),
            anyString(),
            eq(2),
            anyLong()
        );
    }

    static class TestExternalTaskHandler extends BaseExternalTaskHandler {
        @Override
        protected ExternalTaskData handleTask(ExternalTask externalTask) {
            return null;
        }
    }
}

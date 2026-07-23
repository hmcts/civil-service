package uk.gov.hmcts.reform.civil.handler.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerRegistry;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    private static EventProperties configuredEventProperties() {
        EventProperties properties = new EventProperties();
        properties.setRetryCount(3);
        return properties;
    }

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getVariable(SCHEDULER_NAME_VARIABLE)).thenReturn(TEST_SCHEDULER_NAME);

        logger = (Logger) LoggerFactory.getLogger(TriggerSchedulerExternalTaskHandler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void shouldReturn_whenSchedulerFound() {
        when(schedulerRegistry.runScheduler(TEST_SCHEDULER_NAME)).thenReturn(true);

        handler.execute(mockTask, externalTaskService);

        verify(schedulerRegistry).runScheduler(TEST_SCHEDULER_NAME);
        verify(externalTaskService).complete(mockTask, null);

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void shouldLogError_whenSchedulerNotFound() {
        when(schedulerRegistry.runScheduler(TEST_SCHEDULER_NAME)).thenReturn(false);

        handler.execute(mockTask, externalTaskService);

        verify(schedulerRegistry).runScheduler(TEST_SCHEDULER_NAME);
        verify(externalTaskService).complete(mockTask, null);

        List<ILoggingEvent> logsList = listAppender.list;

        assertThat(logsList).hasSize(1);
        assertThat(logsList.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(logsList.getFirst().getFormattedMessage())
            .isEqualTo("Trigger scheduler failed: scheduler not found for name 'testScheduler'");
    }

    @Test
    void shouldLogError_whenSchedulerNameVariableIsNull() {
        when(mockTask.getVariable(SCHEDULER_NAME_VARIABLE)).thenReturn(null);

        handler.execute(mockTask, externalTaskService);

        verify(schedulerRegistry, never()).runScheduler(anyString());
        verify(externalTaskService).complete(mockTask, null);

        List<ILoggingEvent> logsList = listAppender.list;

        assertThat(logsList).hasSize(1);
        assertThat(logsList.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(logsList.getFirst().getFormattedMessage())
            .isEqualTo("Trigger scheduler failed: 'schedulerName' variable not set");
    }
}

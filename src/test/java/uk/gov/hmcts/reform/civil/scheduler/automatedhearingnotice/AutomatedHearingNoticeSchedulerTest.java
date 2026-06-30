package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutomatedHearingNoticeSchedulerTest {

    private static final String SCHEDULER_NAME = "AutomatedHearingNotice";
    private static final String USERNAME = "system-user";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";
    private static final String HEARING_ID = "hearing-id-1";

    @Mock
    private UserService userService;

    @Mock
    private HearingsService hearingsService;

    @Mock
    private AutomatedHearingNoticeScheduledTask scheduledTask;

    @Mock
    private ScheduledEventTracker eventTracker;

    @Mock
    private FeatureToggleService featureToggleService;

    private AutomatedHearingNoticeScheduler scheduler;
    private ScheduledTaskEventConfiguration eventConfiguration;

    @BeforeEach
    void setUp() {
        scheduler = new AutomatedHearingNoticeScheduler(
            userService,
            new SystemUpdateUserConfiguration(USERNAME, PASSWORD),
            hearingsService,
            scheduledTask,
            eventTracker,
            featureToggleService
        );
        ReflectionTestUtils.setField(scheduler, "serviceIds", List.of(SPEC_SERVICE_ID, UNSPEC_SERVICE_ID));
        ReflectionTestUtils.setField(scheduler, "circuitBreakerThreshold", 2);
        eventConfiguration = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
    }

    @Test
    void shouldProcessUnnotifiedHearingsForSpecAndUnspecServices() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(userService.getAccessToken(USERNAME, PASSWORD)).thenReturn(ACCESS_TOKEN);
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(SPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of("spec-hearing-id"), 1L));
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(UNSPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of("unspec-hearing-id"), 1L));

        scheduler.runScheduledTask();

        verify(eventTracker).jobStartedEvent(eventConfiguration, 2);
        verify(scheduledTask).accept("spec-hearing-id");
        verify(scheduledTask).accept("unspec-hearing-id");
        verify(eventTracker).caseProcessedEvent(eventConfiguration, "spec-hearing-id");
        verify(eventTracker).caseProcessedEvent(eventConfiguration, "unspec-hearing-id");
        verify(eventTracker).jobCompletedEvent(eventConfiguration, 2, 2, 0);
    }

    @Test
    void shouldTrackCompletedNoCasesWhenNoUnnotifiedHearingsAreFound() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(userService.getAccessToken(USERNAME, PASSWORD)).thenReturn(ACCESS_TOKEN);
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(SPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of(), 0L));
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(UNSPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of(), 0L));

        scheduler.runScheduledTask();

        verify(eventTracker).jobStartedEvent(eventConfiguration, 0);
        verify(eventTracker).jobCompletedNoCasesEvent(eventConfiguration);
        verifyNoInteractions(scheduledTask);
    }

    @Test
    void shouldNotRunWhenSpringSchedulerFeatureToggleIsDisabled() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(false);

        scheduler.runScheduledTask();

        verifyNoInteractions(userService, hearingsService, scheduledTask, eventTracker);
    }

    @Test
    void shouldTrackFailureAndContinueProcessingFollowingHearing() {
        RuntimeException exception = new RuntimeException("failed");
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(userService.getAccessToken(USERNAME, PASSWORD)).thenReturn(ACCESS_TOKEN);
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(SPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of(HEARING_ID, "hearing-id-2"), 2L));
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(UNSPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of(), 0L));
        doThrow(exception).when(scheduledTask).accept(HEARING_ID);

        scheduler.runScheduledTask();

        verify(eventTracker).caseFailedEvent(eventConfiguration, HEARING_ID, exception);
        verify(scheduledTask).accept("hearing-id-2");
        verify(eventTracker).caseProcessedEvent(eventConfiguration, "hearing-id-2");
        verify(eventTracker).jobCompletedEvent(eventConfiguration, 2, 1, 1);
    }

    @Test
    void shouldAbortAfterConsecutiveFailuresReachCircuitBreakerThreshold() {
        RuntimeException firstException = new RuntimeException("first failed");
        RuntimeException secondException = new RuntimeException("second failed");
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(userService.getAccessToken(USERNAME, PASSWORD)).thenReturn(ACCESS_TOKEN);
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(SPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of("hearing-id-1", "hearing-id-2", "hearing-id-3"), 3L));
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(UNSPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of(), 0L));
        doThrow(firstException).when(scheduledTask).accept("hearing-id-1");
        doThrow(secondException).when(scheduledTask).accept("hearing-id-2");

        scheduler.runScheduledTask();

        verify(eventTracker).caseFailedEvent(eventConfiguration, "hearing-id-1", firstException);
        verify(eventTracker).caseFailedEvent(eventConfiguration, "hearing-id-2", secondException);
        verify(eventTracker).jobAbortedEvent(eventConfiguration, 3, 0, 2, "second failed");
        verify(scheduledTask, never()).accept("hearing-id-3");
    }
}

package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice.AutomatedHearingNoticeScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice.AutomatedHearingNoticeScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=AutomatedHearingNoticeSchedulerIT",
    "scheduler.automatedHearingNotice.enabled=true"
})
public class AutomatedHearingNoticeSchedulerIT {

    private static final String SCHEDULER_NAME = "AutomatedHearingNotice";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";
    private static final String HEARING_ID = "hearing-id-1";

    @Autowired
    private AutomatedHearingNoticeScheduler scheduler;

    @MockBean
    private HearingsService hearingsService;

    @MockBean
    private AutomatedHearingNoticeScheduledTask automatedHearingNoticeScheduledTask;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean(name = "userService")
    private UserService userService;

    @Test
    void shouldExecuteAutomatedHearingNoticeScheduler() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(userService.getAccessToken(anyString(), anyString())).thenReturn(ACCESS_TOKEN);
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(SPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of(HEARING_ID), 1L));
        when(hearingsService.getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(UNSPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        )).thenReturn(new UnNotifiedHearingResponse(List.of(), 0L));

        scheduler.runScheduledTask();

        verify(hearingsService).getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(SPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        );
        verify(hearingsService).getUnNotifiedHearingResponses(
            eq(ACCESS_TOKEN), eq(UNSPEC_SERVICE_ID), any(LocalDateTime.class), isNull()
        );
        verify(automatedHearingNoticeScheduledTask).accept(HEARING_ID, 1L);
        verify(telemetryService).trackEvent(eq("AutomatedHearingNoticeJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("AutomatedHearingNoticeCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("AutomatedHearingNoticeJobCompleted"), anyMap());
    }
}

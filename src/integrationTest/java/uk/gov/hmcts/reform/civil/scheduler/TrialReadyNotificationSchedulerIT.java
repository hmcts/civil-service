package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.trialreadynotification.TrialReadyNotificationScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.trialreadynotification.TrialReadyNotificationScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=TrialReadyNotificationSchedulerIT",
    "scheduler.trial-ready-notification.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class TrialReadyNotificationSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "TrialReadyNotification";

    @Autowired
    private TrialReadyNotificationScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private TrialReadyNotificationScheduledTask trialReadyNotificationScheduledTask;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(trialReadyNotificationScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(trialReadyNotificationScheduledTask.getItemId(any(CaseDetails.class))).thenAnswer(invocation ->
            invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteTrialReadyNotificationScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(searchCase))
            .build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);

        scheduler.runScheduledTask();

        verify(trialReadyNotificationScheduledTask).accept(searchCase);
        verify(telemetryService).trackEvent(eq("TrialReadyNotificationJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("TrialReadyNotificationCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("TrialReadyNotificationJobCompleted"), anyMap());
    }
}

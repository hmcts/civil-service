package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.trialreadycheck.TrialReadyCheckScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.trialreadycheck.TrialReadyCheckScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=TrialReadyCheckSchedulerIT",
    "scheduler.lockAtLeastFor=PT0S"
})
public class TrialReadyCheckSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "TrialReadyCheck";

    @Autowired
    private TrialReadyCheckScheduler scheduler;

    @MockitoBean
    private TelemetryService telemetryService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private TrialReadyCheckScheduledTask trialReadyCheckScheduledTask;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService, trialReadyCheckScheduledTask);
        coreCaseDataApiMockHelper.resetMocks();
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(trialReadyCheckScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(trialReadyCheckScheduledTask.getItemId(any(CaseDetails.class))).thenAnswer(invocation ->
            invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteTrialReadyCheckScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(searchCase))
            .build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);

        scheduler.runScheduledTask();

        verify(trialReadyCheckScheduledTask).accept(searchCase);
        verify(telemetryService).trackEvent(eq("SchedulerJobStarted"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
        verify(telemetryService).trackEvent(eq("SchedulerCaseProcessed"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
        verify(telemetryService).trackEvent(eq("SchedulerJobCompleted"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
    }
}

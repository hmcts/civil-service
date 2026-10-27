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
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter.PollingEventEmitterScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter.PollingEventEmitterScheduler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=PollingEventEmitterSchedulerIT",
    "scheduler.lockAtLeastFor=PT0S"
})
public class PollingEventEmitterSchedulerIT {

    private static final long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "PollingEventEmitter";

    @Autowired
    private PollingEventEmitterScheduler scheduler;

    @MockitoBean
    private CoreCaseDataService coreCaseDataService;

    @MockitoBean
    private PollingEventEmitterScheduledTask pollingEventEmitterScheduledTask;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private TelemetryService telemetryService;

    @BeforeEach
    void setUp() {
        reset(coreCaseDataService, pollingEventEmitterScheduledTask, featureToggleService, telemetryService);
    }

    @Test
    void shouldExecutePollingEventEmitterScheduler() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataService.searchCases(any(Query.class))).thenReturn(searchResult);
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(pollingEventEmitterScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(pollingEventEmitterScheduledTask.getItemId(caseDetails)).thenReturn(CASE_ID);

        scheduler.runScheduledTask();

        verify(coreCaseDataService).searchCases(any(Query.class));
        verify(pollingEventEmitterScheduledTask).accept(caseDetails);
        verify(telemetryService).trackEvent(eq("SchedulerJobStarted"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
        verify(telemetryService).trackEvent(eq("SchedulerCaseProcessed"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
        verify(telemetryService).trackEvent(eq("SchedulerJobCompleted"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
    }
}

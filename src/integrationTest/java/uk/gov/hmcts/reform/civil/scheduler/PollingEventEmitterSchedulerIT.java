package uk.gov.hmcts.reform.civil.scheduler;

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
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter.PollingEventEmitterScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter.PollingEventEmitterScheduler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=PollingEventEmitterSchedulerIT",
    "scheduler.polling-event-emitter.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class PollingEventEmitterSchedulerIT {

    private static final long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "PollingEventEmitter";

    @Autowired
    private PollingEventEmitterScheduler scheduler;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private PollingEventEmitterScheduledTask pollingEventEmitterScheduledTask;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private TelemetryService telemetryService;

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
        verify(telemetryService).trackEvent(eq("PollingEventEmitterJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("PollingEventEmitterCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("PollingEventEmitterJobCompleted"), anyMap());
    }
}

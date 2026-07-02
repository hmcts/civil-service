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
import uk.gov.hmcts.reform.civil.scheduler.takecaseoffline.TakeCaseOfflineScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.takecaseoffline.TakeCaseOfflineScheduler;
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
    "test.id=TakeCaseOfflineSchedulerIT",
    "scheduler.take-case-offline.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class TakeCaseOfflineSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "TakeCaseOffline";

    @Autowired
    private TakeCaseOfflineScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private TakeCaseOfflineScheduledTask takeCaseOfflineScheduledTask;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(takeCaseOfflineScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(takeCaseOfflineScheduledTask.getItemId(any(CaseDetails.class))).thenAnswer(invocation ->
            invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteTakeCaseOfflineScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(searchCase))
            .build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);

        scheduler.runScheduledTask();

        verify(takeCaseOfflineScheduledTask).accept(searchCase);
        verify(telemetryService).trackEvent(eq("TakeCaseOfflineJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("TakeCaseOfflineCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("TakeCaseOfflineJobCompleted"), anyMap());
    }
}

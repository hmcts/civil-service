package uk.gov.hmcts.reform.civil.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.settlementnoresponsefromdefchk.SettlementNoResponseFromDefendantCheckScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.settlementnoresponsefromdefchk.SettlementNoResponseFromDefendantCheckScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("integration-test")
@SpringBootTest(
    classes = {
        Application.class,
        TestIdamConfiguration.class,
        CoreCaseDataApiMockHelperConfiguration.class
    },
    properties = {
        "test.id=SettlementNoResponseFromDefendantCheckSchedulerIT",
        "scheduler.settlement-no-response-from-defendant-check.enabled=true",
        "scheduler.lockAtLeastFor=PT0S"
    }
)
class SettlementNoResponseFromDefendantCheckSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "SettlementNoResponseFromDefendantCheck";

    @Autowired
    private SettlementNoResponseFromDefendantCheckScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private SettlementNoResponseFromDefendantCheckScheduledTask scheduledTask;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService);
        coreCaseDataApiMockHelper.resetMocks();
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(scheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(scheduledTask.getItemId(any(CaseDetails.class)))
            .thenAnswer(invocation -> invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteSettlementNoResponseFromDefendantCheckScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder()
            .id(CASE_ID)
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(searchCase))
            .build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);

        scheduler.runScheduledTask();

        verify(scheduledTask).accept(searchCase);
        verify(telemetryService).trackEvent(eq("SettlementNoResponseFromDefendantCheckJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("SettlementNoResponseFromDefendantCheckCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("SettlementNoResponseFromDefendantCheckJobCompleted"), anyMap());
    }
}

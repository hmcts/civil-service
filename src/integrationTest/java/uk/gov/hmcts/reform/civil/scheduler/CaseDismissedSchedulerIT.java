package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.casedismissed.CaseDismissedScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=CaseDismissedSchedulerIT",
    "scheduler.lockAtLeastFor=PT0S"
})
public class CaseDismissedSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "CaseDismissed";

    @Autowired
    private CaseDismissedScheduler scheduler;

    @MockitoBean
    private TelemetryService telemetryService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService);
        coreCaseDataApiMockHelper.resetMocks();
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
    }

    @Test
    void shouldExecuteCaseDismissedScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder()
            .id(CASE_ID)
            .data(new HashMap<>())
            .build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(searchCase))
            .build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);
        coreCaseDataApiMockHelper.mockGetCaseAnyCase(searchCase);

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId("eventId")
            .caseDetails(CaseDetails.builder().id(CASE_ID).data(new HashMap<>()).build())
            .build();
        coreCaseDataApiMockHelper.mockStartEventAnyCase(startEventResponse, DISMISS_CLAIM.name());
        coreCaseDataApiMockHelper.mockSubmitEventAnyCase(CaseDetailsBuilder.builder().id(CASE_ID).data(new HashMap<>()).build());

        scheduler.runScheduledTask();

        coreCaseDataApiMockHelper.verifySubmitEvent(1);
        verify(telemetryService).trackEvent(eq("CaseDismissedJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("CaseDismissedCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("CaseDismissedJobCompleted"), anyMap());
    }

    @Test
    void shouldAbortCaseDismissedScheduler_whenOngoingBusinessProcess() {
        CaseDetails searchCase = CaseDetailsBuilder.builder()
            .id(CASE_ID)
            .data(new HashMap<>())
            .build();
        CaseDetails fullCaseDetails = CaseDetailsBuilder.builder()
            .id(CASE_ID)
            .data(CaseDataBuilder.builder()
                      .businessProcess(new BusinessProcess()
                                           .setStatus(BusinessProcessStatus.STARTED))
                      .build())
            .build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(searchCase))
            .build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);
        coreCaseDataApiMockHelper.mockGetCaseAnyCase(fullCaseDetails);

        scheduler.runScheduledTask();

        coreCaseDataApiMockHelper.verifySubmitEvent(0);
        verify(telemetryService).trackEvent(eq("CaseDismissedJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("CaseDismissedCaseAborted"), anyMap());
        verify(telemetryService).trackEvent(eq("CaseDismissedJobCompleted"), anyMap());
    }
}

package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.defendantresponse.DefendantResponseDeadlineScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;

import java.util.List;
import java.util.Map;

import static java.util.stream.IntStream.rangeClosed;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_DEADLINE_CHECK;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=DefendantResponseDeadlineSchedulerIT",
    "search.defendant-response.pageSize=50",
    "scheduler.lockAtLeastFor=PT0S"
})
public class DefendantResponseDeadlineSchedulerIT {

    private static final Long CASE_ID = 123L;

    @Autowired
    private DefendantResponseDeadlineScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService);
        coreCaseDataApiMockHelper.resetMocks();
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(DefendantResponseDeadlineScheduler.SCHEDULER_NAME))
            .thenReturn(true);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
    }

    @Test
    void shouldExecuteDefendantResponseDeadlineScheduler() {
        // Given
        String caseIdString = CASE_ID.toString();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateJudgmentRequested().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId(caseIdString).caseDetails(
            caseDetails).build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);
        coreCaseDataApiMockHelper.mockStartEvent(
            caseIdString,
            startEventResponse,
            DEFENDANT_RESPONSE_DEADLINE_CHECK.name()
        );
        coreCaseDataApiMockHelper.mockSubmitEvent(caseIdString, caseDetails);

        // When
        scheduler.runScheduledTask();

        // Then
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineJobCompleted"), anyMap());
        coreCaseDataApiMockHelper.verifySubmitEvent(1);
    }

    @Test
    void shouldExecuteDefendantResponseDeadlineSchedulerAcrossMultiplePages() {
        // Given a result set one case larger than a single page
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateJudgmentRequested().id(CASE_ID).build();

        SearchResult page1 = SearchResult.builder().total(51).cases(createCaseDetailsBatch(50)).build();
        SearchResult page2 = SearchResult.builder().total(51)
            .cases(List.of(CaseDetailsBuilder.builder().id(51L).data(Map.of()).build())).build();

        coreCaseDataApiMockHelper.mockElasticSearchResultPaginated(page1, page2);
        coreCaseDataApiMockHelper.mockStartEventAnyCase(
            StartEventResponse.builder().eventId(CASE_ID.toString()).caseDetails(caseDetails).build(),
            DEFENDANT_RESPONSE_DEADLINE_CHECK.name()
        );
        coreCaseDataApiMockHelper.mockSubmitEventAnyCase(caseDetails);

        // When
        scheduler.runScheduledTask();

        // Then every case on both pages is processed
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineJobCompleted"), anyMap());
        coreCaseDataApiMockHelper.verifySubmitEvent(51);
    }

    private List<CaseDetails> createCaseDetailsBatch(int size) {
        return rangeClosed(1, size)
            .mapToObj(i -> CaseDetailsBuilder.builder().id((long) i).data(Map.of()).build())
            .toList();
    }
}

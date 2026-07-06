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
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.HearingFeeScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_PAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_HEARING_FEE_DUE;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=HearingFeeSchedulerIT",
    "scheduler.hearing-fee.enabled=true",
    "scheduler.lockAtLeastFor=PT0S"
})
public class HearingFeeSchedulerIT {

    private static final Long CASE_ID = 1234L;

    @Autowired
    private HearingFeeScheduler scheduler;

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
        when(featureToggleService.isSpringSchedulerEnabled(HearingFeeScheduler.SCHEDULER_NAME))
            .thenReturn(true);
    }

    @Test
    void shouldExecuteHearingFeeScheduler_WhenNoHearingFeeDue() {
        // Given
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        String caseIdString = CASE_ID.toString();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateDecisionOutcome().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId(caseIdString).caseDetails(
            caseDetails).build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);
        coreCaseDataApiMockHelper.mockGetCase(caseIdString, caseDetails);
        coreCaseDataApiMockHelper.mockStartEvent(
            caseIdString,
            startEventResponse,
            NO_HEARING_FEE_DUE.name()
        );
        coreCaseDataApiMockHelper.mockSubmitEvent(caseIdString, caseDetails);

        // When
        scheduler.runScheduledTask();

        // Then
        verify(telemetryService).trackEvent(eq("HearingFeeJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("HearingFeeJobCompleted"), anyMap());
        coreCaseDataApiMockHelper.verifySubmitEvent(1);
    }

    @Test
    void shouldExecuteHearingFeeScheduler_WhenNoHearingFeePaid() {
        // Given
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        String caseIdString = CASE_ID.toString();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateHearingFeePaid().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId(caseIdString).caseDetails(
            caseDetails).build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);
        coreCaseDataApiMockHelper.mockGetCase(caseIdString, caseDetails);
        coreCaseDataApiMockHelper.mockStartEvent(
            caseIdString,
            startEventResponse,
            HEARING_FEE_PAID.name()
        );
        coreCaseDataApiMockHelper.mockSubmitEvent(caseIdString, caseDetails);

        // When
        scheduler.runScheduledTask();

        // Then
        verify(telemetryService).trackEvent(eq("HearingFeeJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("HearingFeeJobCompleted"), anyMap());
        coreCaseDataApiMockHelper.verifySubmitEvent(1);
    }

    @Test
    void shouldExecuteHearingFeeScheduler_WhenNoHearingFeeUnpaid() {
        // Given
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        String caseIdString = CASE_ID.toString();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateHearingFeeUnpaid().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId(caseIdString).caseDetails(
            caseDetails).build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);
        coreCaseDataApiMockHelper.mockGetCase(caseIdString, caseDetails);
        coreCaseDataApiMockHelper.mockStartEvent(
            caseIdString,
            startEventResponse,
            HEARING_FEE_UNPAID.name()
        );
        coreCaseDataApiMockHelper.mockSubmitEvent(caseIdString, caseDetails);

        // When
        scheduler.runScheduledTask();

        // Then
        verify(telemetryService).trackEvent(eq("HearingFeeJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("HearingFeeJobCompleted"), anyMap());
        coreCaseDataApiMockHelper.verifySubmitEvent(1);
    }
}

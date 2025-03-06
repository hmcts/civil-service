package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.HearingFeeUnpaidEvent;
import uk.gov.hmcts.reform.civil.event.HearingFeePaidEvent;
import uk.gov.hmcts.reform.civil.event.NoHearingFeeDueEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.HearingFeeDueSearchService;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class HearingFeeDueHandlerTest {

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @Mock
    private HearingFeeDueSearchService searchService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private HearingFeeDueHandler handler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");

    }

    @Test
    void shouldEmitNoHearingFeeDueEvent_whenCasesFoundNoFeeDue() {
        long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder().atStateNoHearingFeeDue().build();
        Map<String, Object> data = Map.of("data", caseData);
        Set<CaseDetails> caseDetails = Set.of(CaseDetails.builder().id(caseId).data(data).build());

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        when(searchService.getCases()).thenReturn(caseDetails);
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails.iterator().next());
        when(caseDetailsConverter.toCaseData(caseDetails.iterator().next())).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new NoHearingFeeDueEvent(caseId));
        verify(externalTaskService).complete(mockTask, null);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldEmitHearingFeePaidEvent_whenCasesFoundPaid(boolean toggle) {
        long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDuePaid().build();
        Map<String, Object> data = Map.of("data", caseData);
        Set<CaseDetails> caseDetails = Set.of(CaseDetails.builder().id(caseId).data(data).build());

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(toggle);
        when(searchService.getCases()).thenReturn(caseDetails);
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails.iterator().next());
        when(caseDetailsConverter.toCaseData(caseDetails.iterator().next())).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new HearingFeePaidEvent(caseId));
        verify(externalTaskService).complete(mockTask, null);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldEmitHearingFeePaidEvent_whenCasesFoundPaidWithHWF(boolean toggle) {
        long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder()
            .atStateHearingFeeDuePaidWithHwf()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder()
                                          .hwfFullRemissionGrantedForHearingFee(YesOrNo.YES).build())
            .build();
        caseData = caseData.toBuilder().hearingHelpFeesReferenceNumber("HWF-111-111").build();
        Map<String, Object> data = Map.of("data", caseData);
        Set<CaseDetails> caseDetails = Set.of(CaseDetails.builder().id(caseId).data(data).build());

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(toggle);
        when(searchService.getCases()).thenReturn(caseDetails);
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails.iterator().next());
        when(caseDetailsConverter.toCaseData(caseDetails.iterator().next())).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new HearingFeePaidEvent(caseId));
        verify(externalTaskService).complete(mockTask, null);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldEmitHearingFeeUnpaidEvent_whenCasesFoundUnpaid(boolean toggle) {
        long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder().atStateHearingFeeDueUnpaid().build();
        Map<String, Object> data = Map.of("data", caseData);
        Set<CaseDetails> caseDetails = Set.of(CaseDetails.builder().id(caseId).data(data).build());

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(toggle);
        when(searchService.getCases()).thenReturn(caseDetails);
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails.iterator().next());
        when(caseDetailsConverter.toCaseData(caseDetails.iterator().next())).thenReturn(caseData);

        handler.execute(mockTask, externalTaskService);

        verify(applicationEventPublisher).publishEvent(new HearingFeeUnpaidEvent(caseId));
        verify(externalTaskService).complete(mockTask, null);
    }

    @Test
    void shouldNotEmitTakeCaseOfflineEvent_WhenNoCasesFound() {
        when(searchService.getCases()).thenReturn(Set.of());

        handler.execute(mockTask, externalTaskService);

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
        String errorMessage = "there was an error";

        when(mockTask.getRetries()).thenReturn(null);
        when(searchService.getCases()).thenAnswer(invocation -> {
            throw new Exception(errorMessage);
        });

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).complete(mockTask);
        verify(externalTaskService).handleFailure(
            eq(mockTask),
            eq(errorMessage),
            anyString(),
            eq(2),
            eq(300000L)
        );
    }

    @Test
    void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {
        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }

    @Test
    void shouldHandleExceptionAndContinue_whenOneCaseErrors() {
        long caseId = 1L;
        long otherId = 2L;
        Map<String, Object> data = Map.of("data", "some data");
        Set<CaseDetails> caseDetails = Set.of(
            CaseDetails.builder().id(caseId).data(data).build(),
            CaseDetails.builder().id(otherId).data(data).build());

        when(searchService.getCases()).thenReturn(caseDetails);

        String errorMessage = "there was an error";

        handler.execute(mockTask, externalTaskService);

        verify(externalTaskService, never()).handleFailure(
            any(ExternalTask.class),
            anyString(),
            anyString(),
            anyInt(),
            anyLong()
        );
    }
}

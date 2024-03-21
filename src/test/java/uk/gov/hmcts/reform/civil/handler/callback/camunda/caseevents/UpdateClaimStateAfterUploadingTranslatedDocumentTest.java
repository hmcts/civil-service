package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UpdateClaimStateService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED;

@ExtendWith(MockitoExtension.class)
public class UpdateClaimStateAfterUploadingTranslatedDocumentTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateClaimStateAfterUploadingTranslatedDocuments handler;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private UpdateClaimStateService updateClaimStateService;
    @Mock
    private ToggleConfiguration toggleConfiguration;

    public static final String TASK_ID = "updateClaimStateAfterTranslateDocumentUploadedID";

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo(TASK_ID);
    }

    @Test
    void shouldRunAboutToSubmitSuccessfully() {
        // given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondent1v1BilingualFlagSet().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED);

    }

    @Test
    void shouldUpdateClaimState_WhenClaimStateIsClaimIssued() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
                .build().toBuilder()
                .ccdState(CaseState.CASE_ISSUED)
                .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name());
    }

    @Test
    void shouldUpdateClaimState_WhenAwaitingApplicantIntentionClaimState() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .build().toBuilder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .build();
        when(updateClaimStateService.setUpCaseState(caseData)).thenReturn(JUDICIAL_REFERRAL.name());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        verify(updateClaimStateService, times(1)).setUpCaseState(caseData);
    }

    @Test
    void shouldNotUpdateClaimState_WhenClaimantSignedSettlementAgreement() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .build().toBuilder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .build();
        caseData = caseData.toBuilder()
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1LiPResponse(ClaimantLiPResponse.builder()
                    .applicant1SignedSettlementAgreement(YesOrNo.YES)
                    .build())
                .build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
        verifyNoInteractions(updateClaimStateService);
    }
}

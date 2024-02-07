package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UpdateClaimStateService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class UpdateClaimantIntentionClaimStateCallbackHandlerTests extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateClaimantIntentionClaimStateCallbackHandler handler;

    @Mock
    private  UpdateClaimStateService updateClaimStateService;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("updateClaimantIntentionClaimStateID");
    }

    @Test
    void shouldRunAboutToSubmitSuccessfully() {
        // given
        CaseData caseData = CaseDataBuilder.builder().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(updateClaimStateService, times(1)).setUpCaseState(caseData);
    }

    @Test
    void shouldNotChangeStateIfClaimantPreferenceBilingual() {
        // given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verifyNoInteractions(updateClaimStateService);
    }

    @Test
    void shouldNotChangeStateIfCcjRequestedAndClaimantAcceptCourtDecision() {
        // given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder().caseDataLiP(CaseDataLiP.builder()
            .applicant1LiPResponse(ClaimantLiPResponse.builder()
                .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                .claimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_DATE)
                .build())
            .build())
        .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verifyNoInteractions(updateClaimStateService);
    }

    @Test
    void shouldNotChangeStateIfCcjRequestedAndCourtDecisionInFavourOfClaimant() {
        // given
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData = caseData.toBuilder().caseDataLiP(CaseDataLiP.builder()
            .applicant1LiPResponse(ClaimantLiPResponse.builder()
                .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                .build())
            .build())
        .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verifyNoInteractions(updateClaimStateService);
    }
}

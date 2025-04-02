package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UpdateClaimStateService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class UpdateClaimantIntentionClaimStateCallbackHandlerTests extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateClaimantIntentionClaimStateCallbackHandler handler;

    @Mock
    private  UpdateClaimStateService updateClaimStateService;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ToggleConfiguration toggleConfiguration;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        handler = new UpdateClaimantIntentionClaimStateCallbackHandler(objectMapper, updateClaimStateService, toggleConfiguration);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

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
    void shouldSetTakenOfflineDateIfProceedsInHeritage() {
        // given
        CaseData caseData = CaseDataBuilder.builder().build();
        when(updateClaimStateService.setUpCaseState(any())).thenReturn("PROCEEDS_IN_HERITAGE_SYSTEM");

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(response.getErrors()).isNull();
        assertThat(updatedData.getTakenOfflineDate()).isNotNull();
    }

    @Test
    void shouldNotChangeCaseStateIfClaimantSignedSettlementAgreement() {
        // given
        CaseData caseData = CaseDataBuilder.builder().build();
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
        verifyNoInteractions(updateClaimStateService);
    }
}

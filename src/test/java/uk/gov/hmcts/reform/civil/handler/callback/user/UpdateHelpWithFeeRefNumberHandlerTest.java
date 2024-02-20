package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;

@ExtendWith(MockitoExtension.class)
class UpdateHelpWithFeeRefNumberHandlerTest extends BaseCallbackHandlerTest {

    private UpdateHelpWithFeeRefNumberHandler handler;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new UpdateHelpWithFeeRefNumberHandler(objectMapper);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUpdateHwFReferenceNumberSuccessfully_FeeType_ClaimIssued() {
            //Given
            CaseData caseData = CaseData.builder()
                    .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                            .hwfReferenceNumber("7890").build())
                    .hwfFeeType(FeeType.CLAIMISSUED)
                    .caseDataLiP(CaseDataLiP.builder().helpWithFees(
                            HelpWithFees.builder()
                                    .helpWithFee(YesOrNo.YES)
                                    .helpWithFeesReferenceNumber("23456")
                                    .build()).build())
                    .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData data = getCaseData(response);
            Assertions.assertThat(data.getCaseDataLiP().getHelpWithFees().getHelpWithFeesReferenceNumber()).isEqualTo("7890");
            Assertions.assertThat(data.getClaimIssuedHwfDetails().getHwfReferenceNumber()).isNull();
        }

        @Test
        void shouldUpdateHwFReferenceNumberSuccessfully_FeeType_Hearing() {
            //Given
            CaseData caseData = CaseData.builder()
                    .hearingHwfDetails(HelpWithFeesDetails.builder()
                            .hwfReferenceNumber("78905185430").build())
                    .hwfFeeType(FeeType.HEARING)
                    .hearingHelpFeesReferenceNumber("54376543219")
                    .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData data = getCaseData(response);
            Assertions.assertThat(data.getHearingHelpFeesReferenceNumber()).isEqualTo("78905185430");
            Assertions.assertThat(data.getHearingHwfDetails().getHwfReferenceNumber()).isNull();
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPDATE_HELP_WITH_FEE_NUMBER);
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }
}

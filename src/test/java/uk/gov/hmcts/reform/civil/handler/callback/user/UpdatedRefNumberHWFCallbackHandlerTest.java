package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.genapplication.HelpWithFeesDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER_GA;

@ExtendWith(MockitoExtension.class)
class UpdatedRefNumberHWFCallbackHandlerTest extends BaseCallbackHandlerTest {

    private UpdatedRefNumberHWFCallbackHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new UpdatedRefNumberHWFCallbackHandler(objectMapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPDATE_HELP_WITH_FEE_NUMBER_GA);
    }

    @Nested
    class AboutToSubmit {

        private static final String NEW_HWF_REF_NUMBER = "new_hwf_ref_number";

        @Test
        void shouldUpdateRefNumber_forApplicationHwf() {
            CaseData caseData = CaseData.builder()
                .hwfFeeType(FeeType.APPLICATION)
                .generalAppHelpWithFees(HelpWithFees.builder().build())
                .gaHwfDetails(HelpWithFeesDetails.builder()
                                  .hwfReferenceNumber(NEW_HWF_REF_NUMBER).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseCaseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber()).isEqualTo(NEW_HWF_REF_NUMBER);
            assertThat(responseCaseData.getGaHwfDetails().getHwfReferenceNumber()).isNull();
            assertThat(responseCaseData.getBusinessProcess()
                    .getCamundaEvent()).isEqualTo(NOTIFY_APPLICANT_LIP_HWF.toString());
        }

        @Test
        void shouldUpdateRefNumber_forAdditionalHwf() {
            CaseData caseData = CaseData.builder()
                .hwfFeeType(FeeType.ADDITIONAL)
                .generalAppHelpWithFees(HelpWithFees.builder().build())
                .additionalHwfDetails(HelpWithFeesDetails.builder()
                                  .hwfReferenceNumber(NEW_HWF_REF_NUMBER).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseCaseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber()).isEqualTo(NEW_HWF_REF_NUMBER);
            assertThat(responseCaseData.getAdditionalHwfDetails().getHwfReferenceNumber()).isNull();
            assertThat(responseCaseData.getBusinessProcess()
                    .getCamundaEvent()).isEqualTo(NOTIFY_APPLICANT_LIP_HWF.toString());
        }
    }
}

package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_HWF;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@ExtendWith(MockitoExtension.class)
class GaInvalidHwFCallbackHandlerTest {

    private ObjectMapper objectMapper;
    private GaInvalidHwFCallbackHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new GaInvalidHwFCallbackHandler(objectMapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(INVALID_HWF_REFERENCE_GA);
    }

    @Nested
    class AboutToSubmit {
        @Test
        void shouldSubmit_InvalidHwFEvent() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .ccdState(AWAITING_RESPONDENT_RESPONSE)
                .hwfFeeType(FeeType.APPLICATION)
                .generalAppHelpWithFees(new HelpWithFees()).build();

            CallbackParams params = new CallbackParams()
                .type(CallbackType.ABOUT_TO_SUBMIT)
                .caseData(caseData);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData).isNotNull();
            assertThat(updatedData.getGaHwfDetails().getHwfCaseEvent()).isEqualTo(INVALID_HWF_REFERENCE_GA);
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(NOTIFY_APPLICANT_LIP_HWF.toString());
        }
    }

}

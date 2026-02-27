package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@ExtendWith(MockitoExtension.class)
 class GaNoRemissionHWFCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @InjectMocks
    GaNoRemissionHWFCallbackHandler handler;

    @Mock
    CaseDetailsConverter caseDetailsConverter;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(NO_REMISSION_HWF_GA);
    }

    @Test
    void shouldSubmit_NoRemissionHwFEventAndStartNotifyApplicantLip() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(AWAITING_RESPONDENT_RESPONSE)
            .hwfFeeType(FeeType.APPLICATION)
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                    new Fee()
                        .setCalculatedAmountInPence(BigDecimal.valueOf(500)).setCode("FEE205"))
                                      )
            .gaHwfDetails(new HelpWithFeesDetails()
                               .setRemissionAmount(BigDecimal.valueOf(500))
                               )
            .generalAppHelpWithFees(new HelpWithFees()).build();
        CallbackParams params = callbackParamsOf(caseData, CaseEvent.NO_REMISSION_HWF_GA, CallbackType.ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        //Then
        GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(updatedData).isNotNull();
        assertThat(updatedData.getGaHwfDetails().getHwfCaseEvent()).isEqualTo(NO_REMISSION_HWF_GA);
        assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(NOTIFY_APPLICANT_LIP_HWF.toString());
    }

}

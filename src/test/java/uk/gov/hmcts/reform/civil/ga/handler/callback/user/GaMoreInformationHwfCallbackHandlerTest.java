package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_HWF;

@ExtendWith(MockitoExtension.class)
class GaMoreInformationHwfCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private GaMoreInformationHwfCallbackHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        handler = new GaMoreInformationHwfCallbackHandler(new ObjectMapper());
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    class MidCallback {

        @Test
        void shouldValidationMoreInformationGa_withInvalidDate() {
            //Given
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .helpWithFeesMoreInformationGa(
                        HelpWithFeesMoreInformation.builder()
                        .hwFMoreInfoDocumentDate(LocalDate.now())
                        .build())
                .build();
            //When
            CallbackParams params = callbackParamsOf(caseData, MID, "more-information-hwf");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            Assertions.assertThat(response.getErrors()).containsExactly("Documents date must be future date");
        }

        @Test
        void shouldValidationMoreInformationAdditional_withInvalidDate() {
            //Given
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .hwfFeeType(FeeType.ADDITIONAL)
                .helpWithFeesMoreInformationAdditional(
                    HelpWithFeesMoreInformation.builder()
                        .hwFMoreInfoDocumentDate(LocalDate.now())
                        .build())
                .build();
            //When
            CallbackParams params = callbackParamsOf(caseData, MID, "more-information-hwf");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            Assertions.assertThat(response.getErrors()).containsExactly("Documents date must be future date");
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldCallSubmitMoreInformationHwfAboutToSubmitApplication() {
            //Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(MORE_INFORMATION_HWF_GA)
                .noRemissionDetailsSummary(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET).build();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .gaHwfDetails(hwfeeDetails)
                .hwfFeeType(
                    FeeType.APPLICATION)
                .build();
            //When
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response).isNotNull();
            GeneralApplicationCaseData data = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            Assertions.assertThat(data.getGaHwfDetails().getHwfReferenceNumber()).isNull();
            Assertions.assertThat(data.getBusinessProcess().getCamundaEvent()).isEqualTo(NOTIFY_APPLICANT_LIP_HWF.toString());
            Assertions.assertThat(data.getGaHwfDetails().getHwfCaseEvent()).isEqualTo(MORE_INFORMATION_HWF_GA);
        }

        @Test
        void shouldCallSubmitMoreInformationHwfAboutToSubmitAdditional() {
            //Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(MORE_INFORMATION_HWF_GA)
                .noRemissionDetailsSummary(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET).build();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .additionalHwfDetails(hwfeeDetails)
                .hwfFeeType(
                    FeeType.ADDITIONAL)
                .build();
            //When
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response).isNotNull();
        }
    }
}

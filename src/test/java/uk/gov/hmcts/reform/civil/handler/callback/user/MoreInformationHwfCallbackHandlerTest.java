package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME;

@ExtendWith(MockitoExtension.class)
class MoreInformationHwfCallbackHandlerTest extends BaseCallbackHandlerTest {

    private MoreInformationHwfCallbackHandler handler;
    private ObjectMapper objectMapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        handler = new MoreInformationHwfCallbackHandler(new ObjectMapper());
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    class MidCallback {

        @Test
        void shouldValidationMoreInformationClaimIssued_withInvalidDate() {
            //Given
            CaseData caseData = CaseData.builder()
                .helpWithFeesMoreInformationClaimIssue(
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
        void shouldValidationMoreInformationHearing_withInvalidDate() {
            //Given
            CaseData caseData = CaseData.builder()
                .hwfFeeType(FeeType.HEARING)
                .helpWithFeesMoreInformationHearing(
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
        void shouldCallSubmitMoreInformationHwfAboutToSubmitClaimIssued() {
            //Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(MORE_INFORMATION_HWF)
                .noRemissionDetailsSummary(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET).build();
            CaseData caseData = CaseData.builder()
                .claimIssuedHwfDetails(hwfeeDetails)
                .hwfFeeType(
                    FeeType.CLAIMISSUED)
                .build();
            //When
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response).isNotNull();
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            Assertions.assertThat(data.getClaimIssuedHwfDetails().getHwfReferenceNumber()).isNull();
            Assertions.assertThat(data.getBusinessProcess().getCamundaEvent()).isEqualTo(NOTIFY_LIP_CLAIMANT_HWF_OUTCOME.toString());
            Assertions.assertThat(data.getClaimIssuedHwfDetails().getHwfCaseEvent()).isEqualTo(MORE_INFORMATION_HWF);
        }

        @Test
        void shouldCallSubmitMoreInformationHwfAboutToSubmitHearing() {
            //Given
            HelpWithFeesDetails hwfeeDetails = HelpWithFeesDetails.builder()
                .hwfCaseEvent(MORE_INFORMATION_HWF)
                .noRemissionDetailsSummary(NoRemissionDetailsSummary.FEES_REQUIREMENT_NOT_MET).build();
            CaseData caseData = CaseData.builder()
                .hearingHwfDetails(hwfeeDetails)
                .hwfFeeType(
                    FeeType.HEARING)
                .build();
            //When
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response).isNotNull();
        }
    }
}

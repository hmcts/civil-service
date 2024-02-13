package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    MoreInformationHwfCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
class MoreInformationHwfCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private MoreInformationHwfCallbackHandler handler;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldValidationMoreInformationClaimIssued_withInvalidDate() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build()
                .builder()
                .helpWithFeesMoreInformationClaimIssue(
                    HelpWithFeesMoreInformation.builder()
                        .hwFMoreInfoDocumentDate(LocalDate.now())
                        .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "more-information-hwf");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getErrors()).containsExactly("Documents date must be future date");
        }

        @Test
        void shouldValidationMoreInformationHearing_withInvalidDate() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build()
                .builder()
                .hwfFeeType(FeeType.HEARING)
                .helpWithFeesMoreInformationHearing(
                    HelpWithFeesMoreInformation.builder()
                        .hwFMoreInfoDocumentDate(LocalDate.now())
                        .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "more-information-hwf");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getErrors()).containsExactly("Documents date must be future date");
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCallSubmitMoreInformationHwfAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
        }
    }
}

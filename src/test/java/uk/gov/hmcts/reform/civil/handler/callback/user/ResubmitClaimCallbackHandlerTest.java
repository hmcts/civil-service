package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class ResubmitClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    private ResubmitClaimCallbackHandler handler;

    @Mock
    private ExitSurveyContentService exitSurveyContentService;

    @Mock
    private FeatureToggleService toggleService;

    @BeforeEach
    void setup() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new ResubmitClaimCallbackHandler(exitSurveyContentService, mapper, toggleService);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CREATE_CLAIM.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked_spec() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim()
                .build().toBuilder().caseAccessCategory(SPEC_CLAIM).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CREATE_CLAIM_SPEC.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked_spec_blocked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim()
                .build().toBuilder().caseAccessCategory(SPEC_CLAIM).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            // Then
            assertThat(response.getData())
                .doesNotHaveToString("businessProcess");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1Represented(NO).build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Claim pending")
                    .confirmationBody(
                        "<br />Your claim will be processed. Wait for us to contact you."
                            + exitSurveyContentService.applicantSurvey()
                    )
                    .build());
        }
    }
}

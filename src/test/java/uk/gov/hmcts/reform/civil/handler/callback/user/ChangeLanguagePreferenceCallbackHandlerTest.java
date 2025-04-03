package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.ENGLISH_AND_WELSH;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.WELSH;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.UserType.CLAIMANT;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.UserType.DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class ChangeLanguagePreferenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    private ChangeLanguagePreferenceCallbackHandler handler;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new ChangeLanguagePreferenceCallbackHandler(mapper);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnError_WhenClaimantIsNotLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactly("At least one party on the claim must be unrepresented.");
        }

        @Test
        void shouldNotReturnError_WhenApplicantIsLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            caseData = caseData.toBuilder().applicant1Represented(NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnError_WhenRespondentIsLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            caseData = caseData.toBuilder().respondent1Represented(NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldThrowException_WhenNoLanguageChangeData() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(params))
                .withMessage("Preferred language not found");
        }

        @Test
        void shouldThrowException_WhenNoLanguagePreference() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            caseData = caseData.toBuilder().changeLanguagePreference(ChangeLanguagePreference.builder()
                                                                         .userType(CLAIMANT).build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(params))
                .withMessage("Preferred language not found");
        }

        @Test
        void shouldThrowException_WhenNoUserType() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            caseData = caseData.toBuilder().changeLanguagePreference(ChangeLanguagePreference.builder()
                                                                         .preferredLanguage(WELSH).build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(params))
                .withMessage("User type not found");
        }

        @Test
        void shouldReturnError_WhenClaimantIsNotLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            caseData = caseData.toBuilder().changeLanguagePreference(ChangeLanguagePreference.builder()
                                                                         .userType(CLAIMANT)
                                                                         .preferredLanguage(WELSH).build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactly("The selected party must be unrepresented.");
        }

        @Test
        void shouldReturnError_WhenDefendantIsNotLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            caseData = caseData.toBuilder().changeLanguagePreference(ChangeLanguagePreference.builder()
                                                                         .userType(DEFENDANT)
                                                                         .preferredLanguage(WELSH).build()).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactly("The selected party must be unrepresented.");
        }

        @Test
        void shouldReturnError_WhenDefendantHasNotResponded() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            caseData = caseData.toBuilder()
                .changeLanguagePreference(ChangeLanguagePreference.builder()
                                              .userType(DEFENDANT)
                                              .preferredLanguage(WELSH).build())
                .respondent1Represented(NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactly("The defendant must have already responded in order to change their language preference.");
        }

        @Test
        void shouldChangeClaimantLanguagePreference() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            caseData = caseData.toBuilder()
                .changeLanguagePreference(ChangeLanguagePreference.builder()
                                              .userType(CLAIMANT)
                                              .preferredLanguage(WELSH).build())
                .applicant1Represented(NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getClaimantBilingualLanguagePreference()).isEqualTo("WELSH");
        }

        @Test
        void shouldChangeDefendantLanguagePreference() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            caseData = caseData.toBuilder()
                .changeLanguagePreference(ChangeLanguagePreference.builder()
                                              .userType(DEFENDANT)
                                              .preferredLanguage(ENGLISH_AND_WELSH).build())
                .respondent1Represented(NO)
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage("ENGLISH").build())
                                 .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1ResponseLanguage()).isEqualTo("BOTH");
        }
    }
}

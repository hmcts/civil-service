package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationCancelledReason;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationNotConductedReason;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationNotSettledReason;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulOutcome;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@ExtendWith(MockitoExtension.class)
class MediationUnsuccessfulHandlerTest extends BaseCallbackHandlerTest {

    private MediationUnsuccessfulHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new MediationUnsuccessfulHandler(objectMapper, featureToggleService);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPopulateCarmShowCondition_whenCarmApplicableToCase() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued().build();
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("Yes");
        }

        @Test
        void shouldPopulateCarmShowCondition_whenCarmNotApplicableToCase() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued().build();
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("No");
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCallSubmitSuccessfulMediationUponAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .mediation(new Mediation()
                               .setUnsuccessfulMediationReason("PARTY_WITHDRAWS"))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("unsuccessfulMediationReason").isNotNull();
            assertThat(response.getState())
                .isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldLeaveStoredMediationData_whenCaseworkerMediationActivitiesDisabled() {
            when(featureToggleService.isCaseworkerMediationActivitiesEnabled()).thenReturn(false);
            CaseData caseData = caseWithNotConductedAndEarlierNotSettledAnswer();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
            var submitted = (SubmittedCallbackResponse) handler.handle(callbackParamsOf(caseData, SUBMITTED));

            assertThat(response.getData().get("unsuccessfulMediationReason")).isEqualTo("PARTY_WITHDRAWS");
            assertThat(response.getData().get("mediationCancelledReason")).isEqualTo("ADMIN_ERROR");
            assertThat(response.getData().get("mediationNotSettledReason")).isEqualTo("PARTIES_TOO_FAR_APART");
            assertThat(response.getData().get("mediationOutcome")).isNull();
            assertThat(response.getState()).isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
            assertThat(submitted.getConfirmationHeader()).isNull();
        }

        @Test
        void shouldRecordUnsuccessfulOutcomeAndRemoveUnselectedReason_whenCaseworkerMediationActivitiesEnabled() {
            when(featureToggleService.isCaseworkerMediationActivitiesEnabled()).thenReturn(true);
            CaseData caseData = caseWithNotConductedAndEarlierNotSettledAnswer();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
            var submitted = (SubmittedCallbackResponse) handler.handle(callbackParamsOf(caseData, SUBMITTED));

            assertThat(response.getData().get("unsuccessfulMediationReason")).isEqualTo("PARTY_WITHDRAWS");
            assertThat(response.getData().get("mediationNotConductedReason")).isEqualTo("CANCELLED");
            assertThat(response.getData().get("mediationCancelledReason")).isEqualTo("ADMIN_ERROR");
            assertThat(response.getData().get("mediationNotSettledReason")).isNull();
            assertThat(response.getData().get("mediationOutcome")).isEqualTo("Unsuccessful");
            assertThat(response.getState()).isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
            assertThat(submitted.getConfirmationHeader())
                .isEqualTo("# Unsuccessful mediation outcome is recorded");
        }

        private CaseData caseWithNotConductedAndEarlierNotSettledAnswer() {
            return CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .mediation(new Mediation()
                    .setUnsuccessfulMediationReason("PARTY_WITHDRAWS")
                    .setMediationUnsuccessfulOutcome(MediationUnsuccessfulOutcome.NOT_CONDUCTED)
                    .setMediationNotConductedReason(MediationNotConductedReason.CANCELLED)
                    .setMediationCancelledReason(MediationCancelledReason.ADMIN_ERROR)
                    .setMediationNotSettledReason(MediationNotSettledReason.PARTIES_TOO_FAR_APART))
                .build();
        }
    }
}

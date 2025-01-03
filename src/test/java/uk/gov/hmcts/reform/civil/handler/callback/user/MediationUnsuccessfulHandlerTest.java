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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
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
                .build()
                .builder()
                .mediation(Mediation
                               .builder()
                               .unsuccessfulMediationReason("PARTY_WITHDRAWS")
                               .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("unsuccessfulMediationReason").isNotNull();
            assertThat(response.getState())
                .isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }
    }
}

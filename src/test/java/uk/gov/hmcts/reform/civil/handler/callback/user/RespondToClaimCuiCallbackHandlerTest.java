package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToClaimCuiCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseFlagsInitialiser.class
})
class RespondToClaimCuiCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    OrganisationService organisationService;
    @Autowired
    private RespondToClaimCuiCallbackHandler handler;
    @Autowired
    CaseFlagsInitialiser caseFlagsInitialiser;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime now;
        private final LocalDateTime respondToDeadline = LocalDateTime.of(
            2023,
            1,
            1,
            0,
            0,
            0);

        @BeforeEach
        void setup() {
            now = LocalDateTime.now();
            given(time.now()).willReturn(now);
            given(deadlinesCalculator.calculateApplicantResponseDeadline(any(), any())).willReturn(respondToDeadline);
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        }

        @Test
        void shouldUpdateBusinessProcessAndClaimStatus_whenDefendantResponseLangIsEnglish() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(5000))
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("ENGLISH").build()).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(DEFENDANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
            assertThat(response.getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        }

        @Test
        void shouldOnlyUpdateClaimStatus_whenDefendantResponseLangIsBilingual() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(5000))
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(DEFENDANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
            assertThat(response.getState()).isNull();
        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return mapper.convertValue(response.getData(), CaseData.class);
        }

        @Test
        void shouldAddTheCaseFlagIntialiazerForDefendant() {
            when(featureToggleService.isHmcEnabled()).thenReturn(true);
            when(featureToggleService.isCaseFlagsEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
                .respondent1(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("CLAIMANT_NAME")
                                 .build())
                .respondent1DQ(Respondent1DQ.builder()
                                  .respondent1DQExperts(Experts.builder()
                                                           .expertRequired(YES)
                                                           .details(wrapElements(Expert.builder()
                                                                                     .name(
                                                                                         "John Smith")
                                                                                     .firstName("Jane")
                                                                                     .lastName("Smith")

                                                                                     .build()))
                                                           .build())
                                  .respondent1DQWitnesses(Witnesses.builder().witnessesToAppear(YES)
                                                             .details(wrapElements(Witness.builder()
                                                                                       .name(
                                                                                           "John Smith")
                                                                                       .firstName("Jane")
                                                                                       .lastName("Smith")

                                                                                       .build())).build())
                                  .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getRespondent1DQ().getRespondent1DQExperts()).isNotNull();
            assertThat(updatedCaseData.getRespondent1DQ().getRespondent1DQExperts()).isNotNull();

        }
    }
}

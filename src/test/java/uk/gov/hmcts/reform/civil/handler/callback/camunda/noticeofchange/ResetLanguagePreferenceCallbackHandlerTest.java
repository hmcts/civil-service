package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESET_LANGUAGE_PREFERENCE;

@ExtendWith(MockitoExtension.class)
public class ResetLanguagePreferenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ResetLanguagePreferenceCallbackHandler handler;

    @Mock
    private ObjectMapper mapper;

    private static final String NEW_ORG_ID = "1234";
    private static final String TASK_ID = "ResetLanguagePreferenceAfterNoC";

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new ResetLanguagePreferenceCallbackHandler(mapper);
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(
            RESET_LANGUAGE_PREFERENCE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder().build()))
            .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldResetLanguageFlag_afterNocSubmittedByApplicantSolicitorForClaimantLip() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .changeOfRepresentation(true, false, NEW_ORG_ID, null, null)
                .claimantUserDetails(IdamUserDetails.builder().email("xyz@hmcts.com").id("1234").build())
                .claimantBilingualLanguagePreference("WELSH")
                .build();
            caseData = caseData.toBuilder().claimantLanguagePreferenceDisplay(PreferredLanguage.WELSH).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getClaimantBilingualLanguagePreference()).isNull();
            assertThat(updatedCaseData.getClaimantLanguagePreferenceDisplay()).isNull();
        }

        @Test
        void shouldResetLanguageFlag_afterNocSubmittedByDefendantSolicitorForDefendantLip() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .changeOfRepresentation(false, false, NEW_ORG_ID, null, null)
                .claimantUserDetails(IdamUserDetails.builder().email("xyz@hmcts.com").id("1234").build())
                .build();
            caseData = caseData.toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(
                                     RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build()
                                 ).build())
                .defendantLanguagePreferenceDisplay(PreferredLanguage.WELSH).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1ResponseLanguage()).isNull();
            assertThat(updatedCaseData.getDefendantLanguagePreferenceDisplay()).isNull();
        }
    }
}

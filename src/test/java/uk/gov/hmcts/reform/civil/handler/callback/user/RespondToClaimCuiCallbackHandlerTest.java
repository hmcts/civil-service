package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.EventAddedEvents.DEFENDANT_RESPONSE_EVENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.HearingLength.ONE_DAY;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.ENGLISH;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.WELSH;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToClaimCuiCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseFlagsInitialiser.class
})
class RespondToClaimCuiCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockitoBean
    private Time time;
    @MockitoBean
    private DeadlinesCalculator deadlinesCalculator;
    @MockitoBean
    FeatureToggleService featureToggleService;
    @MockitoBean
    OrganisationService organisationService;
    @MockitoBean
    UpdateCaseManagementDetailsService updateCaseManagementDetailsService;
    @MockitoBean
    RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;
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
            ReflectionTestUtils.setField(handler, "caseFlagsLoggingEnabled", true);

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
            0
        );

        @BeforeEach
        void setup() {
            now = LocalDateTime.now();
            given(time.now()).willReturn(now);
            given(deadlinesCalculator.calculateApplicantResponseDeadline(any())).willReturn(respondToDeadline);
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        }

        @Test
        void shouldUpdateBusinessProcessAndClaimStatus_whenDefendantResponseLangIsEnglish() {
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("ENGLISH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(5000))
                .caseDataLip(caseDataLiP)
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
        void shouldUpdateBusinessProcessAndClaimStatus_when_is_multi_track() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("ENGLISH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(150000))
                .caseDataLip(caseDataLiP)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(DEFENDANT_RESPONSE_CUI.name());
            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
            assertThat(response.getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
            assertThat(updatedData.getResponseClaimTrack()).isEqualTo(MULTI_CLAIM.toString());

        }

        @Test
        void shouldExtendDeadline() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(36, LocalDate.now()))
                .thenReturn(LocalDateTime.now().plusMonths(36));

            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("ENGLISH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(150000))
                .caseDataLip(caseDataLiP)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Object deadlineValue = response.getData().get("claimDismissedDeadline");
            assertThat(deadlineValue).isNotNull();

            LocalDate expectedDate = LocalDate.now().plusMonths(36);
            LocalDate actualDate = LocalDateTime.parse(deadlineValue.toString()).toLocalDate();

            assertThat(actualDate).isEqualTo(expectedDate);
        }

        @Test
        void shouldPopulateRespondentWithUnavailabilityDates() {
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("ENGLISH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            UnavailableDate unavailableDate = new UnavailableDate();
            unavailableDate.setDate(LocalDate.of(2024, 2, 1));
            unavailableDate.setDateAdded(LocalDate.of(
                    2024,
                    1,
                    1
                ));
            unavailableDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            Hearing hearing = new Hearing();
            hearing.setHearingLength(ONE_DAY);
            hearing.setUnavailableDatesRequired(YES);
            hearing.setUnavailableDates(wrapElements(List.of(unavailableDate)));
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQHearing(hearing);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(5000))
                .caseDataLip(caseDataLiP)
                .build();
            caseData.setRespondent1DQ(respondent1DQ);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            LocalDateTime now = LocalDate.now().atTime(12, 0, 0);
            AboutToStartOrSubmitCallbackResponse response;
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }

            UnavailableDate unavailableDate1 = new UnavailableDate();
            unavailableDate1.setEventAdded("Defendant Response Event");
            unavailableDate1.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate1.setDateAdded(now.toLocalDate());
            unavailableDate1.setDate(LocalDate.of(2024, 2, 1));
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getRespondent1().getUnavailableDates()).isEqualTo(
                wrapElements(List.of(unavailableDate1)));
        }

        @Test
        void shouldOnlyUpdateClaimStatus_whenDefendantResponseLangIsBilingual() {
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(5000))
                .caseDataLip(caseDataLiP)
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
            Party party = new Party();
            party.setType(Party.Type.INDIVIDUAL);
            party.setPartyName("CLAIMANT_NAME");
            Expert expert = new Expert();
            expert.setName("John Smith");
            expert.setFirstName("Jane");
            expert.setLastName("Smith");
            Experts experts = new Experts();
            experts.setExpertRequired(YES);
            experts.setDetails(wrapElements(expert));
            Witness witness = new Witness();
            witness.setName("John Smith");
            witness.setFirstName("Jane");
            witness.setLastName("Smith");
            Witnesses witnesses = new Witnesses();
            witnesses.setWitnessesToAppear(YES);
            witnesses.setDetails(wrapElements(witness));
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQExperts(experts);
            respondent1DQ.setRespondent1DQWitnesses(witnesses);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .applicant1(party)
                .respondent1(party)
                .respondent1DQ(respondent1DQ)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            assertThat(updatedCaseData.getRespondent1DQ().getRespondent1DQExperts()).isNotNull();
            assertThat(updatedCaseData.getRespondent1DQ().getRespondent1DQExperts()).isNotNull();

        }

        @Test
        void shouldAddEventAndDateAddedToRespondentExpertsAndWitnesses() {
            Party party = new Party();
            party.setType(Party.Type.INDIVIDUAL);
            party.setPartyName("CLAIMANT_NAME");
            Expert expert = new Expert();
            expert.setName("John Smith");
            expert.setFirstName("Jane");
            expert.setLastName("Smith");
            Experts experts = new Experts();
            experts.setExpertRequired(YES);
            experts.setDetails(wrapElements(expert));
            Witness witness = new Witness();
            witness.setName("John Smith");
            witness.setFirstName("Jane");
            witness.setLastName("Smith");
            Witnesses witnesses = new Witnesses();
            witnesses.setWitnessesToAppear(YES);
            witnesses.setDetails(wrapElements(witness));
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQExperts(experts);
            respondent1DQ.setRespondent1DQWitnesses(witnesses);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .applicant1(party)
                .respondent1(party)
                .respondent1DQ(respondent1DQ)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);
            Expert expert1 = updatedCaseData.getRespondent1DQ().getRespondent1DQExperts().getDetails().get(0).getValue();
            Witness witness1 = updatedCaseData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails().get(0).getValue();

            assertThat(expert1.getDateAdded()).isEqualTo(LocalDateTime.now().toLocalDate());
            assertThat(expert1.getEventAdded()).isEqualTo(DEFENDANT_RESPONSE_EVENT.getValue());
            assertThat(witness1.getDateAdded()).isEqualTo(LocalDateTime.now().toLocalDate());
            assertThat(witness1.getEventAdded()).isEqualTo(DEFENDANT_RESPONSE_EVENT.getValue());
            assertThat(updatedCaseData.getNextDeadline()).isEqualTo(respondToDeadline.toLocalDate());
        }

        @ParameterizedTest
        @CsvSource({
            "WELSH, ENGLISH, ENGLISH, true, false",
            "ENGLISH, ENGLISH, ENGLISH, true, true",
            "WELSH, ENGLISH, WELSH, true, false",
            "WELSH, WELSH, ENGLISH, true, false",
            "ENGLISH, WELSH, WELSH, true, false",
            "WELSH, WELSH, WELSH, true, false",
            "WELSH, WELSH, ENGLISH, true, false",
            "WELSH, ENGLISH, ENGLISH, false, true",
            "WELSH, WELSH, ENGLISH, false, false",
            "ENGLISH, WELSH, ENGLISH, false, false",
            "WELSH, WELSH, WELSH, false, false"
        })
            void shouldMoveToAwaitingApplicantResponse_whenNoTranslations(String claimantBilingualPreference,
                                                                          String defendantBilingualPreference,
                                                                          String defendantDocumentLanguage,
                                                                          boolean toggleEnabled,
                                                                          boolean changeState) {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(toggleEnabled);
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage(defendantBilingualPreference);
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            WelshLanguageRequirements welshLanguageRequirements = new WelshLanguageRequirements();
            welshLanguageRequirements.setDocuments(Language.valueOf(defendantDocumentLanguage));
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQLanguage(welshLanguageRequirements);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimantBilingualLanguagePreference(claimantBilingualPreference)
                .caseDataLip(caseDataLiP)
                .respondent1DQ(respondent1DQ)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            String newState = response.getState();

            if (changeState) {
                assertThat(newState).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
            } else {
                assertThat(newState).isNull();
            }
        }

        @Test
        void shouldNotSetDefendantResponseLanguageDisplayIfWelshNotEnabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            assertThat(updatedCaseData.getDefendantLanguagePreferenceDisplay()).isNull();
        }

        @Test
        void shouldSetDefendantResponseLanguageDisplayToEnglishIfNotSpecified() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            assertThat(updatedCaseData.getDefendantLanguagePreferenceDisplay()).isEqualTo(ENGLISH);
        }

        @Test
        void shouldSetDefendantResponseLanguageDisplayToWelshIfSpecified() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("WELSH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .caseDataLip(caseDataLiP)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            assertThat(updatedCaseData.getDefendantLanguagePreferenceDisplay()).isEqualTo(WELSH);
        }

        @Test
        void shouldUpdateLanguagePreferenceIfWelshDocsSpecified() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("ENGLISH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            WelshLanguageRequirements welshLanguageRequirements = new WelshLanguageRequirements();
            welshLanguageRequirements.setDocuments(Language.WELSH);
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQLanguage(welshLanguageRequirements);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .caseDataLip(caseDataLiP)
                .respondent1DQ(respondent1DQ)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            assertThat(updatedCaseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1ResponseLanguage()).isEqualTo("WELSH");
            assertThat(updatedCaseData.getDefendantLanguagePreferenceDisplay()).isEqualTo(WELSH);
        }
    }
}

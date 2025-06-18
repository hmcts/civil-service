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
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private Time time;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    OrganisationService organisationService;
    @MockBean
    UpdateCaseManagementDetailsService updateCaseManagementDetailsService;
    @MockBean
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
            ReflectionTestUtils.setField(handler, "caseFlagsLoggingEnabled", false);

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
            ReflectionTestUtils.setField(handler, "caseFlagsLoggingEnabled", false);

            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        }

        @Test
        void shouldUpdateBusinessProcessAndClaimStatus_whenDefendantResponseLangIsEnglish() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(5000))
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(
                    "ENGLISH").build()).build())
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

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(150000))
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(
                    "ENGLISH").build()).build())
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
            when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(24, LocalDate.now()))
                .thenReturn(LocalDateTime.now().plusMonths(24));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(150000))
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(
                    "ENGLISH").build()).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Object deadlineValue = response.getData().get("claimDismissedDeadline");
            assertThat(deadlineValue).isNotNull();

            LocalDate expectedDate = LocalDate.now().plusMonths(24);
            LocalDate actualDate = LocalDateTime.parse(deadlineValue.toString()).toLocalDate();

            assertThat(actualDate).isEqualTo(expectedDate);
        }

        @Test
        void shouldPopulateRespondentWithUnavailabilityDates() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(5000))
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(
                    "ENGLISH").build()).build())
                .build().toBuilder().respondent1DQ(Respondent1DQ.builder()
                                                       .respondent1DQHearing(Hearing.builder()
                                                                                 .hearingLength(ONE_DAY)
                                                                                 .unavailableDatesRequired(YES)
                                                                                 .unavailableDates(wrapElements(List.of(
                                                                                     UnavailableDate.builder()
                                                                                         .date(LocalDate.of(2024, 2, 1))
                                                                                         .dateAdded(LocalDate.of(
                                                                                             2024,
                                                                                             1,
                                                                                             1
                                                                                         ))
                                                                                         .unavailableDateType(
                                                                                             UnavailableDateType.SINGLE_DATE)
                                                                                         .build())))
                                                                                 .build())
                                                       .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            LocalDateTime now = LocalDate.now().atTime(12, 0, 0);
            AboutToStartOrSubmitCallbackResponse response;
            try (MockedStatic<LocalDateTime> mock = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
                mock.when(LocalDateTime::now).thenReturn(now);
                response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            }

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getRespondent1().getUnavailableDates()).isEqualTo(
                wrapElements(List.of(UnavailableDate.builder()
                                         .eventAdded("Defendant Response Event")
                                         .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                         .dateAdded(now.toLocalDate())
                                         .date(LocalDate.of(2024, 2, 1))
                                         .build())));
        }

        @Test
        void shouldOnlyUpdateClaimStatus_whenDefendantResponseLangIsBilingual() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .totalClaimAmount(BigDecimal.valueOf(5000))
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(
                    "BOTH").build()).build())
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

        @Test
        void shouldAddEventAndDateAddedToRespondentExpertsAndWitnesses() {
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
            Expert expert = updatedCaseData.getRespondent1DQ().getRespondent1DQExperts().getDetails().get(0).getValue();
            Witness witness = updatedCaseData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails().get(0).getValue();

            assertThat(expert.getDateAdded()).isEqualTo(LocalDateTime.now().toLocalDate());
            assertThat(expert.getEventAdded()).isEqualTo(DEFENDANT_RESPONSE_EVENT.getValue());
            assertThat(witness.getDateAdded()).isEqualTo(LocalDateTime.now().toLocalDate());
            assertThat(witness.getEventAdded()).isEqualTo(DEFENDANT_RESPONSE_EVENT.getValue());
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
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(toggleEnabled);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimantBilingualLanguagePreference(claimantBilingualPreference)
                .caseDataLip(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage(defendantBilingualPreference)
                                                             .build())
                                 .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQLanguage(WelshLanguageRequirements.builder()
                                                              .documents(Language.valueOf(defendantDocumentLanguage))
                                                              .build())
                                   .build())
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
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
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
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build()).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            assertThat(updatedCaseData.getDefendantLanguagePreferenceDisplay()).isEqualTo(WELSH);
        }

        @Test
        void shouldUpdateLanguagePreferenceIfWelshDocsSpecified() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("ENGLISH").build()).build())
                .respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(WelshLanguageRequirements.builder().documents(Language.WELSH).build()).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedCaseData = getCaseData(response);

            assertThat(updatedCaseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1ResponseLanguage()).isEqualTo("WELSH");
            assertThat(updatedCaseData.getDefendantLanguagePreferenceDisplay()).isEqualTo(WELSH);
        }
    }
}

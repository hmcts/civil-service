package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.DocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToDefenceCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    UnavailableDateValidator.class,
    CaseDetailsConverter.class,
    LocationHelper.class,
    AssignCategoryId.class
})
class RespondToDefenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private Time time;

    @MockBean
    private LocationRefDataService locationRefDataService;

    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @MockBean
    private LocationRefDataUtil locationRefDataUtil;

    @Autowired
    private RespondToDefenceCallbackHandler handler;

    @Autowired
    private ExitSurveyContentService exitSurveyContentService;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private AssignCategoryId assignCategoryId;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private ToggleConfiguration toggleConfiguration;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPopulateClaimantResponseScenarioFlag_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("claimantResponseScenarioFlag")).isEqualTo("ONE_V_ONE");
        }

        @Test
        void shouldSetClaimantResponseScenarioFlagTo1V1_WhenAboutToStartIsInvokedAndMulitPatyScenariois1V1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("claimantResponseScenarioFlag"))
                .isEqualTo("ONE_V_ONE");
        }

        @Test
        void shouldSetClaimantResponseScenarioFlagTo2V1_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                .multiPartyClaimTwoApplicants()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("claimantResponseScenarioFlag"))
                .isEqualTo("TWO_V_ONE");
        }

        @Test
        void shouldSetClaimantResponseScenarioFlagTo1V2OneSol_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                .multiPartyClaimOneDefendantSolicitor()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("claimantResponseScenarioFlag"))
                .isEqualTo("ONE_V_TWO_ONE_LEGAL_REP");
        }

        @Test
        void shouldSetClaimantResponseScenarioFlagTo1V2TwoSol_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("claimantResponseScenarioFlag"))
                .isEqualTo("ONE_V_TWO_TWO_LEGAL_REP");
        }

        @Nested
        class OneVTwo {

            @Test
            void shouldSetRespondentSharedClaimResponseDocumentSameSolicitorScenario_WhenAboutToStartIsInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                    .respondent2(PartyBuilder.builder().individual().build())
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .defendantResponseDocuments(wrapElements(CaseDocument.builder()
                                                                 .documentType(DEFENDANT_DEFENCE)
                                                                 .documentLink(Document.builder()
                                                                                   .documentUrl("url")
                                                                                   .documentHash("hash")
                                                                                   .documentFileName("respondent defense")
                                                                                   .documentBinaryUrl("binUrl")
                                                                                   .build()).build()))
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                assertThat(response.getErrors()).isNull();
                assertThat(response.getData()).extracting("respondentSharedClaimResponseDocument").isNotNull();
            }

            @Test
            void shouldSetRespondent1ClaimResponseDocument_WhenAboutToStartIsInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceAfterNotifyClaimDetails().build().toBuilder()
                    .defendantResponseDocuments(wrapElements(CaseDocument.builder()
                                                                 .documentType(DEFENDANT_DEFENCE)
                                                                 .documentLink(Document.builder()
                                                                                   .documentUrl("url")
                                                                                   .documentHash("hash")
                                                                                   .documentFileName("respondent defense")
                                                                                   .documentBinaryUrl("binUrl")
                                                                                   .build()).build()))
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                assertThat(response.getErrors()).isNull();
                assertThat(response.getData()).extracting("respondent1ClaimResponseDocument").isNotNull();
            }

            @Test
            void shouldNotSetRespondentSharedClaimResponseDocumentDiffSolicitorScenario_WhenAboutToStartIsInvoked() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefenceAfterNotifyClaimDetails()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getErrors()).isNull();
                assertThat(response.getData().get("respondentSharedClaimResponseDocument")).isNull();
            }
        }
    }

    @Nested
    class MidEventCallbackValidateUnavailableDates {

        @Test
        void shouldReturnError_whenUnavailableDateIsMoreThanOneYearInFuture() {
            CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
            caseDataBuilder
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearing(Hearing.builder()
                                                           .unavailableDatesRequired(YES)
                                                           .unavailableDates(wrapElements(
                                                               UnavailableDate.builder()
                                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                                   .date(LocalDate.now().plusYears(5)).build()))
                                                           .build())
                                  .build())
                .build();

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsExactly("Dates must be within the next 12 months.");
        }

        @Test
        void shouldReturnError_whenUnavailableDateIsInPast() {
            CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
            caseDataBuilder
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearing(Hearing.builder()
                                                           .unavailableDatesRequired(YES)
                                                           .unavailableDates(wrapElements(
                                                               UnavailableDate.builder()
                                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                                   .date(LocalDate.now().minusYears(5)).build()))
                                                           .build())
                                  .build())
                .build();

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnNoError_whenUnavailableDateIsValid() {
            CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
            caseDataBuilder
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearing(Hearing.builder()
                                                           .unavailableDatesRequired(YES)
                                                           .unavailableDates(wrapElements(
                                                               UnavailableDate.builder()
                                                                   .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                                   .date(LocalDate.now().plusDays(5)).build()))
                                                           .build())
                                  .build())
                .build();

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenNoUnavailableDate() {
            CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
            caseDataBuilder
                .applicant1DQ(Applicant1DQ.builder().applicant1DQHearing(Hearing.builder().build()).build()).build();

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenUnavailableDatesNotRequired() {
            CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();
            Hearing hearing = Hearing.builder().unavailableDatesRequired(NO).build();
            caseDataBuilder
                .applicant1DQ(Applicant1DQ.builder().applicant1DQHearing(hearing).build()).build();

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventCallbackValidateWitnesses {

        private static final String PAGE_ID = "witnesses";

        @Test
        void shouldReturnError_whenWitnessRequiredAndNullDetails() {
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).build();
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder().applicant1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldReturnNoError_whenWitnessRequiredAndDetailsProvided() {
            List<Element<Witness>> testWitness = wrapElements(Witness.builder().name("test witness").build());
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).details(testWitness).build();
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder().applicant1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenWitnessRequiredAndDetailsProvidedAndRespondentFlagEnabled() {
            List<Element<Witness>> testWitness = wrapElements(Witness.builder().name("test witness").build());
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(YES).details(testWitness).build();
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder().applicant1DQWitnesses(witnesses).build())
                .applicant2DQ(Applicant2DQ.builder().applicant2DQWitnesses(witnesses).build())
                .enableRespondent2ResponseFlag()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenWitnessNotRequired() {
            Witnesses witnesses = Witnesses.builder().witnessesToAppear(NO).build();
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder().applicant1DQWitnesses(witnesses).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventCallbackValidateExperts {

        private static final String PAGE_ID = "experts";

        @Test
        void shouldReturnError_whenExpertRequiredAndNullDetails() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(YES)
                                                           .build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Expert details required");
        }

        @Test
        void shouldReturnNoError_whenExpertRequiredAndDetailsProvided() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(YES)
                                                           .details(wrapElements(Expert.builder()
                                                                                     .name("test expert").build()))
                                                           .build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenExpertRequiredAndDetailsProvidedInApplicant2() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(YES)
                                                           .details(wrapElements(Expert.builder()
                                                                                     .name("test expert").build()))
                                                           .build())
                                  .build())
                .applicant2DQ(Applicant2DQ.builder()
                                  .applicant2DQExperts(Experts.builder()
                                                           .expertRequired(YES)
                                                           .details(wrapElements(Expert.builder()
                                                                                     .name("test expert").build()))
                                                           .build())
                                  .build())
                .enableRespondent2ResponseFlag()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenExpertNotRequired() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(NO)
                                                           .build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidStatementOfTruth {

        @Test
        void shouldSetStatementOfTruthFieldsToNull_whenPopulated() {
            String name = "John Smith";
            String role = "Solicitor";

            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "statement-of-truth");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("uiStatementOfTruth")
                .doesNotHaveToString("name")
                .doesNotHaveToString("role");
        }
    }

    @Nested
    class AboutToSubmitCallback {
        private final LocalDateTime localDateTime = now();

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(localDateTime);
            given(toggleConfiguration.getFeatureToggle()).willReturn("WA 3.5");

            Address address = Address.builder()
                .postCode("E11 5BB")
                .build();
            CaseData oldCaseData = CaseDataBuilder.builder()
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .applicant2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .respondent1(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .respondent2(Party.builder().partyName("name").type(INDIVIDUAL).primaryAddress(address).build())
                .build();
            when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(oldCaseData);
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"},
            mode = EnumSource.Mode.INCLUDE)
        void shouldUpdateBusinessProcess_whenAtFullDefenceState(FlowState.Main flowState) {
            var params = callbackParamsOf(
                CaseDataBuilder.builder().atState(flowState).build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), CLAIMANT_RESPONSE.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
        }

        @Test
        void shouldUpdateBusinessProcess_whenAtFullDefenceState() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .build();
            var params = callbackParamsOf(
                caseData.toBuilder()
                    .applicant2(Party.builder()
                                    .companyName("company")
                                    .type(Party.Type.COMPANY)
                                    .build())
                    .addApplicant2(YES)
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQFileDirectionsQuestionnaire(
                                          caseData.getApplicant1DQ()
                                              .getApplicant1DQFileDirectionsQuestionnaire())
                                      .build())
                    .build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsEntry("applicant2ResponseDate", localDateTime.format(ISO_DATE_TIME));
            assertThat(response.getData()).extracting("applicant2DQStatementOfTruth").isNotNull();
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"},
            mode = EnumSource.Mode.INCLUDE)
        void shouldUpdateBusinessProcess_whenAtFullDefenceStateForSDO(FlowState.Main flowState) {
            var params = callbackParamsOf(
                CaseDataBuilder.builder().atState(flowState).build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), CLAIMANT_RESPONSE.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"},
            mode = EnumSource.Mode.INCLUDE)
        void shouldUpdateBusinessProcess_whenAtFullDefenceStateForSdoMP(FlowState.Main flowState) {

            var params = callbackParamsOf(
                CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                    .multiPartyClaimTwoDefendantSolicitorsForSdoMP().build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), CLAIMANT_RESPONSE.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
        }

        @Test
        void shouldAddPartyIdsToPartyFields_whenInvoked() {
            when(featureToggleService.isHmcEnabled()).thenReturn(true);

            var caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .build();

            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1").hasFieldOrProperty("partyID");
            assertThat(response.getData()).extracting("respondent1").hasFieldOrProperty("partyID");
        }

        @Test
        void shouldNotAddPartyIdsToPartyFields_whenInvokedWithHMCToggleOff() {
            when(featureToggleService.isHmcEnabled()).thenReturn(false);

            var objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            var caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .build();

            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1")
                .isEqualTo(objectMapper.convertValue(caseData.getApplicant1(), HashMap.class));
            assertThat(response.getData()).extracting("respondent1")
                .isEqualTo(objectMapper.convertValue(caseData.getRespondent1(), HashMap.class));
        }

        @Test
        void shouldAssembleClaimantResponseDocuments2v1ProceedBoth() {
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            var caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1(Party.builder().companyName("company").type(Party.Type.COMPANY).build())
                .applicant1DefenceResponseDocument(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def1.pdf").build())
                                                       .build())
                .claimantDefenceResDocToDefendant2(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def2.pdf").build())
                                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-1-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .addApplicant2(YesOrNo.YES)
                .applicant2DQ(Applicant2DQ.builder()
                                  .applicant2DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-2-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .build().toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourt("127").build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                                .build())
                .applicant1ProceedWithClaimMultiParty2v1(YES)
                .applicant2ProceedWithClaimMultiParty2v1(YES)
                .build();
            /*
            CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
             */
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(8, docs.size());

            assertThat(response.getData())
                .extracting("claimantResponseDocuments")
                .asString()
                .contains("createdBy=Claimant")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentSize=0")
                .contains("createdDatetime=2022-02-18T12:10:55")
                .contains("documentType=CLAIMANT_DEFENCE")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentName=claimant-response-def2.pdf")
                .contains("documentName=claimant-1-draft-dir.pdf")
                .contains("documentName=claimant-2-draft-dir.pdf")
                .contains("documentType=CLAIMANT_DRAFT_DIRECTIONS");
            assertThat(response.getState()).isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldAssembleClaimantResponseDocuments2v1ProceedOne() {
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            var caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1(Party.builder().companyName("company").type(Party.Type.COMPANY).build())
                .applicant1DefenceResponseDocument(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def1.pdf").build())
                                                       .build())
                .claimantDefenceResDocToDefendant2(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def2.pdf").build())
                                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-1-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .addApplicant2(YesOrNo.YES)
                .applicant2DQ(Applicant2DQ.builder()
                                  .applicant2DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-2-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .build().toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourt("127").build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                                .build())
                .applicant1ProceedWithClaimMultiParty2v1(NO)
                .applicant2ProceedWithClaimMultiParty2v1(YES)
                .build();
            /*
            CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
             */
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(8, docs.size());

            assertThat(response.getData())
                .extracting("claimantResponseDocuments")
                .asString()
                .contains("createdBy=Claimant")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentSize=0")
                .contains("createdDatetime=2022-02-18T12:10:55")
                .contains("documentType=CLAIMANT_DEFENCE")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentName=claimant-response-def2.pdf")
                .contains("documentName=claimant-1-draft-dir.pdf")
                .contains("documentName=claimant-2-draft-dir.pdf")
                .contains("documentType=CLAIMANT_DRAFT_DIRECTIONS");
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldAssembleClaimantResponseDocuments2v1NotProceed() {
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            var caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1(Party.builder().companyName("company").type(Party.Type.COMPANY).build())
                .applicant1DefenceResponseDocument(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def1.pdf").build())
                                                       .build())
                .claimantDefenceResDocToDefendant2(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def2.pdf").build())
                                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-1-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .addApplicant2(YesOrNo.YES)
                .applicant2DQ(Applicant2DQ.builder()
                                  .applicant2DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-2-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .build().toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourt("127").build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                                .build())
                .applicant1ProceedWithClaimMultiParty2v1(NO)
                .applicant2ProceedWithClaimMultiParty2v1(NO)
                .build();
            /*
            CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
             */
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(8, docs.size());

            assertThat(response.getData())
                .extracting("claimantResponseDocuments")
                .asString()
                .contains("createdBy=Claimant")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentSize=0")
                .contains("createdDatetime=2022-02-18T12:10:55")
                .contains("documentType=CLAIMANT_DEFENCE")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentName=claimant-response-def2.pdf")
                .contains("documentName=claimant-1-draft-dir.pdf")
                .contains("documentName=claimant-2-draft-dir.pdf")
                .contains("documentType=CLAIMANT_DRAFT_DIRECTIONS");
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldAssembleClaimantResponseDocuments1v1Proceed() {
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            var caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1(Party.builder().companyName("company").type(Party.Type.COMPANY).build())
                .applicant1DefenceResponseDocument(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def1.pdf").build())
                                                       .build())
                .claimantDefenceResDocToDefendant2(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def2.pdf").build())
                                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-1-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .build().toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourt("127").build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                                .build())
                .applicant1ProceedWithClaim(YES)
                .build();
            /*
            CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
             */
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(6, docs.size());

            assertThat(response.getData())
                .extracting("claimantResponseDocuments")
                .asString()
                .contains("createdBy=Claimant")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentSize=0")
                .contains("createdDatetime=2022-02-18T12:10:55")
                .contains("documentType=CLAIMANT_DEFENCE")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentName=claimant-response-def2.pdf")
                .contains("documentName=claimant-1-draft-dir.pdf")
                .contains("documentType=CLAIMANT_DRAFT_DIRECTIONS");
            assertThat(response.getState()).isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldAssembleClaimantResponseDocuments1v1NotProceed() {
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            var caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1(Party.builder().companyName("company").type(Party.Type.COMPANY).build())
                .applicant1DefenceResponseDocument(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def1.pdf").build())
                                                       .build())
                .claimantDefenceResDocToDefendant2(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def2.pdf").build())
                                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-1-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .build().toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourt("127").build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                                .build())
                .applicant1ProceedWithClaim(NO)
                .build();
            /*
            CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
             */
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(6, docs.size());

            assertThat(response.getData())
                .extracting("claimantResponseDocuments")
                .asString()
                .contains("createdBy=Claimant")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentSize=0")
                .contains("createdDatetime=2022-02-18T12:10:55")
                .contains("documentType=CLAIMANT_DEFENCE")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentName=claimant-response-def2.pdf")
                .contains("documentName=claimant-1-draft-dir.pdf")
                .contains("documentType=CLAIMANT_DRAFT_DIRECTIONS");
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldAssembleClaimantResponseDocuments1v2ssProceedBoth() {
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            var caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1(Party.builder().companyName("company").type(Party.Type.COMPANY).build())
                .applicant1DefenceResponseDocument(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def1.pdf").build())
                                                       .build())
                .claimantDefenceResDocToDefendant2(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def2.pdf").build())
                                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-1-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .respondent2(Party.builder().companyName("company 2")
                                 .type(Party.Type.COMPANY)
                                 .build())
                .respondentResponseIsSame(YesOrNo.YES)
                .build().toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourt("127").build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                                .build())
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YES)
                .build();
            /*
            CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
             */
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(6, docs.size());

            assertThat(response.getData())
                .extracting("claimantResponseDocuments")
                .asString()
                .contains("createdBy=Claimant")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentSize=0")
                .contains("createdDatetime=2022-02-18T12:10:55")
                .contains("documentType=CLAIMANT_DEFENCE")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentName=claimant-response-def2.pdf")
                .contains("documentName=claimant-1-draft-dir.pdf")
                .contains("documentType=CLAIMANT_DRAFT_DIRECTIONS");
            assertThat(response.getState()).isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldAssembleClaimantResponseDocuments1v2ssProceedOne() {
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            var caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1(Party.builder().companyName("company").type(Party.Type.COMPANY).build())
                .applicant1DefenceResponseDocument(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def1.pdf").build())
                                                       .build())
                .claimantDefenceResDocToDefendant2(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def2.pdf").build())
                                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-1-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .respondent2(Party.builder().companyName("company 2")
                                 .type(Party.Type.COMPANY)
                                 .build())
                .respondentResponseIsSame(YesOrNo.YES)
                .build().toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourt("127").build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                                .build())
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO)
                .build();
            /*
            CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
             */
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(6, docs.size());

            assertThat(response.getData())
                .extracting("claimantResponseDocuments")
                .asString()
                .contains("createdBy=Claimant")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentSize=0")
                .contains("createdDatetime=2022-02-18T12:10:55")
                .contains("documentType=CLAIMANT_DEFENCE")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentName=claimant-response-def2.pdf")
                .contains("documentName=claimant-1-draft-dir.pdf")
                .contains("documentType=CLAIMANT_DRAFT_DIRECTIONS");
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldAssembleClaimantResponseDocuments1v2ssNotProceed() {
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            var caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1(Party.builder().companyName("company").type(Party.Type.COMPANY).build())
                .applicant1DefenceResponseDocument(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def1.pdf").build())
                                                       .build())
                .claimantDefenceResDocToDefendant2(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def2.pdf").build())
                                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-1-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .respondent2(Party.builder().companyName("company 2")
                                 .type(Party.Type.COMPANY)
                                 .build())
                .respondentResponseIsSame(YesOrNo.YES)
                .build().toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourt("127").build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                                .build())
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO)
                .build();
            /*
            CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
             */
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(6, docs.size());

            assertThat(response.getData())
                .extracting("claimantResponseDocuments")
                .asString()
                .contains("createdBy=Claimant")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentSize=0")
                .contains("createdDatetime=2022-02-18T12:10:55")
                .contains("documentType=CLAIMANT_DEFENCE")
                .contains("documentName=claimant-response-def1.pdf")
                .contains("documentName=claimant-response-def2.pdf")
                .contains("documentName=claimant-1-draft-dir.pdf")
                .contains("documentType=CLAIMANT_DRAFT_DIRECTIONS");
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldAssignCategoryId_whenInvoked() {
            // Given
            when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            var caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1(Party.builder().companyName("company").type(Party.Type.COMPANY).build())
                .applicant1DefenceResponseDocument(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def1.pdf").build())
                                                       .build())
                .claimantDefenceResDocToDefendant2(ResponseDocument.builder()
                                                       .file(DocumentBuilder.builder().documentName(
                                                           "claimant-response-def2.pdf").build())
                                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-1-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .applicant2DQ(Applicant2DQ.builder()
                                  .applicant2DQDraftDirections(DocumentBuilder.builder().documentName(
                                          "claimant-2-draft-dir.pdf")
                                                                   .build())
                                  .build())
                .build().toBuilder()
                .courtLocation(CourtLocation.builder().applicantPreferredCourt("127").build())
                .claimValue(ClaimValue.builder()
                                .statementOfValueInPennies(BigDecimal.valueOf(1000_00))
                                .build())
                .build();
            //When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            //Then
            assertThat(updatedData.getClaimantResponseDocuments().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo("directionsQuestionnaire");
            assertThat(updatedData.getClaimantResponseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo("directionsQuestionnaire");
            assertThat(updatedData.getClaimantResponseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo("directionsQuestionnaire");
            assertThat(updatedData.getClaimantResponseDocuments().get(3).getValue().getDocumentLink().getCategoryID()).isEqualTo("directionsQuestionnaire");
            assertThat(updatedData.getClaimantResponseDocuments().get(4).getValue().getDocumentLink().getCategoryID()).isEqualTo("DQApplicant");
            assertThat(updatedData.getClaimantResponseDocuments().get(5).getValue().getDocumentLink().getCategoryID()).isEqualTo("DQApplicant");
            assertThat(updatedData.getClaimantResponseDocuments().get(6).getValue().getDocumentLink().getCategoryID()).isEqualTo("DQApplicant");
            assertThat(updatedData.getClaimantResponseDocuments().get(7).getValue().getDocumentLink().getCategoryID()).isEqualTo("DQApplicant");

        }

        @Nested
        class UpdateRequestedCourt {

            @Test
            void updateApplicant1DQRequestedCourt() {
                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                    .courtLocation()
                    .build();
                when(locationRefDataUtil.getPreferredCourtData(any(), any(), eq(true))).thenReturn("127");
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

                System.out.println(response.getData());

                assertThat(response.getData()).extracting("applicant1DQRequestedCourt")
                    .extracting("responseCourtCode")
                    .isEqualTo("127");

                assertThat(response.getData()).extracting("applicant1DQRequestedCourt")
                    .extracting("caseLocation")
                    .extracting("region", "baseLocation")
                    .containsExactly("2", "000000");
            }

            @Test
            void updateApplicant1DQRequestedCourtWhenNoCourtLocationIsReturnedByRefData() {
                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                    .courtLocation()
                    .build();
                List<LocationRefData> locations = new ArrayList<>();
                locations.add(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                                  .courtName("Court Name").region("Region").regionId("regionId1").courtVenueId("000")
                                  .courtTypeId("10")
                                  .epimmsId("4532").build());
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

                System.out.println(response.getData());

                assertThat(response.getData()).extracting("applicant1DQRequestedCourt")
                    .extracting("responseCourtCode")
                    .isEqualTo(null);
            }
        }

        @Nested
        class ResetStatementOfTruth {

            @Test
            void shouldAddUiStatementOfTruthToApplicantStatementOfTruth_whenInvoked() {
                String name = "John Smith";
                String role = "Solicitor";

                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build()
                    .toBuilder()
                    .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(
                        caseData,
                        ABOUT_TO_SUBMIT
                    ));

                assertThat(response.getData())
                    .extracting("applicant1DQStatementOfTruth")
                    .extracting("name", "role")
                    .containsExactly("John Smith", "Solicitor");

                assertThat(response.getData())
                    .extracting("uiStatementOfTruth")
                    .doesNotHaveToString("name")
                    .doesNotHaveToString("role");
            }
        }

        @Nested
        class OneVTwo {
            @Test
            void shouldRemoveRespondentSharedClaimResponseDocument_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .build();

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(
                        caseData.toBuilder().respondentSharedClaimResponseDocument(
                            caseData.getRespondent1ClaimResponseDocument()).build(), ABOUT_TO_SUBMIT
                    ));

                assertThat(response.getData().get("respondentSharedClaimResponseDocument")).isNull();
            }
        }
    }

    @Nested
    class SubmittedCallback {

        @Nested
        class OneVOne {
            @Test
            void shouldReturnExpectedResponse_whenApplicantIsProceedingWithClaim() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# You have chosen to proceed with the claim%n## Claim number: 000DC001"))
                        .confirmationBody(format(
                            "<br />We will review the case and contact you to tell you what to do next.%n%n"
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }

            @Test
            void shouldReturnExpectedResponse_whenApplicantIsNotProceedingWithClaim() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndNotProceed()
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# You have chosen not to proceed with the claim%n## Claim number:"
                                                       + " 000DC001"))
                        .confirmationBody(
                            "<br />If you do want to proceed you need to do it within:"
                                + "<ul><li>14 days if the claim is allocated to a small claims track</li>"
                                + "<li>28 days if the claim is allocated to a fast or multi track</li></ul>"
                                + "<p>The case will be stayed if you do not proceed within the allowed "
                                + "timescale.</p>" + exitSurveyContentService.applicantSurvey())
                        .build());
            }
        }

        @Nested
        class OneVTwo {
            @Test
            void shouldReturnExpectedResponse_whenApplicantsIsProceedingWithClaimAgainstBothDefendants() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# You have chosen to proceed with the claim%n## Claim number: 000DC001"))
                        .confirmationBody(format(
                            "<br />We will review the case and contact you to tell you what to do next.%n%n"
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }

            @Test
            void shouldReturnExpectedResponse_whenApplicantIsNotProceedingWithClaimAgainstBothDefendants() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndNotProceed_1v2()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# You have chosen not to proceed with the claim%n## Claim number:"
                                                       + " 000DC001"))
                        .confirmationBody(
                            "<br />If you do want to proceed you need to do it within:"
                                + "<ul><li>14 days if the claim is allocated to a small claims track</li>"
                                + "<li>28 days if the claim is allocated to a fast or multi track</li></ul>"
                                + "<p>The case will be stayed if you do not proceed within the allowed "
                                + "timescale.</p>" + exitSurveyContentService.applicantSurvey())
                        .build());
            }

            @Test
            void shouldReturnExpectedResponse_whenApplicantIsProceedingWithClaimAgainstFirstDefendantOnly() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                    .multiPartyClaimOneDefendantSolicitor()
                    .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# You have chosen to proceed with the claim against one defendant only%n"
                                + "## Claim number: 000DC001"))
                        .confirmationBody(format(
                            "<br />We will review the case and contact you to tell you what to do next.%n%n"
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }

            @Test
            void shouldReturnExpectedResponse_whenApplicantIsProceedingWithClaimAgainstSecondDefendantOnly() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                    .multiPartyClaimOneDefendantSolicitor()
                    .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# You have chosen to proceed with the claim against one defendant only%n"
                                + "## Claim number: 000DC001"))
                        .confirmationBody(format(
                            "<br />We will review the case and contact you to tell you what to do next.%n%n"
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }
        }

        @Nested
        class TwoVOne {
            @Test
            void shouldReturnExpectedResponse_whenBothApplicantsAreProceedingWithClaim() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                    .multiPartyClaimTwoApplicants()
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# You have chosen to proceed with the claim%n## Claim number: 000DC001"))
                        .confirmationBody(format(
                            "<br />We will review the case and contact you to tell you what to do next.%n%n"
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }

            @Test
            void shouldReturnExpectedResponse_whenBothApplicantAreNotProceedingWithClaim() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndNotProceed_2v1()
                    .multiPartyClaimTwoApplicants()
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# You have chosen not to proceed with the claim%n## Claim number:"
                                                       + " 000DC001"))
                        .confirmationBody(
                            "<br />If you do want to proceed you need to do it within:"
                                + "<ul><li>14 days if the claim is allocated to a small claims track</li>"
                                + "<li>28 days if the claim is allocated to a fast or multi track</li></ul>"
                                + "<p>The case will be stayed if you do not proceed within the allowed "
                                + "timescale.</p>" + exitSurveyContentService.applicantSurvey())
                        .build());
            }

            @Test
            void shouldReturnExpectedResponse_whenOnlyFirstApplicantIsProceedingWithClaim() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                    .multiPartyClaimTwoApplicants()
                    .applicant1ProceedWithClaimMultiParty2v1(YES)
                    .applicant2ProceedWithClaimMultiParty2v1(NO)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# You have chosen to proceed with the claim against one defendant only%n"
                                + "## Claim number: 000DC001"))
                        .confirmationBody(format(
                            "<br />We will review the case and contact you to tell you what to do next.%n%n"
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }

            @Test
            void shouldReturnExpectedResponse_whenOnlySecondApplicantIsProceedingWithClaim() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                    .multiPartyClaimTwoApplicants()
                    .applicant1ProceedWithClaimMultiParty2v1(NO)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format(
                            "# You have chosen to proceed with the claim against one defendant only%n"
                                + "## Claim number: 000DC001"))
                        .confirmationBody(format(
                            "<br />We will review the case and contact you to tell you what to do next.%n%n"
                        ) + exitSurveyContentService.applicantSurvey())
                        .build());
            }
        }

    }

    @Nested
    class MidSetApplicantsProceedIntention {

        @Test
        void shouldSetToYes_whenApplicant1IntendToProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1ProceedWithClaim(YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "set-applicants-proceed-intention");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicantsProceedIntention").isEqualTo("Yes");
        }

        @Test
        void shouldSetToYes_whenApplicant2IntendToProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant2ProceedWithClaimMultiParty2v1(YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "set-applicants-proceed-intention");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicantsProceedIntention").isEqualTo("Yes");
        }

        @Test
        void shouldSetToNo_whenApplicant1OrApplicant2DoNotIntendToProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1ProceedWithClaim(NO)
                .applicant2ProceedWithClaimMultiParty2v1(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "set-applicants-proceed-intention");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicantsProceedIntention").isEqualTo("No");
        }
    }
}

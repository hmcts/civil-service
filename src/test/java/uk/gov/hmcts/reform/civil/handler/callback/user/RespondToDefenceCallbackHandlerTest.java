package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
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
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
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
    AssignCategoryId.class,
    FrcDocumentsUtils.class
})
class RespondToDefenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private Time time;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

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

    @Autowired
    private FrcDocumentsUtils frcDocumentsUtils;

    @MockBean
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    @Mock
    private UnavailableDateValidator unavailableDateValidator;

    @Mock
    private LocationHelper locationHelper;

    @MockBean
    private RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;

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
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceAfterNotifyClaimDetails().build();
                caseData.setRespondent2(PartyBuilder.builder().individual().build());
                caseData.setAddRespondent2(YES);
                caseData.setRespondent2(PartyBuilder.builder().individual().build());
                caseData.setRespondent2SameLegalRepresentative(YES);
                Document document = new Document();
                document.setDocumentUrl("url");
                document.setDocumentHash("hash");
                document.setDocumentFileName("respondent defense");
                document.setDocumentBinaryUrl("binUrl");
                CaseDocument caseDocument = new CaseDocument();
                caseDocument.setCreatedBy("Defendant");
                caseDocument.setDocumentType(DEFENDANT_DEFENCE);
                caseDocument.setDocumentLink(document);
                caseData.setDefendantResponseDocuments(wrapElements(caseDocument));
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                assertThat(response.getErrors()).isNull();
                assertThat(response.getData()).extracting("respondentSharedClaimResponseDocument").isNotNull();
            }

            @Test
            void shouldSetRespondent1ClaimResponseDocument_WhenAboutToStartIsInvoked() {
                Document document = new Document();
                document.setDocumentUrl("url");
                document.setDocumentHash("hash");
                document.setDocumentFileName("respondent defense");
                document.setDocumentBinaryUrl("binUrl");
                CaseDocument caseDocument = new CaseDocument();
                caseDocument.setCreatedBy("Defendant");
                caseDocument.setDocumentType(DEFENDANT_DEFENCE);
                caseDocument.setDocumentLink(document);
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceAfterNotifyClaimDetails().build();
                caseData.setDefendantResponseDocuments(wrapElements(caseDocument));
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                assertThat(response.getErrors()).isNull();
                assertThat(response.getData()).extracting("respondent1ClaimResponseDocument").isNotNull();
            }

            @Test
            void shouldSetRespondent2ClaimResponseDocument_WhenAboutToStartIsInvoked() {
                Document document = new Document();
                document.setDocumentUrl("url");
                document.setDocumentHash("hash");
                document.setDocumentFileName("respondent defense");
                document.setDocumentBinaryUrl("binUrl");
                CaseDocument caseDocument = new CaseDocument();
                caseDocument.setCreatedBy("Defendant 2");
                caseDocument.setDocumentType(DEFENDANT_DEFENCE);
                caseDocument.setDocumentLink(document);
                CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceAfterNotifyClaimDetails().build();
                caseData.setDefendantResponseDocuments(wrapElements(caseDocument));
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                assertThat(response.getErrors()).isNull();
                assertThat(response.getData()).extracting("respondent2ClaimResponseDocument").isNotNull();
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
            UnavailableDate unavailableDate = new UnavailableDate();
            unavailableDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate.setDate(LocalDate.now().plusYears(5));
            Hearing hearing = new Hearing();
            hearing.setUnavailableDatesRequired(YES);
            hearing.setUnavailableDates(wrapElements(unavailableDate));
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQHearing(hearing);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1DQ(applicant1DQ);

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsExactly("Dates must be within the next 12 months.");
        }

        @Test
        void shouldReturnError_whenUnavailableDateIsInPast() {
            UnavailableDate unavailableDate = new UnavailableDate();
            unavailableDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate.setDate(LocalDate.now().minusYears(5));
            Hearing hearing = new Hearing();
            hearing.setUnavailableDatesRequired(YES);
            hearing.setUnavailableDates(wrapElements(unavailableDate));
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQHearing(hearing);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1DQ(applicant1DQ);

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnNoError_whenUnavailableDateIsValid() {
            UnavailableDate unavailableDate = new UnavailableDate();
            unavailableDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            unavailableDate.setDate(LocalDate.now().plusDays(5));
            Hearing hearing = new Hearing();
            hearing.setUnavailableDatesRequired(YES);
            hearing.setUnavailableDates(wrapElements(unavailableDate));
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQHearing(hearing);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1DQ(applicant1DQ);

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenNoUnavailableDate() {
            CaseData caseData = CaseDataBuilder.builder().build();
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQHearing(new Hearing());
            caseData.setApplicant1DQ(applicant1DQ);

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenUnavailableDatesNotRequired() {
            CaseData caseData = CaseData.builder().build();
            Hearing hearing = new Hearing();
            hearing.setUnavailableDatesRequired(NO);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQHearing(hearing);
            caseData.setApplicant1DQ(applicant1DQ);

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-unavailable-dates");

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
            Witnesses witnesses = new Witnesses();
            witnesses.setWitnessesToAppear(YES);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQWitnesses(witnesses);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Witness details required");
        }

        @Test
        void shouldReturnNoError_whenWitnessRequiredAndDetailsProvided() {
            Witness witness = new Witness();
            witness.setName("test witness");
            List<Element<Witness>> testWitness = wrapElements(witness);
            Witnesses witnesses = new Witnesses();
            witnesses.setWitnessesToAppear(YES);
            witnesses.setDetails(testWitness);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQWitnesses(witnesses);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenWitnessRequiredAndDetailsProvidedAndRespondentFlagEnabled() {
            Witness witness = new Witness();
            witness.setName("test witness");
            List<Element<Witness>> testWitness = wrapElements(witness);
            Witnesses witnesses = new Witnesses();
            witnesses.setWitnessesToAppear(YES);
            witnesses.setDetails(testWitness);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQWitnesses(witnesses);
            Applicant2DQ applicant2DQ = new Applicant2DQ();
            applicant2DQ.setApplicant2DQWitnesses(witnesses);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
                .applicant2DQ(applicant2DQ)
                .enableRespondent2ResponseFlag()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenWitnessNotRequired() {
            Witnesses witnesses = new Witnesses();
            witnesses.setWitnessesToAppear(NO);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQWitnesses(witnesses);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
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
            Experts experts = new Experts();
            experts.setExpertRequired(YES);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQExperts(experts);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly("Expert details required");
        }

        @Test
        void shouldReturnNoError_whenExpertRequiredAndDetailsProvided() {
            Expert expert = new Expert();
            expert.setName("test expert");
            Experts experts = new Experts();
            experts.setExpertRequired(YES);
            experts.setDetails(wrapElements(expert));
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQExperts(experts);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenExpertRequiredAndDetailsProvidedInApplicant2() {
            Expert expert = new Expert();
            expert.setName("test expert");
            Experts experts = new Experts();
            experts.setExpertRequired(YES);
            experts.setDetails(wrapElements(expert));
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQExperts(experts);
            Applicant2DQ applicant2DQ = new Applicant2DQ();
            applicant2DQ.setApplicant2DQExperts(experts);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
                .applicant2DQ(applicant2DQ)
                .enableRespondent2ResponseFlag()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenExpertNotRequired() {
            Experts experts = new Experts();
            experts.setExpertRequired(NO);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQExperts(experts);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(applicant1DQ)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidStatementOfTruth {

        @Test
        void shouldSetEmptyCallbackResponse_whenStatementOfTruthMidEventIsCalled() {
            String name = "John Smith";
            String role = "Solicitor";

            StatementOfTruth statementOfTruth = new StatementOfTruth();
            statementOfTruth.setName(name);
            statementOfTruth.setRole(role);
            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(statementOfTruth)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "statement-of-truth");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        private final LocalDateTime localDateTime = now();

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(localDateTime);
            given(toggleConfiguration.getFeatureToggle()).willReturn("WA 3.5");

            Address address = new Address();
            address.setPostCode("E11 5BB");
            Party party = new Party();
            party.setPartyName("name");
            party.setType(INDIVIDUAL);
            party.setPrimaryAddress(address);
            CaseData oldCaseData = CaseDataBuilder.builder()
                .applicant1(party)
                .applicant2(party)
                .respondent1(party)
                .respondent2(party)
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
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            caseData.setApplicant2(party);
            caseData.setAddApplicant2(YES);
            Applicant2DQ applicant2DQ = new Applicant2DQ();
            applicant2DQ.setApplicant2DQFileDirectionsQuestionnaire(
                caseData.getApplicant1DQ()
                    .getApplicant1DQFileDirectionsQuestionnaire());
            caseData.setApplicant2DQ(applicant2DQ);
            var params = callbackParamsOf(
                caseData,
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
            var caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .build();

            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1").hasFieldOrProperty("partyID");
            assertThat(response.getData()).extracting("respondent1").hasFieldOrProperty("partyID");
        }

        @Test
        void shouldAssembleClaimantResponseDocuments2v1ProceedBoth() {
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            Party party1 = new Party();
            party1.setPartyName("name");
            party1.setType(INDIVIDUAL);
            var caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(party1);
            caseData.setRespondent1(party);
            ResponseDocument responseDocument = new ResponseDocument();
            responseDocument.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def1.pdf").build());
            ResponseDocument  responseDocument1 = new ResponseDocument();
            responseDocument1.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def2.pdf").build());
            caseData.setApplicant1DefenceResponseDocument(responseDocument);
            caseData.setClaimantDefenceResDocToDefendant2(responseDocument1);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                    "claimant-1-draft-dir.pdf").build());
            caseData.setApplicant1DQ(applicant1DQ);
            caseData.setAddApplicant2(YesOrNo.YES);
            Applicant2DQ applicant2DQ = new Applicant2DQ();
            applicant2DQ.setApplicant2DQDraftDirections(DocumentBuilder.builder().documentName(
                    "claimant-2-draft-dir.pdf").build());
            caseData.setApplicant2DQ(applicant2DQ);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setApplicantPreferredCourt("127");
            caseData.setCourtLocation(courtLocation);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(1000_00));
            caseData.setClaimValue(claimValue);
            caseData.setApplicant1ProceedWithClaimMultiParty2v1(YES);
            caseData.setApplicant2ProceedWithClaimMultiParty2v1(YES);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("00000");
            caseLocationCivil.setRegion("4");
            caseData.setCaseManagementLocation(caseLocationCivil);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(4, docs.size());

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
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            Party party1 = new Party();
            party1.setPartyName("name");
            party1.setType(INDIVIDUAL);
            var caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(party1);
            caseData.setRespondent1(party);
            ResponseDocument responseDocument = new ResponseDocument();
            responseDocument.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def1.pdf").build());
            ResponseDocument  responseDocument1 = new ResponseDocument();
            responseDocument1.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def2.pdf").build());
            caseData.setApplicant1DefenceResponseDocument(responseDocument);
            caseData.setClaimantDefenceResDocToDefendant2(responseDocument1);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-1-draft-dir.pdf").build());
            caseData.setApplicant1DQ(applicant1DQ);
            caseData.setAddApplicant2(YesOrNo.YES);
            Applicant2DQ applicant2DQ = new Applicant2DQ();
            applicant2DQ.setApplicant2DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-2-draft-dir.pdf").build());
            caseData.setApplicant2DQ(applicant2DQ);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setApplicantPreferredCourt("127");
            caseData.setCourtLocation(courtLocation);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(1000_00));
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            caseData.setApplicant1ProceedWithClaimMultiParty2v1(NO);
            caseData.setApplicant2ProceedWithClaimMultiParty2v1(YES);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("00000");
            caseLocationCivil.setRegion("4");
            caseData.setCaseManagementLocation(caseLocationCivil);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(4, docs.size());

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
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            Party party1 = new Party();
            party1.setPartyName("name");
            party1.setType(INDIVIDUAL);
            var caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(party1);
            caseData.setRespondent1(party);
            ResponseDocument responseDocument = new ResponseDocument();
            responseDocument.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def1.pdf").build());
            ResponseDocument  responseDocument1 = new ResponseDocument();
            responseDocument1.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def2.pdf").build());
            caseData.setApplicant1DefenceResponseDocument(responseDocument);
            caseData.setClaimantDefenceResDocToDefendant2(responseDocument1);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-1-draft-dir.pdf").build());
            caseData.setApplicant1DQ(applicant1DQ);
            caseData.setAddApplicant2(YesOrNo.YES);
            Applicant2DQ applicant2DQ = new Applicant2DQ();
            applicant2DQ.setApplicant2DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-2-draft-dir.pdf").build());
            caseData.setApplicant2DQ(applicant2DQ);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setApplicantPreferredCourt("127");
            caseData.setCourtLocation(courtLocation);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(1000_00));
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            caseData.setApplicant1ProceedWithClaimMultiParty2v1(NO);
            caseData.setApplicant2ProceedWithClaimMultiParty2v1(NO);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("00000");
            caseLocationCivil.setRegion("4");
            caseData.setCaseManagementLocation(caseLocationCivil);

            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(4, docs.size());

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
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            Party party1 = new Party();
            party1.setPartyName("name");
            party1.setType(INDIVIDUAL);
            var caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(party1);
            caseData.setRespondent1(party);
            ResponseDocument responseDocument = new ResponseDocument();
            responseDocument.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def1.pdf").build());
            ResponseDocument  responseDocument1 = new ResponseDocument();
            responseDocument1.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def2.pdf").build());
            caseData.setApplicant1DefenceResponseDocument(responseDocument);
            caseData.setClaimantDefenceResDocToDefendant2(responseDocument1);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-1-draft-dir.pdf").build());
            caseData.setApplicant1DQ(applicant1DQ);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setApplicantPreferredCourt("127");
            caseData.setCourtLocation(courtLocation);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(1000_00));
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            caseData.setApplicant1ProceedWithClaim(YES);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("00000");
            caseLocationCivil.setRegion("4");
            caseData.setCaseManagementLocation(caseLocationCivil);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(3, docs.size());

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
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            Party party1 = new Party();
            party1.setPartyName("name");
            party1.setType(INDIVIDUAL);
            var caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(party1);
            caseData.setRespondent1(party);
            ResponseDocument responseDocument = new ResponseDocument();
            responseDocument.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def1.pdf").build());
            ResponseDocument  responseDocument1 = new ResponseDocument();
            responseDocument1.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def2.pdf").build());
            caseData.setApplicant1DefenceResponseDocument(responseDocument);
            caseData.setClaimantDefenceResDocToDefendant2(responseDocument1);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-1-draft-dir.pdf").build());
            caseData.setApplicant1DQ(applicant1DQ);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setApplicantPreferredCourt("127");
            caseData.setCourtLocation(courtLocation);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(1000_00));
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            caseData.setApplicant1ProceedWithClaim(NO);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("00000");
            caseLocationCivil.setRegion("4");
            caseData.setCaseManagementLocation(caseLocationCivil);

            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(3, docs.size());

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
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            Party party1 = new Party();
            party1.setPartyName("name");
            party1.setType(INDIVIDUAL);
            var caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(party1);
            caseData.setRespondent1(party);
            ResponseDocument responseDocument = new ResponseDocument();
            responseDocument.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def1.pdf").build());
            ResponseDocument  responseDocument1 = new ResponseDocument();
            responseDocument1.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def2.pdf").build());
            caseData.setApplicant1DefenceResponseDocument(responseDocument);
            caseData.setClaimantDefenceResDocToDefendant2(responseDocument1);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-1-draft-dir.pdf").build());
            caseData.setApplicant1DQ(applicant1DQ);
            Party party3 = new Party();
            party3.setCompanyName("company 2");
            party3.setType(COMPANY);
            caseData.setRespondent2(party3);
            caseData.setRespondentResponseIsSame(YesOrNo.YES);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setApplicantPreferredCourt("127");
            caseData.setCourtLocation(courtLocation);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(1000_00));
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            caseData.setApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES);
            caseData.setApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YES);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("00000");
            caseLocationCivil.setRegion("4");
            caseData.setCaseManagementLocation(caseLocationCivil);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(3, docs.size());

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
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            Party party1 = new Party();
            party1.setPartyName("name");
            party1.setType(INDIVIDUAL);
            var caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(party1);
            caseData.setRespondent1(party);
            ResponseDocument responseDocument = new ResponseDocument();
            responseDocument.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def1.pdf").build());
            ResponseDocument  responseDocument1 = new ResponseDocument();
            responseDocument1.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def2.pdf").build());
            caseData.setApplicant1DefenceResponseDocument(responseDocument);
            caseData.setClaimantDefenceResDocToDefendant2(responseDocument1);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-1-draft-dir.pdf").build());
            caseData.setApplicant1DQ(applicant1DQ);
            Party party3 = new Party();
            party3.setCompanyName("company 2");
            party3.setType(COMPANY);
            caseData.setRespondent2(party3);
            caseData.setRespondentResponseIsSame(YesOrNo.YES);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setApplicantPreferredCourt("127");
            caseData.setCourtLocation(courtLocation);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(1000_00));
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            caseData.setApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES);
            caseData.setApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("00000");
            caseLocationCivil.setRegion("4");
            caseData.setCaseManagementLocation(caseLocationCivil);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(3, docs.size());

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
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            Party party1 = new Party();
            party1.setPartyName("name");
            party1.setType(INDIVIDUAL);
            var caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(party1);
            caseData.setRespondent1(party);
            ResponseDocument responseDocument = new ResponseDocument();
            responseDocument.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def1.pdf").build());
            ResponseDocument  responseDocument1 = new ResponseDocument();
            responseDocument1.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def2.pdf").build());
            caseData.setApplicant1DefenceResponseDocument(responseDocument);
            caseData.setClaimantDefenceResDocToDefendant2(responseDocument1);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-1-draft-dir.pdf").build());
            caseData.setApplicant1DQ(applicant1DQ);
            Party party3 = new Party();
            party3.setCompanyName("company 2");
            party3.setType(COMPANY);
            caseData.setRespondent2(party3);
            caseData.setRespondentResponseIsSame(YesOrNo.YES);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setApplicantPreferredCourt("127");
            caseData.setCourtLocation(courtLocation);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(1000_00));
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            caseData.setApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO);
            caseData.setApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("00000");
            caseLocationCivil.setRegion("4");
            caseData.setCaseManagementLocation(caseLocationCivil);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            @SuppressWarnings("unchecked")
            List<CaseDocument> docs = (ArrayList<CaseDocument>) response.getData().get("claimantResponseDocuments");
            assertEquals(3, docs.size());

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
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            Party party = new Party();
            party.setCompanyName("company");
            party.setType(COMPANY);
            Party party1 = new Party();
            party1.setPartyName("name");
            party1.setType(INDIVIDUAL);
            var caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(party1);
            caseData.setRespondent1(party);
            ResponseDocument responseDocument = new ResponseDocument();
            responseDocument.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def1.pdf").build());
            ResponseDocument  responseDocument1 = new ResponseDocument();
            responseDocument1.setFile(DocumentBuilder.builder().documentName(
                "claimant-response-def2.pdf").build());
            caseData.setApplicant1DefenceResponseDocument(responseDocument);
            caseData.setClaimantDefenceResDocToDefendant2(responseDocument1);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQDraftDirections(DocumentBuilder.builder().documentName(
                "claimant-1-draft-dir.pdf").build());
            caseData.setApplicant1DQ(applicant1DQ);
            Applicant2DQ applicant2DQ = new Applicant2DQ();
            applicant2DQ.setApplicant2DQDraftDirections(DocumentBuilder.builder().documentName(
                    "claimant-2-draft-dir.pdf").build());
            caseData.setApplicant2DQ(applicant2DQ);
            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setApplicantPreferredCourt("127");
            caseData.setCourtLocation(courtLocation);
            ClaimValue claimValue = new ClaimValue();
            claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(1000_00));
            caseData.setCourtLocation(courtLocation);
            caseData.setClaimValue(claimValue);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("00000");
            caseLocationCivil.setRegion("4");
            caseData.setCaseManagementLocation(caseLocationCivil);
            //When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            System.out.println(updatedData.getClaimantResponseDocuments());
            //Then
            assertThat(updatedData.getClaimantResponseDocuments().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "directionsQuestionnaire");
            assertThat(updatedData.getClaimantResponseDocuments().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "directionsQuestionnaire");
            assertThat(updatedData.getClaimantResponseDocuments().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "directionsQuestionnaire");
            assertThat(updatedData.getClaimantResponseDocuments().get(3).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "directionsQuestionnaire");
            assertThat(updatedData.getDuplicateClaimantDefendantResponseDocs().get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQApplicant");
            assertThat(updatedData.getDuplicateClaimantDefendantResponseDocs().get(1).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQApplicant");
            assertThat(updatedData.getDuplicateClaimantDefendantResponseDocs().get(2).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQApplicant");
            assertThat(updatedData.getDuplicateClaimantDefendantResponseDocs().get(3).getValue().getDocumentLink().getCategoryID()).isEqualTo(
                "DQApplicant");

        }

        @Test
        void shouldAssignCategoryId_frc_whenInvoked() {
            // Given
            when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

            var caseData = CaseDataBuilder.builder()
                .setIntermediateTrackClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.TWO_V_ONE)
                .multiPartyClaimTwoApplicants()
                .applicant1DQWithFixedRecoverableCostsIntermediate()
                .build();
            //When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            System.out.println(updatedData.getClaimantResponseDocuments());
            //Then
            assertThat(updatedData.getApplicant1DQ().getApplicant1DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument().getCategoryID()).isEqualTo(
                "DQApplicant");
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
                LocationRefData locationRefData = new LocationRefData();
                locationRefData.setSiteName("SiteName");
                locationRefData.setCourtAddress("1");
                locationRefData.setPostcode("1");
                locationRefData.setCourtName("Court Name");
                locationRefData.setRegion("Region");
                locationRefData.setRegionId("regionId1");
                locationRefData.setCourtVenueId("000");
                locationRefData.setCourtTypeId("10");
                locationRefData.setEpimmsId("4532");
                List<LocationRefData> locations = new ArrayList<>();
                locations.add(locationRefData);
                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                    .courtLocation()
                    .build();
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

                CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
                StatementOfTruth statementOfTruth = new StatementOfTruth();
                statementOfTruth.setName(name);
                statementOfTruth.setRole(role);
                caseData.setUiStatementOfTruth(statementOfTruth);

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
                caseData.setRespondentSharedClaimResponseDocument(
                    caseData.getRespondent1ClaimResponseDocument());

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                    callbackParamsOf(
                        caseData, ABOUT_TO_SUBMIT
                    ));

                assertThat(response.getData().get("respondentSharedClaimResponseDocument")).isNull();
            }
        }

        @Test
        void shouldUpdateLocation_WhenCmlIsCcmccAndToggleOn() {
            // Given
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation(handler.ccmccEpimsId);
            caseLocationCivil.setRegion("ccmcRegion");
            var caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.TWO_V_ONE)
                .caseManagementLocation(caseLocationCivil)
                .build();
            //When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsExactly("10", "214320");
        }

        @Test
        void shouldNotUpdateLocation_WhenCmlIsNotCcmccAndToggleOn() {
            // Given
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("12345");
            caseLocationCivil.setRegion("3");
            var caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.TWO_V_ONE)
                .caseManagementLocation(caseLocationCivil)
                .build();
            //When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsExactly("3", "12345");
        }

        @Test
        void shouldCallUpdateWaCourtLocationsServiceWhenPresent_AndMintiEnabled() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("12345");
            caseLocationCivil.setRegion("3");
            var caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.TWO_V_ONE)
                .caseManagementLocation(caseLocationCivil)
                .build();
            //When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(updateWaCourtLocationsService).updateCourtListingWALocations(any(), any());
        }

        @Test
        void shouldNotCallUpdateWaCourtLocationsServiceWhenNotPresent_AndMintiEnabled() {

            handler = new RespondToDefenceCallbackHandler(exitSurveyContentService, unavailableDateValidator, mapper,
                                                          time, featureToggleService, locationRefDataService, locationRefDataUtil,
                                                          locationHelper, caseFlagsInitialiser, toggleConfiguration, assignCategoryId,
                                                          caseDetailsConverter, frcDocumentsUtils, Optional.empty(),
                                                          requestedCourtForClaimDetailsTab);
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation("12345");
            caseLocationCivil.setRegion("3");
            var caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.TWO_V_ONE)
                .caseManagementLocation(caseLocationCivil)
                .build();
            //When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(updateWaCourtLocationsService);
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

package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask.AboutToSubmitRespondToDefenceTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask.BuildConfirmationTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask.DetermineNextState;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask.PopulateCaseDataTask;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.AirlineEpimsId;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.DocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.citizenui.RespondentMediationService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToDefenceSpecCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    AboutToSubmitRespondToDefenceTask.class,
    DetermineNextState.class,
    BuildConfirmationTask.class,
    PopulateCaseDataTask.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    UnavailableDateValidator.class,
    CaseDetailsConverter.class,
    CourtLocationUtils.class,
    LocationHelper.class,
    LocationReferenceDataService.class,
    JudgementService.class,
    PaymentDateService.class,
    ResponseOneVOneShowTagService.class,
    JudgmentByAdmissionOnlineMapper.class,
    AssignCategoryId.class,
    FrcDocumentsUtils.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class
})
class RespondToDefenceSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToDefenceSpecCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JudgementService judgementService;

    @Autowired
    private PaymentDateService paymentDateService;

    @Autowired
    private ResponseOneVOneShowTagService responseOneVOneShowTagService;

    @Autowired
    private FrcDocumentsUtils frcDocumentsUtils;

    @MockBean
    private UnavailableDateValidator unavailableDateValidator;

    @MockBean
    private Time time;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private CourtLocationUtils courtLocationUtils;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private DQResponseDocumentUtils dqResponseDocumentUtils;

    @Autowired
    private AssignCategoryId assignCategoryId;

    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;
    @MockBean
    private RespondentMediationService respondentMediationService;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @MockBean
    private WorkingDayIndicator workingDayIndicator;
    @MockBean
    private DeadlineExtensionCalculatorService deadlineCalculatorService;
    @Autowired
    private RoboticsAddressMapper addressMapper;
    @Autowired
    private AddressLinesMapper linesMapper;
    @MockBean
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    @Nested
    class AboutToStart {

        @Test
        void shouldPopulateInitialData() {
            var params = callbackParamsOf(
                CaseData.builder()
                    .respondent1(Party.builder()
                                     .type(Party.Type.COMPANY)
                                     .companyName("company name")
                                     .build())
                    .build(),
                ABOUT_TO_START
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("respondent1Copy")
                .isNotNull();
            assertThat(response.getData()).extracting("claimantResponseScenarioFlag")
                .isNotNull();
        }

        @Test
        void shouldPopulateCourtLocations() {
            when(courtLocationUtils.getLocationsFromList(any()))
                .thenReturn(fromList(List.of("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124")));

            CaseData caseData = CaseData.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            System.out.println(getCaseData(response));

            DynamicList dynamicList = getCaseData(response).getApplicant1DQ()
                .getApplicant1DQRequestedCourt().getResponseCourtLocations();

            List<String> courtlist = dynamicList.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .toList();

            assertThat(courtlist).containsOnly("Site 1 - Lane 1 - 123", "Site 2 - Lane 2 - 124");
        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return objectMapper.convertValue(response.getData(), CaseData.class);
        }

        @Test
        void shouldPopulateOnlyRespondent1Docs_whenInvokedAndSystemGeneratedContainsDQ() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder()
                                                               .documentName("defendant_directions_questionnaire_form")
                                                               .documentType(DIRECTIONS_QUESTIONNAIRE).build()))
                .respondent1DocumentURL(null)
                .build();
            // When
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Given
            assertThat(response.getData()).extracting("respondent1GeneratedResponseDocument").isNotNull();
            assertThat(response.getData()).extracting("respondent2GeneratedResponseDocument").isNull();
        }

        @Test
        void shouldNotPopulateOnlyRespondent1Docs_whenInvokedAndSystemGeneratedDoesNotContainDQ() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder()
                                                               .documentName("banana")
                                                               .documentType(SEALED_CLAIM).build()))
                .respondent1DocumentURL(null)
                .build();
            // When
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Given
            assertThat(response.getData()).extracting("respondent1GeneratedResponseDocument").isNull();
            assertThat(response.getData()).extracting("respondent2GeneratedResponseDocument").isNull();
        }

        @Test
        void shouldPopulateRespondent1AndRespondent2Docs_whenInvokedAndSystemGeneratedContainsDQ() {
            // Given
            var testDocument1 = CaseDocument.builder()
                .documentName("defendant_directions_questionnaire_form")
                .documentType(DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder()
                                  .documentUrl("test-respondent1Doc-url")
                                  .documentFileName("file-name")
                                  .documentBinaryUrl("binary-url")
                                  .build()).build();

            var testDocument2 = CaseDocument.builder()
                .documentName("defendant_directions_questionnaire_form")
                .documentType(DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder()
                                  .documentUrl("test-respondent2Doc-url")
                                  .documentFileName("file-name")
                                  .documentBinaryUrl("binary-url")
                                  .build()).build();

            List<Element<CaseDocument>> documentList = new ArrayList<>();
            documentList.add(element(testDocument1));
            documentList.add(element(testDocument2));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .systemGeneratedCaseDocuments(documentList)
                .respondent1DocumentURL("test-respondent1Doc-url")
                .respondent2DocumentURL("test-respondent2Doc-url")
                .build();

            // When
            var params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("respondent1GeneratedResponseDocument").isNotNull();
            assertThat(response.getData()).extracting("respondent2GeneratedResponseDocument").isNotNull();
        }

        @Test
        void shouldPopulateResponse_whenInvokedAndSystemGeneratedContainsResponseDoc() {
            // Given
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
            var testDocument1 = CaseDocument.builder()
                .documentName("response_sealed_form.pdf")
                .documentType(SEALED_CLAIM)
                .documentLink(Document.builder()
                                  .documentUrl("test-respondent1Doc-url")
                                  .documentFileName("response_sealed_form.pdf")
                                  .documentBinaryUrl("binary-url")
                                  .build()).build();
            List<Element<CaseDocument>> documentList = new ArrayList<>();
            documentList.add(element(testDocument1));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .systemGeneratedCaseDocuments(documentList)
                .build();
            // When
            var params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("respondent1ClaimResponseDocumentSpec").isNotNull();
        }
    }

    @Nested
    class ValidateUnavailableDates {

        @Test
        void shouldCheckDates_whenFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearingLRspec(Hearing.builder()
                                                                 .build())
                                  .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(MID, caseData)
                .pageId("validate-unavailable-dates")
                .build();

            Mockito.when(unavailableDateValidator.validateFastClaimHearing(
                    caseData.getApplicant1DQ().getApplicant1DQHearingLRspec()))
                .thenReturn(Collections.emptyList());

            handler.handle(params);

            Mockito.verify(unavailableDateValidator).validateFastClaimHearing(
                caseData.getApplicant1DQ().getApplicant1DQHearingLRspec());
        }

        @Test
        void shouldCheckDates_whenSmallClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQSmallClaimHearing(SmallClaimHearing.builder()
                                                                     .build())
                                  .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(MID, caseData)
                .pageId("validate-unavailable-dates")
                .build();

            Mockito.when(unavailableDateValidator.validateSmallClaimsHearing(
                    caseData.getApplicant1DQ().getApplicant1DQSmallClaimHearing()))
                .thenReturn(Collections.emptyList());

            handler.handle(params);

            Mockito.verify(unavailableDateValidator).validateSmallClaimsHearing(
                caseData.getApplicant1DQ().getApplicant1DQSmallClaimHearing());
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
    class MidEventCallbackSetApplicantProceedFlag {

        private static final String PAGE_ID = "set-applicant1-proceed-flag";

        @Test
        void shouldSetApplicant1Proceed_whenCaseIs2v1AndApplicantIntendsToProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec2v1ClaimSubmitted()
                .atStateRespondent2v1FullDefence()
                .applicant1ProceedWithClaimSpec2v1(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicant1ProceedWithClaim"))
                .isEqualTo("Yes");
        }

        @Test
        void shouldNotSetApplicant1Proceed_whenCaseIs2v1AndApplicantNotIntendsToProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec2v1ClaimSubmitted()
                .atStateRespondent2v1FullDefence()
                .applicant1ProceedWithClaimSpec2v1(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicant1ProceedWithClaim"))
                .isEqualTo(null);
        }

        @Test
        void shouldSetVulnerability_whenRejectAllAndProceed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullDefenceSpec()
                .applicant1ProceedWithClaim(YES)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_2, caseData, MID,
                                                     "set-applicant-route-flags");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("showConditionFlags").asList()
                    .contains(DefendantResponseShowTag.VULNERABILITY.name());
        }

        @Test
        void shouldSetVulnerability_whenNotAgreeOwedAmount() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec1v1ClaimSubmitted()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_2, caseData, MID,
                                                     "set-applicant-route-flags");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("showConditionFlags").asList()
                .contains(DefendantResponseShowTag.VULNERABILITY.name());
        }
    }

    @Nested
    class MidEventCallbackSetApplicantRoutesFlag {

        private static final String PAGE_ID = "set-applicant-route-flags";

        @Test
        void shouldSetApplicantRouteFlag_whenClaimantRejectPartPaymentPlan() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1AcceptAdmitAmountPaidSpec(NO)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicantDefenceResponseDocumentAndDQFlag"))
                .isEqualTo("Yes");
        }

        @Test
        void shouldSetApplicantRouteFlag_whenItsFullDefence() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .applicant1ProceedWithClaim(YES)
                .applicant1ProceedWithClaimSpec2v1(YES)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicantDefenceResponseDocumentAndDQFlag"))
                .isEqualTo("Yes");
        }

        @Test
        void shouldNotSetApplicantRouteFlag_whenClaimantAcceptPartPaymentPlan() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .applicant1AcceptAdmitAmountPaidSpec(YES)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData().get("applicantDefenceResponseDocumentAndDQFlag"))
                .isEqualTo("No");
        }

        @Test
        void shouldSetVulnerability_whenApplicant1IsProceedWithClaimSpec2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .applicant1ProceedWithClaimSpec2v1(YES)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_2, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("showConditionFlags").asList()
                .contains(DefendantResponseShowTag.VULNERABILITY.name());
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
    class MidGetPaymentDate {
        private static final String PAGE_ID = "get-payment-date";

        @Test
        void shouldSetStatementOfTruthFieldsToNull_whenPopulated() {
            String name = "John Smith";
            String role = "Solicitor";

            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("currentDateboxDefendantSpec")
                .isNotNull();
        }

    }

    @Nested
    class MidSugestInstalmentsValidation {
        private static final String PAGE_ID = "validate-suggest-instalments";

        @Test
        void shouldreturnError_whenPopulated() {
            String name = "John Smith";
            String role = "Solicitor";

            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build().toBuilder()
                .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(BigDecimal.TEN)
                .totalClaimAmount(BigDecimal.ONE)
                .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.now())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("Enter a valid amount for equal instalments", response.getErrors().get(0));
        }

        @Test
        void shouldReturnPaymentError_whenPopulated() {
            String name = "John Smith";
            String role = "Solicitor";

            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build().toBuilder()
                .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal(-4))
                .totalClaimAmount(BigDecimal.ONE)
                .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.now())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("Enter an amount of £1 or more", response.getErrors().get(0));
        }

    }

    @Nested
    class AboutToSubmitCallback {
        private final LocalDateTime localDateTime = now();

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(localDateTime);
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
            when(dqResponseDocumentUtils.buildClaimantResponseDocuments(any(CaseData.class))).thenReturn(new ArrayList<>());
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
                .containsExactly(READY.name(), CLAIMANT_RESPONSE_SPEC.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
            verify(dqResponseDocumentUtils, times(1)).buildClaimantResponseDocuments(any(CaseData.class));
        }

        @Test
        void shouldSetClaimantResponseDocs() {
            Document document = DocumentBuilder.builder().build();
            CaseData fullDefenceData = CaseDataBuilder.builder().atState(FULL_DEFENCE_PROCEED).build();
            CaseData caseData = fullDefenceData.toBuilder()
                .applicant1DQ(fullDefenceData.getApplicant1DQ().toBuilder()
                                  .applicant1DQDraftDirections(document)
                    .build())
                .build();
            var expectedResponseDocuments = List.of(
                Element.<CaseDocument>builder()
                    .id(UUID.randomUUID())
                    .value(CaseDocument.builder()
                               .documentLink(document)
                               .documentName("doc-name")
                               .createdBy("Claimant")
                               .createdDatetime(LocalDateTime.now())
                               .build())
                    .build());
            when(dqResponseDocumentUtils.buildClaimantResponseDocuments(any(CaseData.class))).thenReturn(expectedResponseDocuments);

            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            var actualCaseData = getCaseData(response);

            assertThat(actualCaseData.getClaimantResponseDocuments()).isEqualTo(expectedResponseDocuments);
            assertThat(actualCaseData.getApplicant1DQ().getApplicant1DQDraftDirections()).isEqualTo(null);

            verify(dqResponseDocumentUtils, times(1)).buildClaimantResponseDocuments(any(CaseData.class));
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"},
            mode = EnumSource.Mode.INCLUDE)
        void shouldUpdateBusinessProcess_whenAtFullDefenceStateV2(FlowState.Main flowState) {
            var params = callbackParamsOf(
                CallbackVersion.V_2,
                CaseDataBuilder.builder().atState(flowState).build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), CLAIMANT_RESPONSE_SPEC.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
        }

        @Test
        void shouldAddExperts_whenAtFullDefenceStateV1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FULL_DEFENCE_PROCEED)
                .build();
            var params = callbackParamsOf(
                CallbackVersion.V_1,
                caseData.toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .applicant2(Party.builder()
                                    .companyName("company")
                                    .type(Party.Type.COMPANY)
                                    .build())
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQFileDirectionsQuestionnaire(
                                          caseData.getApplicant1DQ()
                                              .getApplicant1DQFileDirectionsQuestionnaire())
                                      .applicant2RespondToClaimExperts(
                                          ExpertDetails.builder().build())
                                      .build())
                    .applicant1DQ(Applicant1DQ.builder().applicant1RespondToClaimExperts(
                        ExpertDetails.builder().build()).build())
                    .applicant2ResponseDate(LocalDateTime.now())
                    .build(),
                ABOUT_TO_SUBMIT
            );
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1DQExperts").isNotNull();
        }

        @Test
        void shouldMoveCaseToIn_MediationAndUpdateDate_V2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FULL_DEFENCE_PROCEED)
                .build();
            var params = callbackParamsOf(
                CallbackVersion.V_2,
                caseData.toBuilder()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                    .caseDataLiP(CaseDataLiP.builder()
                                     .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                                                  .hasAgreedFreeMediation(
                                                                                      MediationDecision.Yes)
                                                                                  .build())
                                     .build())
                    .applicant2(Party.builder()
                                    .companyName("company")
                                    .type(Party.Type.COMPANY)
                                    .build())
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQFileDirectionsQuestionnaire(
                                          caseData.getApplicant1DQ()
                                              .getApplicant1DQFileDirectionsQuestionnaire())
                                      .applicant2RespondToClaimExperts(
                                          ExpertDetails.builder().build())
                                      .build())
                    .applicant1DQ(Applicant1DQ.builder().applicant1RespondToClaimExperts(
                        ExpertDetails.builder().build()).build())
                    .applicant2ResponseDate(LocalDateTime.now())
                    .build(),
                ABOUT_TO_SUBMIT
            );
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getState()).isEqualTo(IN_MEDIATION.toString());
            assertThat(response.getData()).extracting("claimMovedToMediationOn").isNotNull();
        }

        @Test
        void shouldAddPartyIdsToPartyFields_whenInvoked() {
            var params = callbackParamsOf(
                CaseDataBuilder.builder().atState(FlowState.Main.FULL_DEFENCE).build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("applicant1").hasFieldOrProperty("partyID");
            assertThat(response.getData()).extracting("respondent1").hasFieldOrProperty("partyID");
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"},
            mode = EnumSource.Mode.INCLUDE)
        void shouldUpdateApplicant1DQExpertsDetails(FlowState.Main flowState) {
            var params = callbackParamsOf(
                CaseDataBuilder.builder()
                    .applicant2DQSmallClaimExperts()
                    .applicant2ResponseDate(LocalDateTime.now())
                    .atState(flowState).build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), CLAIMANT_RESPONSE_SPEC.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class,
            names = {"FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED"},
            mode = EnumSource.Mode.INCLUDE)
        void shouldUpdateApplicant2DQExpertsDetails(FlowState.Main flowState) {
            var params = callbackParamsOf(
                CaseDataBuilder.builder()
                    .applicant2DQSmallClaimExperts()
                    .applicant2ResponseDate(LocalDateTime.now())
                    .atState(flowState).build(),
                ABOUT_TO_SUBMIT
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent")
                .containsExactly(READY.name(), CLAIMANT_RESPONSE_SPEC.name());

            assertThat(response.getData()).containsEntry("applicant1ResponseDate", localDateTime.format(ISO_DATE_TIME));
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
        class HandleCourtLocation {

            @Test
            void shouldHandleCourtLocationDataIfFastTrack() {
                LocationRefData locationA = LocationRefData.builder()
                    .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                    .courtAddress("Lane 1").postcode("123").build();
                when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                    .thenReturn(locationA);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .applicant1DQ(
                        Applicant1DQ.builder().applicant1DQRequestedCourt(
                            RequestedCourt.builder()
                                .responseCourtLocations(DynamicList.builder().build())
                                .build()).build())
                    .responseClaimTrack(FAST_CLAIM.name())
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation(handler.cnbcEpimsId).region("cnbc region").build())
                    .build();

                CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                assertThat(response.getData())
                    .extracting("applicant1DQRequestedCourt")
                    .extracting("responseCourtLocations").isNull();

                assertThat(response.getData())
                    .extracting("applicant1DQRequestedCourt")
                    .extracting("caseLocation")
                    .extracting("region", "baseLocation")
                    .containsExactly("regionId1", "epimmsId1");

                assertThat(response.getData())
                    .extracting("applicant1DQRequestedCourt")
                    .extracting("responseCourtCode").isEqualTo("312");
            }

            void shouldHandleCourtLocationDataIfSmallTrackAndNotFlightDelay() {
                LocationRefData locationA = LocationRefData.builder()
                    .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                    .courtAddress("Lane 1").postcode("123").build();
                when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                    .thenReturn(locationA);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .applicant1DQ(
                        Applicant1DQ.builder().applicant1DQRequestedCourt(
                            RequestedCourt.builder()
                                .responseCourtLocations(DynamicList.builder().build())
                                .build()).build())
                    .responseClaimTrack(SMALL_CLAIM.name())
                    .build();

                caseData.setIsFlightDelayClaim(NO);
                CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                assertThat(response.getData())
                    .extracting("applicant1DQRequestedCourt")
                    .extracting("responseCourtLocations").isNull();

                assertThat(response.getData())
                    .extracting("applicant1DQRequestedCourt")
                    .extracting("caseLocation")
                    .extracting("region", "baseLocation")
                    .containsExactly("regionId1", "epimmsId1");

                assertThat(response.getData())
                    .extracting("applicant1DQRequestedCourt")
                    .extracting("responseCourtCode").isEqualTo("312");
            }

        }

        @Nested
        class ClaimTypeFlightDelay {

            @Test
            void shouldUpdateCaseManagmentLocationIfAirlineNotOther() {
                given(featureToggleService.isSdoR2Enabled()).willReturn(true);
                LocationRefData locationA = LocationRefData.builder()
                    .regionId("regionId1").epimmsId("111000").courtLocationCode("312").siteName("Site 1")
                    .courtAddress("Lane 1").postcode("123").build();
                when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                    .thenReturn(locationA);
                CaseLocationCivil flightLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();

                List<AirlineEpimsId> airlineEpimsIDList = new ArrayList<>();
                airlineEpimsIDList.add(AirlineEpimsId.builder().airline("BA/Cityflyer").epimsID("111000").build());

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation(handler.cnbcEpimsId).region("cnbc region").build())
                    .applicant1DQ(
                        Applicant1DQ.builder().applicant1DQRequestedCourt(
                            RequestedCourt.builder()
                                .responseCourtLocations(DynamicList.builder().build())
                                .build()).build())

                    .flightDelay(FlightDelayDetails.builder()
                                     .airlineList(fromList(airlineEpimsIDList.stream()
                                                                    .map(AirlineEpimsId::getAirline).toList(),
                                                           Object::toString, airlineEpimsIDList.stream()
                                                                    .map(AirlineEpimsId::getAirline).toList().get(0), false))
                                     .flightCourtLocation(flightLocation).build())
                    .build();

                caseData.setIsFlightDelayClaim(YES);

                CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                assertThat(response.getData())
                    .extracting("caseManagementLocation")
                    .extracting("region", "baseLocation")
                    .containsExactly("2", "111000");
            }

            @Test
            void shouldUpdateCaseManagementLocatioToClaimantCourtIfAirlineOther() {

                given(featureToggleService.isSdoR2Enabled()).willReturn(true);
                LocationRefData locationA = LocationRefData.builder()
                    .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                    .courtAddress("Lane 1").postcode("123").build();
                when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                    .thenReturn(locationA);

                DynamicList airlineList = DynamicList.builder()
                    .listItems(List.of(
                                   DynamicListElement.builder().code("OTHER").label("OTHER").build()
                               )
                    )
                    .value(DynamicListElement.builder().code("OTHER").label("OTHER").build())
                    .build();
                CaseLocationCivil requestCourt = CaseLocationCivil.builder().baseLocation("111000").region("2").build();

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                    .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                    .applicant1DQ(
                        Applicant1DQ.builder().applicant1DQRequestedCourt(
                            RequestedCourt.builder()
                                .responseCourtLocations(DynamicList.builder().build())
                                .build()).build())
                    .flightDelay(FlightDelayDetails.builder()
                                     .airlineList(airlineList)
                                     .flightCourtLocation(null)
                                     .build())
                    .build();

                caseData.setIsFlightDelayClaim(YES);

                CallbackParams callbackParams = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                assertThat(response.getData())
                    .extracting("caseManagementLocation").isNotNull();

                assertThat(response.getData())
                    .extracting("caseManagementLocation")
                    .extracting("region", "baseLocation")
                    .containsExactly("4", "00000");

            }

        }

        @Nested
        class UpdateExperts {
            @Test
            void updateApplicant1Experts() {
                ExpertDetails experts = ExpertDetails.builder()
                    .expertName("Mr Expert Defendant")
                    .firstName("Expert")
                    .lastName("Defendant")
                    .phoneNumber("07123456789")
                    .emailAddress("test@email.com")
                    .fieldofExpertise("Roofing")
                    .estimatedCost(new BigDecimal(434))
                    .build();

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .applicant1DQSmallClaimExperts(experts, YES)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response.getData())
                    .extracting("applicant1ClaimExpertSpecRequired").isEqualTo("Yes");
                assertThat(response.getData()).extracting("applicant1DQExperts").extracting("expertRequired").isEqualTo("Yes");
            }

            @Test
            void updateApplicant1Experts_NoExperts() {
                CaseData caseData = CaseDataBuilder.builder()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .atStateApplicantRespondToDefenceAndProceed()
                    .applicant1DQSmallClaimExperts(null, NO)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                    .thenReturn(LocalDateTime.now());

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response.getData())
                    .extracting("applicant1ClaimExpertSpecRequired").isEqualTo("No");
                assertThat(response.getData()).extracting("applicant1DQExperts").extracting("expertRequired").isEqualTo("No");
            }

            @Test
            void updateApplicant2Experts() {
                ExpertDetails experts = ExpertDetails.builder()
                    .expertName("Mr Expert Defendant")
                    .firstName("Expert")
                    .lastName("Defendant")
                    .phoneNumber("07123456789")
                    .emailAddress("test@email.com")
                    .fieldofExpertise("Roofing")
                    .estimatedCost(new BigDecimal(434))
                    .build();

                CaseData caseData = CaseDataBuilder.builder()
                    .respondent2(PartyBuilder.builder().individual().build())
                    .atStateApplicantRespondToDefenceAndProceed()
                    .applicant2DQSmallClaimExperts(experts, YES)
                    .applicant2ResponseDate(LocalDateTime.now())
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response.getData())
                    .extracting("applicantMPClaimExpertSpecRequired").isEqualTo("Yes");
                assertThat(response.getData()).extracting("applicant2DQExperts").extracting("expertRequired").isEqualTo("Yes");
            }

            @Test
            void updateAddress() {
                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent1(PartyBuilder.builder().individual().build())
                    .applicant1(PartyBuilder.builder().individual().build())
                    .applicant2(PartyBuilder.builder().individual().build())
                    .build();
                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                    .build();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response.getData())
                    .extracting("applicant1").extracting("primaryAddress").isNotNull();
                assertThat(response.getData())
                    .extracting("applicant2").extracting("primaryAddress").isNotNull();
                assertThat(response.getData())
                    .extracting("respondent1").extracting("primaryAddress").isNotNull();
                assertThat(response.getData())
                    .extracting("respondent2").extracting("primaryAddress").isNotNull();
            }

            @Test
            void updateApplicant2Experts_WhenNoExperts() {
                CaseData caseData = CaseDataBuilder.builder()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .atStateApplicantRespondToDefenceAndProceed()
                    .noApplicant2DQSmallClaimExperts()
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                // When
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                // Then
                assertThat(response.getData())
                    .extracting("applicantMPClaimExpertSpecRequired").isEqualTo("No");
                assertThat(response.getData()).extracting("applicant2DQExperts").extracting("expertRequired").isEqualTo("No");
            }
        }

        @Test
        void shouldChangeCaseState_WhenRespondentRepaymentPlanAndFlagV2WithJudgementLive() {
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            given(featureToggleService.isJudgmentOnlineLive()).willReturn(true);
            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.YES)
                .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL).build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldChangeCaseState_WhenRespondentRepaymentPlanAndFlagV2WithJudgementLiveAndLrVLr() {
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            given(featureToggleService.isJudgmentOnlineLive()).willReturn(true);
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(50.0))
                .ccjJudgmentTotalStillOwed(BigDecimal.valueOf(500.0))
                .build();
            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.YES)
                .applicant1AcceptFullAdmitPaymentPlanSpec(YES)
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(SUGGESTION_OF_REPAYMENT_PLAN)
                .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                              .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                              .firstRepaymentDate(LocalDate.now().plusDays(5))
                                              .paymentAmount(BigDecimal.valueOf(100.0))
                                              .build())
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.COMPANY)
                                 .companyName("company name").build())
                .ccjPaymentDetails(ccjPaymentDetails)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(All_FINAL_ORDERS_ISSUED.name());
        }

        @Test
        void shouldChangeCaseState_WhenRespondentPaymentSetByDateAndFlagV2WithJudgementLive() {
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            given(featureToggleService.isJudgmentOnlineLive()).willReturn(true);
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(50.0))
                .ccjJudgmentTotalStillOwed(BigDecimal.valueOf(500.0))
                .build();
            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.YES)
                .applicant1AcceptFullAdmitPaymentPlanSpec(YES)
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                   .whenWillThisAmountBePaid(LocalDate.now().plusDays(5)).build())
                .ccjPaymentDetails(ccjPaymentDetails)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL).build()).build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData()).extracting("activeJudgment").isNotNull();
            assertThat(response.getData().get("activeJudgment")).extracting("state").isEqualTo("ISSUED");
            assertThat(response.getData().get("activeJudgment")).extracting("type").isEqualTo("JUDGMENT_BY_ADMISSION");
            assertThat(response.getData().get("activeJudgment")).extracting("judgmentId").isEqualTo(1);
            assertThat(response.getData().get("activeJudgment")).extracting("isRegisterWithRTL").isEqualTo("Yes");

        }

        @Test
        void shouldChangeCaseState_WhenRespondentPaymentImmediatelyAndFlagV2WithJudgementLive() {
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            given(featureToggleService.isJudgmentOnlineLive()).willReturn(true);
            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.YES)
                .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL).build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldChangeCaseState_WhenApplicant1AcceptFullAdmitPaymentPlanSpecNoAndFlagV2() {
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            CaseData caseData = CaseData.builder().applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL).build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldChangeCaseState_WhenApplicant1AcceptFullAdmitPaymentPlanAndFlagV2() {
            //Given
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            CaseData caseData = CaseData.builder().applicant1AcceptFullAdmitPaymentPlanSpec(YES)
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL).build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldChangeCaseState_WhenApplicant1AcceptPartAdmitPaymentPlanSpecAndFlagV2() {
            //Given
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            CaseData caseData = CaseData.builder().applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL).build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldChangeCaseState_WhenApplicant1AcceptPartAdmitPaymentPlanSpecNoAndFlagV2() {
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            CaseData caseData = CaseData.builder().applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL)
                                 .build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }

        @Test
        void shouldNullDocument_whenCaseFileEnabled() {
            // Given
            CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.COMPANY)
                                 .companyName("company name").build())
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentName("defendant_directions_questionnaire_form")
                                                               .documentType(DIRECTIONS_QUESTIONNAIRE)
                                                               .build()))
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            // When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            assertThat(response.getData()).extracting("respondent1GeneratedResponseDocument").isNull();
            assertThat(response.getData()).extracting("respondent2GeneratedResponseDocument").isNull();
            assertThat(response.getData()).extracting("respondent1ClaimResponseDocumentSpec").isNull();
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
            CaseData updatedData = getCaseData(response);
            System.out.println(updatedData.getClaimantResponseDocuments());
            //Then
            assertThat(updatedData.getApplicant1DQ().getApplicant1DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument().getCategoryID()).isEqualTo("DQApplicant");
        }

        @Test
        void shouldChangeCaseState_WhenApplicant1NotAcceptPartAdmitAmountWithoutMediationAndFlagV2() {
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            CaseData caseData = CaseData.builder().applicant1AcceptAdmitAmountPaidSpec(NO)
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .responseClaimMediationSpecRequired(NO)
                .applicant1DQ(Applicant1DQ.builder().applicant1RespondToClaimExperts(
                ExpertDetails.builder().build()).build())
                .respondent1DetailsForClaimDetailsTab(Party.builder()
                                                          .type(Party.Type.COMPANY)
                                                          .primaryAddress(Address.builder().build())
                                                          .build())
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldChangeCaseState_WhenApplicant1NotAcceptPartAdmitAmountWithFastTrackAndFlagV2() {
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            CaseData caseData = CaseData.builder().applicant1AcceptAdmitAmountPaidSpec(NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                .responseClaimTrack(FAST_CLAIM.name())
                .applicant1DQ(Applicant1DQ.builder().applicant1RespondToClaimExperts(
                    ExpertDetails.builder().build()).build())
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL)
                                 .build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldChangeCaseStateToJudicialReferral_ONE_V_ONE() {
            CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .applicant1ProceedWithClaim(YES)
                .responseClaimTrack(FAST_CLAIM.name())
                .applicant1DQ(Applicant1DQ.builder().applicant1RespondToClaimExperts(
                    ExpertDetails.builder().build()).build())
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL).build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldChangeCaseStateToJudicialReferral_TWO_V_ONE() {
            CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .applicant2(Party.builder()
                                .individualTitle("Mr")
                                .individualFirstName("Test")
                                .individualLastName("Test")
                                .individualDateOfBirth(LocalDate.now().minusYears(18))
                                .type(Party.Type.INDIVIDUAL)
                                .build())
                .addApplicant2(YES)
                .applicant1ProceedWithClaimSpec2v1(YES)
                .responseClaimTrack(FAST_CLAIM.name())
                .applicant1DQ(Applicant1DQ.builder().applicant1RespondToClaimExperts(
                    ExpertDetails.builder().build()).build())
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL)
                                 .build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldChangeCaseStateToJudicialReferral_ONE_V_TWO_ONE_REP() {
            CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .respondent2(PartyBuilder.builder().company().build())
                .respondent2SameLegalRepresentative(YES)
                .addRespondent2(YES)
                .respondentResponseIsSame(YES)
                .responseClaimTrack(FAST_CLAIM.name())
                .applicant1ProceedWithClaim(YES)
                .applicant1DQ(Applicant1DQ.builder().applicant1RespondToClaimExperts(
                    ExpertDetails.builder().build()).build())
                .respondent1(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .primaryAddress(Address.builder().build())
                                 .build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldChangeCaseStateToJudicialReferral_ONE_V_TWO_TWO_REP() {
            CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .respondent2(PartyBuilder.builder().company().build())
                .respondent2SameLegalRepresentative(NO)
                .addRespondent2(YES)
                .responseClaimTrack(FAST_CLAIM.name())
                .applicant1ProceedWithClaim(YES)
                .applicant1DQ(Applicant1DQ.builder().applicant1RespondToClaimExperts(
                    ExpertDetails.builder().build()).build())
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL)
                                 .build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState())
                .isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        }

        @Test
        void shouldMoveCaseToIn_MediationWhenClaimantProceeds1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1ProceedWithClaim(YES)
                .build().toBuilder()
                .responseClaimTrack(SMALL_CLAIM.name())
                .build();

            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getState()).isEqualTo(IN_MEDIATION.toString());
        }

        @Test
        void shouldMoveCaseToIn_MediationWhenClaimantProceeds2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .responseClaimTrack(SMALL_CLAIM.name())
                .applicant1ProceedWithClaimSpec2v1(YES).build();

            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getState()).isEqualTo(IN_MEDIATION.toString());
        }

        @Test
        void shouldNotMoveCaseToIn_MediationWhenClaimantProceedsFastClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .responseClaimTrack(FAST_CLAIM.name())
                .applicant1ProceedWithClaimSpec2v1(YES).build();

            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getState()).isNotEqualTo(IN_MEDIATION.toString());
        }

        @Test
        void shouldNotMoveCaseToIn_MediationWhenClaimantProceedsMultiClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .responseClaimTrack(MULTI_CLAIM.name())
                .applicant1ProceedWithClaim(YES).build();

            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getState()).isNotEqualTo(IN_MEDIATION.toString());
        }

        @Test
        void shouldMoveCaseTo_case_stayed_State_LRvLip_One_V_One() {
            CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().partyName("name").type(INDIVIDUAL).build())
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .defenceRouteRequired(DISPUTES_THE_CLAIM)
                .respondent1Represented(NO)
                .applicant1Represented(YES)
                .responseClaimTrack(SMALL_CLAIM.name())
                .applicant1ProceedWithClaim(NO)
                .applicant1DQ(Applicant1DQ.builder().applicant1RespondToClaimExperts(
                    ExpertDetails.builder().build()).build())
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL)
                                 .build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();

            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            var params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getState()).isEqualTo(CASE_STAYED.toString());

        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return objectMapper.convertValue(response.getData(), CaseData.class);
        }

        @Test
        void shouldUpdateLocation_WhenCmlIsCnbcNonMintiLip() {
            // Given
            var caseData = CaseDataBuilder.builder()
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.TWO_V_ONE)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation(handler.cnbcEpimsId).region("cnbcRegion").build())
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
        void shouldNotUpdateLocation_WhenCmlIsNotCnbcNonMintiLip() {
            // Given
            var caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.TWO_V_ONE)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("12345").region("3").build())
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
        void shouldUpdateMaintainLocationToCnbc_WhenMintiLip() {
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
            // Given
            var caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .responseClaimTrack("INTERMEDIATE_CLAIM")
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("2").region("420219").build())
                .respondent1Represented(NO)
                .build();
            //When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsExactly("2", "420219");
        }

        @Test
        void shouldUpdateLocation_WhenCmlIsCnbcToggleOnFlightDelayOtherSmall() {
            // Given
            given(featureToggleService.isSdoR2Enabled()).willReturn(true);
            LocationRefData locationA = LocationRefData.builder()
                .regionId("regionId1").epimmsId("epimmsId1").courtLocationCode("312").siteName("Site 1")
                .courtAddress("Lane 1").postcode("123").build();
            when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class)))
                .thenReturn(locationA);

            DynamicList airlineList = DynamicList.builder()
                .listItems(List.of(
                               DynamicListElement.builder().code("OTHER").label("OTHER").build()
                           )
                )
                .value(DynamicListElement.builder().code("OTHER").label("OTHER").build())
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
                .applicant1DQ(
                    Applicant1DQ.builder().applicant1DQRequestedCourt(
                        RequestedCourt.builder()
                            .responseCourtLocations(DynamicList.builder().build())
                            .build()).build())
                .flightDelay(FlightDelayDetails.builder()
                                 .airlineList(airlineList)
                                 .flightCourtLocation(null)
                                 .build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation(handler.cnbcEpimsId).region("cnbcRegion").build())
                .build();

            caseData.setIsFlightDelayClaim(YES);
            //When
            var params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData())
                .extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsExactly("10", "214320");
        }
    }

    @Nested
    class ConfirmationText {

        @Test
        void summary_WhenProceeds() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .applicant1ProceedWithClaim(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);

            assertThat(response.getConfirmationBody())
                .contains("contact you about what to do next");
            assertThat(response.getConfirmationHeader())
                .contains(
                    "decided to proceed",
                    caseData.getLegacyCaseReference()
                );
        }

        @Test
        void summary_WhenDoesNotProceed() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .applicant1ProceedWithClaim(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);

            assertThat(response.getConfirmationBody())
                .contains("not to proceed");
            assertThat(response.getConfirmationHeader())
                .contains(
                    "not to proceed",
                    caseData.getLegacyCaseReference()
                );
        }

        @Test
        void summary_when_all_finals_order_issued() {
            given(featureToggleService.isPinInPostEnabled()).willReturn(true);
            given(featureToggleService.isJudgmentOnlineLive()).willReturn(true);
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(500.0))
                .ccjJudgmentLipInterest(BigDecimal.valueOf(300))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(0))
                .build();
            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.YES)
                .applicant1AcceptFullAdmitPaymentPlanSpec(YES)
                .applicant1(Party.builder().type(COMPANY).companyName("Applicant1").build())
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                   .whenWillThisAmountBePaid(LocalDate.now().plusDays(5)).build())
                .ccjPaymentDetails(ccjPaymentDetails)
                .ccdState(All_FINAL_ORDERS_ISSUED)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .respondent1(Party.builder()
                                 .primaryAddress(Address.builder().build())
                                 .type(Party.Type.INDIVIDUAL).build()).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);

            assertThat(response.getConfirmationBody())
                .contains("Download county court judgment");
            assertThat(response.getConfirmationHeader())
                .contains(
                    "Judgment Submitted");
        }
    }

    @Nested
    class SetUpOveVOneFlag {

        @Test
        void shouldGetOneVOneFullDefenceFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOnePartAdmitFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceAdmittedRequired(YES)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_HAS_PAID);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOnePartAdmitBySetDateFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .specDefenceAdmittedRequired(NO)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_BY_SET_DATE);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @ParameterizedTest
        @EnumSource(value = RespondentResponsePartAdmissionPaymentTimeLRspec.class)
        void shouldGetOneVOnePartAdmitImmediatelyFlagV2(RespondentResponsePartAdmissionPaymentTimeLRspec type) {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(type)
                .specDefenceAdmittedRequired(NO)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isNotNull();
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOneFullAdmitFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YES)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();
            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_HAS_PAID);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOneFullAdmitBySetDateFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(BY_SET_DATE)
                .specDefenceFullAdmittedRequired(NO)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();
            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_BY_SET_DATE);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @ParameterizedTest
        @EnumSource(value = RespondentResponsePartAdmissionPaymentTimeLRspec.class)
        void shouldGetOneVOneFullAdmitBySetDateFlagV2Parameterized(
            RespondentResponsePartAdmissionPaymentTimeLRspec type) {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(type)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                   .whenWillThisAmountBePaid(LocalDate.now()).build())
                .specDefenceFullAdmittedRequired(NO)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();
            assertThat(result).isNotNull();
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetOneVOneCounterClaimFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isEqualTo(ResponseOneVOneShowTag.ONE_V_ONE_COUNTER_CLAIM);
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldGetNullFlagV2() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseData.builder()
                .respondent2(PartyBuilder.builder().company().build())
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            ResponseOneVOneShowTag result = getCaseData(response).getShowResponseOneVOneFlag();

            assertThat(result).isNull();
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return objectMapper.convertValue(response.getData(), CaseData.class);
        }
    }

    @Nested
    class SetUpPaymentDateToStringField {
        @Test
        void shouldSetUpPaymentDateToString() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            given(workingDayIndicator.isWorkingDay(any())).willReturn(true);
            LocalDate whenWillPay = LocalDate.now();
            given(deadlineCalculatorService.calculateExtendedDeadline(any(), anyInt())).willReturn(whenWillPay);

            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec =
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build();

            CaseData caseData = CaseData.builder()
                .respondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            String result = getCaseData(response).getRespondent1PaymentDateToStringSpec();

            assertThat(result).isEqualTo(whenWillPay
                                             .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)));
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldSetUpPaymentDateToStringForPartAdmitPaid() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            given(deadlineCalculatorService.calculateExtendedDeadline(any(), anyInt())).willReturn(whenWillPay);

            RespondToClaim respondToAdmittedClaim =
                RespondToClaim.builder()
                    .howMuchWasPaid(null)
                    .whenWasThisAmountPaid(whenWillPay)
                    .build();

            CaseData caseData = CaseData.builder()
                .respondToAdmittedClaim(respondToAdmittedClaim)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            String result = getCaseData(response).getRespondent1PaymentDateToStringSpec();

            assertThat(result).isEqualTo(whenWillPay
                                             .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)));
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        @Test
        void shouldSetUpPaymentDateForResponseDateToString() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            given(workingDayIndicator.isWorkingDay(any())).willReturn(true);
            LocalDate whenWillPay = LocalDate.now();
            given(deadlineCalculatorService.calculateExtendedDeadline(any(), anyInt())).willReturn(whenWillPay);

            CaseData caseData = CaseData.builder()
                .respondent1ResponseDate(LocalDateTime.now())
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            String result = getCaseData(response).getRespondent1PaymentDateToStringSpec();

            assertThat(result).isEqualTo(whenWillPay
                                             .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)));
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return objectMapper.convertValue(response.getData(), CaseData.class);
        }

    }

    @Nested
    class PaymentDateValidationCallback {

        private static final String PAGE_ID = "validate-respondent-payment-date";

        @Test
        void shouldReturnError_whenPastPaymentDate() {
            PaymentBySetDate paymentBySetDate = PaymentBySetDate.builder()
                .paymentSetDate(LocalDate.now().minusDays(15)).build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .applicant1RequestedPaymentDateForDefendantSpec(paymentBySetDate)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Enter a date that is today or in the future");
        }

        @Test
        void shouldNotReturnError_whenFuturePaymentDate() {
            PaymentBySetDate paymentBySetDate = PaymentBySetDate.builder()
                .paymentSetDate(LocalDate.now().plusDays(15)).build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .applicant1RequestedPaymentDateForDefendantSpec(paymentBySetDate)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class SetUpPaymentAmountField {

        @Test
        void shouldConvertPartAdmitPaidValueFromPenniesToPounds() {
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            RespondToClaim respondToAdmittedClaim =
                RespondToClaim.builder()
                    .howMuchWasPaid(BigDecimal.valueOf(1050))
                    .build();

            CaseData caseData = CaseData.builder()
                .respondToAdmittedClaim(respondToAdmittedClaim)
                .totalClaimAmount(BigDecimal.valueOf(5000_00))
                .build();
            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            BigDecimal result = getCaseData(response).getPartAdmitPaidValuePounds();

            assertThat(result).isEqualTo(new BigDecimal("10.50"));
            assertThat(getCaseData(response).getResponseClaimTrack()).isNotNull();
        }
    }

    @Nested
    class MidEventCallbackValidateAmountPaidFlag {

        private static final String PAGE_ID = "validate-amount-paid";

        @Test
        void shouldCheckValidateAmountPaid_withErrorMessage() {

            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(150000))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(new BigDecimal(1000))
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("The amount paid must be less than the full claim amount.");
        }
    }

    @Nested
    class MidEventCallbackSetUpCcjSummaryPage {

        private static final String PAGE_ID = "set-up-ccj-amount-summary";

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceed() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount());
            assertThat(subTotal).isEqualTo(expectedSubTotal);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal.subtract(BigDecimal.valueOf(100)));
        }

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceedWhenPartPaymentAccepted() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .applicant1AcceptPartAdmitPaymentPlanSpec(YES)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(500))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimAmount = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount();
            assertThat(claimAmount).isEqualTo(BigDecimal.valueOf(500));

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount());
            assertThat(subTotal).isEqualTo(expectedSubTotal);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal.subtract(BigDecimal.valueOf(100)));
        }

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceedWithFixedCost() {
            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentFixedCostOption(YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            BigDecimal claimFee = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimFee();
            assertThat(claimFee).isEqualTo(MonetaryConversions.penniesToPounds(fee.getCalculatedAmountInPence()));

            BigDecimal subTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentSummarySubtotalAmount();
            BigDecimal expectedSubTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentAmountClaimAmount()
                .add(caseData.getTotalInterest())
                .add(caseData.getClaimFee().toFeeDto().getCalculatedAmount())
                .add(BigDecimal.valueOf(40));
            BigDecimal fixedCost = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentFixedCostAmount();
            BigDecimal expectedFixedCost = BigDecimal.valueOf(40);
            assertThat(subTotal).isEqualTo(expectedSubTotal);
            assertThat(fixedCost).isEqualTo(expectedFixedCost);

            BigDecimal finalTotal = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentTotalStillOwed();
            assertThat(finalTotal).isEqualTo(subTotal.subtract(BigDecimal.valueOf(100)));
        }

        @Test
        void shouldSetTheJudgmentSummaryDetailsToProceedWithoutDefendantSolicitor() {
            String expected = "The Judgement request will be reviewed by the court, this case will proceed offline, you will receive any further updates by post.";

            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentFixedCostOption(YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .specRespondent1Represented(NO)
                .build();
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String judgementStatement = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentStatement();

            assertThat(judgementStatement).isEqualTo(expected);
        }

        @Test
        void shouldReturnCorrectSummaryForAllFinalsOrderIssued() {
            String expected = "The judgment request will be processed and a County"
                + " Court Judgment (CCJ) will be issued, you will receive any further updates by email.";

            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

            Fee fee = Fee.builder().version("1").code("CODE").calculatedAmountInPence(BigDecimal.valueOf(100)).build();
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeAmount(BigDecimal.valueOf(10000))
                .ccjPaymentPaidSomeOption(YesOrNo.YES)
                .ccjJudgmentFixedCostOption(YES)
                .build();

            BigDecimal interestAmount = BigDecimal.valueOf(100);
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .applicant1Represented(YES)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("John Doe")
                                                     .build())
                                          .build())
                .ccjPaymentDetails(ccjPaymentDetails)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .claimFee(fee)
                .totalInterest(interestAmount)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String judgementStatement = getCaseData(response).getCcjPaymentDetails().getCcjJudgmentStatement();

            assertThat(judgementStatement).isEqualTo(expected);
        }
    }

    @Nested
    class MidEventCallbackSetMediationShowFlag {

        private static final String PAGE_ID = "set-mediation-show-tag";

        @Test
        void shouldSetMediationShowFlag_whenGivenConditionMeets() {
            CaseData caseData = CaseDataBuilder.builder().build();
            given(respondentMediationService.setMediationRequired(any())).willReturn(DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE);
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            Set<DefendantResponseShowTag> showConditionFlags = getCaseData(response).getShowConditionFlags();
            assertThat(showConditionFlags).contains(DefendantResponseShowTag.CLAIMANT_MEDIATION_ONE_V_ONE);
        }

        @Test
        void shouldSetVulnerability_whenGivenConditionMeets() {
            given(respondentMediationService.setMediationRequired(any())).willReturn(DefendantResponseShowTag.VULNERABILITY);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateSpec1v1ClaimSubmitted()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
                .build();
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).extracting("showConditionFlags").asList()
                .contains(DefendantResponseShowTag.VULNERABILITY.name());
        }

        @Test
        void shouldNotSetMediationShowFlag_whenGivenConditionNotMeet() {
            CaseData caseData = CaseDataBuilder.builder().build();
            given(respondentMediationService.setMediationRequired(any())).willReturn(null);
            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("showConditionFlags").asList().hasSizeLessThan(1);
        }
    }

    @Nested
    class AboutToSubmitCallbackForLiP {
        private final LocalDateTime localDateTime = now();

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(localDateTime);
        }

        @Test
        void shouldUpdateToCaseSettled_whenClaimantChooseToSettle() {

            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .applicant1PartAdmitIntentionToSettleClaimSpec(YES)
                .applicant1PartAdmitConfirmAmountPaidSpec(YES)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("11111").region("2").build())
                .build();

            CallbackParams params = callbackParamsOf(V_2, caseData, ABOUT_TO_SUBMIT);
            when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getState()).isEqualTo(CaseState.CASE_SETTLED.name());
        }
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(CLAIMANT_RESPONSE_SPEC);
    }

    @Nested
    class InstalmentsValidationCallback {
        private static final String PAGE_ID = "validate-suggest-instalments";
        LocalDate inputDate = LocalDate.now().plusDays(31);

        @Test
        void shouldReturnError_whenAmountIsZero() {
            CaseData caseData = CaseData.builder()
                .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(BigDecimal.ZERO)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(inputDate)
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0))
                .isEqualTo("Enter an amount of £1 or more");
        }

        @Test
        void shouldReturnError_whenAmountIsLarger() {
            CaseData caseData = CaseData.builder()
                .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(BigDecimal.valueOf(150000))
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(inputDate)
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0))
                .isEqualTo("Enter a valid amount for equal instalments");
        }

        @Test
        void shouldReturnError_whenDateIsBefore() {
            LocalDate eligibleDate = LocalDate.now().plusDays(30);
            var testDate = LocalDate.now().plusDays(25);

            CaseData caseData = CaseData.builder()
                .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(BigDecimal.valueOf(100))
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(testDate)
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0))
                .isEqualTo("Selected date must be after " + formatLocalDate(
                    eligibleDate,
                    DATE
                ));
        }
    }

    @Nested
    class MidEventPaymentDate {
        private static final String PAGE_ID = "get-payment-date";

        @Test
        void shouldReturnError_whenDateIsBefore() {
            CaseData caseData = CaseData.builder()
                .build();

            CallbackParams params = callbackParamsOf(V_1, caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String paymentDate = getCaseData(response).getCurrentDateboxDefendantSpec();
            assertThat(paymentDate).isEqualTo(formatLocalDateTime(LocalDateTime.now().plusDays(30), DATE));
        }
    }

    @Nested
    class MidValidateMediationUnavailabiltyDates {

        @Test
        public void testValidateApplicantUnavailableDateWhenAvailabilityIsNo() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .addApplicant2(YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            CaseData updatedCaseData = caseData.toBuilder()
                .app1MediationAvailability(MediationAvailability.builder().isMediationUnavailablityExists(NO).build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        public void testValidateApp1UnavailableDateWhenAvailabilityIsYesAndSingleDate() {

            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.now().plusDays(4))
                    .build(),
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.DATE_RANGE)
                    .fromDate(LocalDate.now().plusDays(4))
                    .toDate(LocalDate.now().plusDays(6))
                    .build()
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .addApplicant2(YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            CaseData updatedCaseData = caseData.toBuilder()
                .app1MediationAvailability(MediationAvailability.builder().isMediationUnavailablityExists(YES)
                                                .unavailableDatesForMediation(unAvailableDates)
                                                .build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        public void testValidateApp1UnavailableDateWhenAvailabilityIsYesAndSingleDateErrored() {

            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.now().minusDays(4))
                    .build(),
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.DATE_RANGE)
                    .fromDate(LocalDate.now().plusDays(4))
                    .toDate(LocalDate.now().plusDays(6))
                    .build()
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .addApplicant2(YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            CaseData updatedCaseData = caseData.toBuilder()
                .app1MediationAvailability(MediationAvailability.builder().isMediationUnavailablityExists(YES)
                                                .unavailableDatesForMediation(unAvailableDates)
                                                .build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains("Unavailability Date must not be before today.");
        }

        @Test
        public void testApp1UnavailableDateWhenAvailabilityIsYesAndSingleDateIsBeyond3Months() {

            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.now().plusMonths(4))
                    .build(),
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.DATE_RANGE)
                    .fromDate(LocalDate.now().plusDays(4))
                    .toDate(LocalDate.now().plusDays(6))
                    .build()
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .addApplicant2(YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            CaseData updatedCaseData = caseData.toBuilder()
                .app1MediationAvailability(MediationAvailability.builder().isMediationUnavailablityExists(YES)
                                                .unavailableDatesForMediation(unAvailableDates)
                                                .build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains("Unavailability Date must not be more than three months in the future.");
        }

        @Test
        public void testApp1UnavailableDateWhenDateToIsBeforeDateFrom() {

            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.now().plusDays(4))
                    .build(),
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.DATE_RANGE)
                    .fromDate(LocalDate.now().plusDays(6))
                    .toDate(LocalDate.now().plusDays(4))
                    .build()
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .addApplicant2(YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            CaseData updatedCaseData = caseData.toBuilder()
                .app1MediationAvailability(MediationAvailability.builder().isMediationUnavailablityExists(YES)
                                                .unavailableDatesForMediation(unAvailableDates)
                                                .build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains("Unavailability Date From cannot be after Unavailability Date To. Please enter valid range.");
        }

        @Test
        public void testApp1UnavailableDateWhenDateFromIsBeforeToday() {

            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.now().plusDays(4))
                    .build(),
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.DATE_RANGE)
                    .fromDate(LocalDate.now().minusDays(6))
                    .toDate(LocalDate.now().plusDays(4))
                    .build()
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .addApplicant2(YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            CaseData updatedCaseData = caseData.toBuilder()
                .app1MediationAvailability(MediationAvailability.builder().isMediationUnavailablityExists(YES)
                                                .unavailableDatesForMediation(unAvailableDates)
                                                .build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains("Unavailability Date From must not be before today.");
        }

        @Test
        public void testApp1UnavailableDateWhenDateToIsBeforeToday() {

            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.now().plusDays(4))
                    .build(),
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.DATE_RANGE)
                    .fromDate(LocalDate.now().plusDays(6))
                    .toDate(LocalDate.now().minusDays(4))
                    .build()
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .addApplicant2(YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            CaseData updatedCaseData = caseData.toBuilder()
                .app1MediationAvailability(MediationAvailability.builder().isMediationUnavailablityExists(YES)
                                                .unavailableDatesForMediation(unAvailableDates)
                                                .build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains("Unavailability Date From cannot be after Unavailability Date To. Please enter valid range.");
        }

        @Test
        public void testApp1UnavailableDateWhenDateToIsBeyondThreeMonths() {

            List<Element<UnavailableDate>> unAvailableDates = Stream.of(
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                    .date(LocalDate.now().plusDays(4))
                    .build(),
                UnavailableDate.builder()
                    .unavailableDateType(UnavailableDateType.DATE_RANGE)
                    .fromDate(LocalDate.now().plusDays(6))
                    .toDate(LocalDate.now().plusMonths(4))
                    .build()
            ).map(ElementUtils::element).toList();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .addApplicant2(YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .build();
            CaseData updatedCaseData = caseData.toBuilder()
                .app1MediationAvailability(MediationAvailability.builder().isMediationUnavailablityExists(YES)
                                                .unavailableDatesForMediation(unAvailableDates)
                                                .build()).build();
            CallbackParams params = callbackParamsOf(updatedCaseData, MID, "validate-mediation-unavailable-dates");
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains("Unavailability Date To must not be more than three months in the future.");
        }

    }
}

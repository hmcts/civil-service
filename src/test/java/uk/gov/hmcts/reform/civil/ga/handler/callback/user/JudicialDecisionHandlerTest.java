package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.ga.enums.MakeAppAvailableCheckGAspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAMakeApplicationAvailableCheck;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderCourtOwnInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderWithoutNoticeGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.sampledata.PDFBuilder;
import uk.gov.hmcts.reform.civil.ga.service.AssignCaseToRespondentSolHelper;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionWrittenRepService;
import uk.gov.hmcts.reform.civil.ga.service.JudicialTimeEstimateHelper;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.directionorder.DirectionOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.DismissalOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.FreeFormOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.GeneralOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.HearingOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.RequestForInformationGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.WrittenRepresentationConcurrentOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.WrittenRepresentationSequentialOrderGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_DECISION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec.OPTION_1;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec.OPTION_2;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec.OPTION_3;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.LIST_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_AN_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionWrittenRepService.WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class JudicialDecisionHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private JudicialDecisionHandler handler;

    @Mock
    private JudicialDecisionWrittenRepService service;

    @Mock
    private JudicialDecisionHelper helper;

    @Mock
    private GeneralAppLocationRefDataService locationRefDataService;

    @Mock
    private Time time;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @Mock
    private GaForLipService gaForLipService;

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private GeneralOrderGenerator generalOrderGenerator;

    @Mock
    private RequestForInformationGenerator requestForInformationGenerator;

    @Mock
    private DirectionOrderGenerator directionOrderGenerator;

    @Mock
    private DismissalOrderGenerator dismissalOrderGenerator;

    @Mock
    private HearingOrderGenerator hearingOrderGenerator;

    @Mock
    private WrittenRepresentationConcurrentOrderGenerator writtenRepresentationConcurrentOrderGenerator;

    @Mock
    private WrittenRepresentationSequentialOrderGenerator writtenRepresentationSequentialOrderGenerator;

    @Mock
    private FreeFormOrderGenerator gaFreeFormOrderGenerator;

    @Mock
    private IdamClient idamClient;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private JudicialTimeEstimateHelper timeEstimateHelper;

    @Mock
    private AssignCaseToRespondentSolHelper assignCaseToResopondentSolHelper;

    private static final String CAMUNDA_EVENT = "INITIATE_GENERAL_APPLICATION";
    private static final String BUSINESS_PROCESS_INSTANCE_ID = "11111";
    private static final String ACTIVITY_ID = "anyActivity";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yy");
    private static final DateTimeFormatter DATE_FORMATTER_SUBMIT_CALLBACK = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String EXPECTED_DISMISSAL_ORDER = "This application is dismissed.\n\n";

    private static final String JUDICIAL_REQUEST_MORE_INFO_RECITAL_TEXT = "<Title> <Name> \n"
        + "Upon reviewing the application made and upon considering the information "
        + "provided by the parties, the court requests more information from the applicant.";

    public static final String MAKE_DECISION_APPROVE_BY_DATE_IN_PAST = "The date entered cannot be in the past.";

    private static final String ON_INITIATIVE_SELECTION_TEST = "As this order was made on the court's own initiative, "
        + "any party affected by the order may apply to set aside, vary, or stay the order."
        + " Any such application must be made by 4pm on";
    private static final String WITHOUT_NOTICE_SELECTION_TEXT = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary, or stay the order."
        + " Any such application must be made by 4pm on";

    private static final LocalDate localDatePlus7days = LocalDate.now().plusDays(7);

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(MAKE_DECISION);
    }

    @Nested
    class AboutToStartCallbackHandling {

        @BeforeEach
        void setUp() {
            when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(), anyInt())).thenReturn(localDatePlus7days);
            when(idamClient
                     .getUserInfo(any()))
                .thenReturn(UserInfo.builder().givenName("John").familyName("Doe").build());
        }

        YesOrNo hasRespondentResponseVul = NO;

        @Test
        void testAboutToStartForHearingGeneralOrderRecital() {

            String expecetedJudicialTimeEstimateText = "Both applicant and respondent estimate it would take %s.";
            String expecetedJudicialPreferrenceText = "Both applicant and respondent prefer %s.";
            when(helper.isApplicantAndRespondentLocationPrefSame(any())).thenReturn(true);
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, YES), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getData().get("judgeTitle").toString()).isEqualTo("John Doe");
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingTimeEstimateText1())
                .isEqualTo(String.format(expecetedJudicialTimeEstimateText, getHearingOrderApplnAndResp(types, NO, YES)
                    .getGeneralAppHearingDetails().getHearingDuration().getDisplayedValue()));
            assertThat(responseCaseData.getHearingPreferencesPreferredTypeLabel1())
                .isEqualTo(String.format(expecetedJudicialPreferrenceText, getHearingOrderApplnAndResp(types, NO, YES)
                    .getGeneralAppHearingDetails().getHearingPreferencesPreferredType().getDisplayedValue()));

        }

        @Test
        void testAboutToStartForApplicationCloakedForLipCase() {

            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getGaCaseAppln(types, NO, NO), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getData().get("applicationIsCloaked")).isEqualTo("Yes");

        }

        @Test
        void testAboutToStartForApplicationCloakedForLipCaseWhenApplicationIsNotCloaked() {

            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getGaCaseAppln(types, NO, NO), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getData().get("applicationIsCloaked")).isEqualTo("Yes");

        }

        @Test
        void testAboutToStartForHearingPreferLocationsApplicantRespondent() {

            String expectedJudicialPreferenceLocationApplicantRespondent1Text =
                "Applicant prefers Location %s. Respondent 1 prefers Location %s.";

            when(helper.isApplicantAndRespondentLocationPrefSame(any())).thenReturn(false);

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            var caseDataApplicantRespondent1 = getHearingOrderAppForCourtLocationPreference(types, YES, YES,
                                                                                            NO);
            var caseDataApplicantRespondent2 = getHearingOrderAppForCourtLocationPreference(types, YES, NO,
                                                                                            YES);

            CallbackParams params = callbackParamsOf(caseDataApplicantRespondent1, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);
            assertThat(responseCaseData.getJudgeHearingCourtLocationText1())
                .isEqualTo(String.format(expectedJudicialPreferenceLocationApplicantRespondent1Text,
                                         caseDataApplicantRespondent1.getGeneralAppHearingDetails()
                                             .getHearingPreferredLocation().getValue().getLabel(),
                                         caseDataApplicantRespondent1.getRespondentsResponses().getFirst().getValue()
                                             .getGaHearingDetails().getHearingPreferredLocation().getValue()
                                             .getLabel()));

            params = callbackParamsOf(caseDataApplicantRespondent2, ABOUT_TO_START);
            response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            responseCaseData = getJudicialHearingOrder(response);
            String expectedJudicialPreferenceLocationApplicantRespondent2Text =
                "Applicant prefers Location %s. Respondent 2 prefers Location %s.";

            assertThat(response).isNotNull();
            assertThat(responseCaseData.getJudgeHearingCourtLocationText1())
                .isEqualTo(String.format(expectedJudicialPreferenceLocationApplicantRespondent2Text,
                                         caseDataApplicantRespondent2.getGeneralAppHearingDetails()
                                             .getHearingPreferredLocation().getValue().getLabel(),
                                         caseDataApplicantRespondent2.getRespondentsResponses().get(1).getValue()
                                             .getGaHearingDetails().getHearingPreferredLocation().getValue()
                                             .getLabel()));

        }

        @Test
        void testAboutToStartForHearingOnlyRespondent1Respondent2LocationPreference() {

            String expectedOnlyRespondent1LocationText =
                "Respondent 1 prefers Location %s. Respondent 2 prefers Location %s.";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            var caseData = getHearingOrderAppForCourtLocationPreference(types, NO, YES, YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingCourtLocationText1())
                .isEqualTo(String.format(expectedOnlyRespondent1LocationText,
                                         caseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails()
                                             .getHearingPreferredLocation().getValue().getLabel(),
                                         caseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails()
                                            .getHearingPreferredLocation().getValue().getLabel()));
        }

        @Test
        void testAboutToStartForHearingOnlyRespondent1LocationPreference() {

            String expectedOnlyRespondent1LocationText = "Respondent 1 prefers Location %s.";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            var caseData = getHearingOrderAppForCourtLocationPreference(types, NO, YES, NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingCourtLocationText1())
                .isEqualTo(String.format(expectedOnlyRespondent1LocationText,
                                         caseData.getRespondentsResponses().getFirst().getValue().getGaHearingDetails()
                                             .getHearingPreferredLocation().getValue().getLabel()));
        }

        @Test
        void testAboutToStartForHearingOnlyRespondent2LocationPreference() {

            String expectedOnlyRespondent2LocationText = "Respondent 2 prefers Location %s.";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            var caseData = getHearingOrderAppForCourtLocationPreference(types, NO, NO, YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingCourtLocationText1())
                .isEqualTo(String.format(expectedOnlyRespondent2LocationText,
                                         caseData.getRespondentsResponses().get(1).getValue().getGaHearingDetails()
                                             .getHearingPreferredLocation().getValue().getLabel()));
        }

        @Test
        void testAboutToStartForHearingOnlyApplicantLocationPreference() {

            String expectedOnlyRespondent1LocationText = "Applicant prefers Location %s.";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            var caseData = getHearingOrderAppForCourtLocationPreference(types, YES, NO, NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingCourtLocationText1())
                .isEqualTo(String.format(expectedOnlyRespondent1LocationText, caseData.getGeneralAppHearingDetails()
                    .getHearingPreferredLocation().getValue().getLabel()));
        }

        @Test
        void testSolicitorsHearingDetailsWithNoSupportText() {

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
            respondentsResponses
                .add(element(new GARespondentResponse()
                                 .setGaHearingDetails(GAHearingDetails.builder()
                                                       .vulnerabilityQuestionsYesOrNo(YES)
                                                       .vulnerabilityQuestion("dummy1")
                                                       .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                       .hearingDuration(GAHearingDuration.HOUR_1)
                                                       .hearingPreferredLocation(getLocationDynamicList())
                                                       .build())
                                 .setGaRespondentDetails("1L")));
            respondentsResponses
                .add(element(new GARespondentResponse()
                                 .setGaHearingDetails(GAHearingDetails.builder()
                                                       .vulnerabilityQuestionsYesOrNo(YES)
                                                       .vulnerabilityQuestion("dummy2")
                                                       .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                       .hearingDuration(GAHearingDuration.MINUTES_30)
                                                       .hearingPreferredLocation(getLocationDynamicList())
                                                       .build())
                                 .setGaRespondentDetails("2L")));

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp1and2(types, NO, YES, YES);
            GeneralApplicationCaseData builder = caseData.copy();
            builder.respondentsResponses(respondentsResponses);
            builder.generalAppHearingDetails(GAHearingDetails.builder()
                                                  .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                  .hearingDuration(GAHearingDuration.HOUR_1)
                                                  .build());

            CallbackParams params = callbackParamsOf(builder.build(), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            String expectedJudicialSupportText =
                "Applicant requires no support. Respondent 1 requires no support. Respondent 2 requires no support.";

            assertThat(responseCaseData.getJudgeHearingSupportReqText1())
                .isEqualTo(expectedJudicialSupportText);

        }

        @Test
        void testApplicant_Resp1_WithSupportText_Resp2_NoSupportText() {

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
            respondentsResponses
                .add(element(new GARespondentResponse()
                                 .setGaHearingDetails(GAHearingDetails.builder()
                                                       .vulnerabilityQuestionsYesOrNo(YES)
                                                       .vulnerabilityQuestion("dummy1")
                                                       .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                       .hearingDuration(GAHearingDuration.HOUR_1)
                                                       .hearingPreferredLocation(getLocationDynamicList())
                                                       .supportRequirement(getApplicant1Responses())
                                                       .build())
                                 .setGaRespondentDetails("1L")));
            respondentsResponses
                .add(element(new GARespondentResponse()
                                 .setGaHearingDetails(GAHearingDetails.builder()
                                                       .vulnerabilityQuestionsYesOrNo(YES)
                                                       .vulnerabilityQuestion("dummy2")
                                                       .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                       .hearingDuration(GAHearingDuration.MINUTES_30)
                                                       .hearingPreferredLocation(getLocationDynamicList())
                                                       .build())
                                 .setGaRespondentDetails("2L")));

            String expecetedJudicialSupportText =
                "Applicant requires Hearing loop. Respondent 1 requires Hearing loop. "
                    + "Respondent 2 requires no support.";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp1and2(types, NO, YES, YES);
            GeneralApplicationCaseData builder = caseData.copy();
            builder.respondentsResponses(respondentsResponses);

            CallbackParams params = callbackParamsOf(builder.build(), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingSupportReqText1())
                .isEqualTo(expecetedJudicialSupportText);

        }

        @Test
        void testApplicant_Resp1_WithNoSupportText_Resp2_SupportText() {

            List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
            respondentsResponses
                .add(element(new GARespondentResponse()
                                 .setGaHearingDetails(GAHearingDetails.builder()
                                                       .vulnerabilityQuestionsYesOrNo(YES)
                                                       .vulnerabilityQuestion("dummy1")
                                                       .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                       .hearingDuration(GAHearingDuration.HOUR_1)
                                                       .hearingPreferredLocation(getLocationDynamicList())
                                                       .build())
                                 .setGaRespondentDetails("1L")));
            respondentsResponses
                .add(element(new GARespondentResponse()
                                 .setGaHearingDetails(GAHearingDetails.builder()
                                                       .vulnerabilityQuestionsYesOrNo(YES)
                                                       .vulnerabilityQuestion("dummy2")
                                                       .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                       .hearingDuration(GAHearingDuration.MINUTES_30)
                                                       .hearingPreferredLocation(getLocationDynamicList())
                                                       .supportRequirement(getApplicant1Responses())
                                                       .build())
                                 .setGaRespondentDetails("2L")));

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp1and2(types, NO, YES, YES);
            GeneralApplicationCaseData builder = caseData.copy();
            builder.respondentsResponses(respondentsResponses);
            builder.generalAppHearingDetails(GAHearingDetails.builder()
                                                 .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                 .hearingDuration(GAHearingDuration.HOUR_1)
                                                 .build());

            CallbackParams params = callbackParamsOf(builder.build(), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            String expectedJudicialSupportText =
                "Applicant requires no support. Respondent 1 requires no support. Respondent 2 requires Hearing loop.";

            assertThat(responseCaseData.getJudgeHearingSupportReqText1())
                .isEqualTo(expectedJudicialSupportText);

        }

        @Test
        void testAboutToStartForHearingDetails() {

            String expectedJudicialTimeEstimateText =
                "Applicant estimates %s. Respondent 1 estimates %s. Respondent 2 estimates %s.";
            String expectedJudicialPreferrenceText =
                "Applicant prefers %s. Respondent 1 prefers %s. Respondent 2 prefers %s.";
            String expectedJudicialSupportText =
                "Applicant requires %s. Respondent 1 requires %s. Respondent 2 requires %s.";
            String expectedJudicialPreferenceLocationText =
                "Applicant prefers Location %s. Respondent 1 prefers Location %s. Respondent 2 prefers Location %s.";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(
                getHearingOrderApplnAndResp1and2(types, NO, YES, YES),
                ABOUT_TO_START
            );

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingTimeEstimateText1())
                .isEqualTo(String.format(
                    expectedJudicialTimeEstimateText,
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getGeneralAppHearingDetails().getHearingDuration()
                        .getDisplayedValue(),
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getRespondentsResponses().get(0).getValue().getGaHearingDetails()
                        .getHearingDuration().getDisplayedValue(),
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getRespondentsResponses().get(1).getValue().getGaHearingDetails()
                        .getHearingDuration().getDisplayedValue()
                ));

            assertThat(responseCaseData.getHearingPreferencesPreferredTypeLabel1())
                .isEqualTo(String.format(
                    expectedJudicialPreferrenceText,
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getGeneralAppHearingDetails().getHearingPreferencesPreferredType()
                        .getDisplayedValue(),
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getRespondentsResponses().get(0).getValue().getGaHearingDetails()
                        .getHearingPreferencesPreferredType().getDisplayedValue(),
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getRespondentsResponses().get(1).getValue().getGaHearingDetails()
                        .getHearingPreferencesPreferredType().getDisplayedValue()
                ));
            assertThat(responseCaseData.getJudgeHearingCourtLocationText1())
                .isEqualTo(String.format(
                    expectedJudicialPreferenceLocationText,
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getGeneralAppHearingDetails().getHearingPreferredLocation()
                        .getValue().getLabel(),
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getRespondentsResponses().get(0).getValue().getGaHearingDetails()
                        .getHearingPreferredLocation().getValue().getLabel(),
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getRespondentsResponses().get(1).getValue().getGaHearingDetails()
                        .getHearingPreferredLocation().getValue().getLabel()));

            assertThat(responseCaseData.getJudgeHearingSupportReqText1())
                .isEqualTo(String.format(
                    expectedJudicialSupportText,
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getGeneralAppHearingDetails()
                        .getSupportRequirement().getFirst().getDisplayedValue(),
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getRespondentsResponses().get(0).getValue()
                        .getGaHearingDetails().getSupportRequirement()
                        .getFirst().getDisplayedValue(),
                    getHearingOrderApplnAndResp1and2(types, NO, YES, YES)
                        .getRespondentsResponses().get(1).getValue()
                        .getGaHearingDetails().getSupportRequirement()
                        .getFirst().getDisplayedValue()
                ));

        }

        @Test
        void testAboutToStartForHearingDetails_noTimeEstimates() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp1and2(types, NO, YES, YES);
            List<Element<GARespondentResponse>> respondentResponses = new ArrayList<>();
            caseData.getRespondentsResponses().forEach(
                response -> respondentResponses.add(Element.<GARespondentResponse>builder()
                    .id(response.getId())
                    .value(new GARespondentResponse()
                               .setGaHearingDetails(response.getValue().getGaHearingDetails().toBuilder()
                                                        .hearingDuration(null).build())
                               .setGeneralAppRespondent1Representative(
                                   response.getValue().getGeneralAppRespondent1Representative())
                               .setGaRespondentDetails(response.getValue().getGaRespondentDetails())
                               .setGaRespondentResponseReason(response.getValue().getGaRespondentResponseReason()))
                    .build()));
            caseData = caseData.copy()
                .generalAppHearingDetails(
                    caseData.getGeneralAppHearingDetails().toBuilder()
                        .hearingDuration(null)
                        .build())
                .respondentsResponses(respondentResponses)
                .build();
            CallbackParams params = callbackParamsOf(
                caseData,
                ABOUT_TO_START
            );
            String expectedJudicialTimeEstimateText = "Applicant and respondent have not provided estimates";
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingTimeEstimateText1())
                .isEqualTo(expectedJudicialTimeEstimateText);
        }

        @Test
        void shouldReturnEmptyStringForNullSupportReq() {

            List<Element<GARespondentResponse>> respondentResponse = null;

            GAUrgencyRequirement urgentApp = null;

            CallbackParams params = callbackParamsOf(getCaseDateWithNoSupportReq(
                respondentResponse,
                urgentApp
            ), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingTimeEstimateText1())
                .isEqualTo("Applicant estimates 1 hour");

            assertThat(responseCaseData.getHearingPreferencesPreferredTypeLabel1())
                .isEqualTo("Applicant prefers In person");

            assertThat(responseCaseData.getJudgeHearingSupportReqText1())
                .isEqualTo("Applicant requires no support");

            assertThat(responseCaseData.getJudgeHearingCourtLocationText1())
                .isEqualTo(StringUtils.EMPTY);

        }

        @Test
        void testAboutToStartForHearingScreenForUrgentApp() {

            String expectedJudicialTimeEstimateText = "Applicant estimates 1 hour";
            String expectedJudicialPreferenceText = "Applicant prefers In person";
            String expectedJudicialSupportReqText = "Applicant requires Hearing loop, Other support";

            CallbackParams params = callbackParamsOf(getCaseDateForUrgentApp(), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingTimeEstimateText1())
                .isEqualTo(expectedJudicialTimeEstimateText);

            assertThat(responseCaseData.getHearingPreferencesPreferredTypeLabel1())
                .isEqualTo(expectedJudicialPreferenceText);

            assertThat(responseCaseData.getJudgeHearingSupportReqText1())
                .isEqualTo(expectedJudicialSupportReqText);

        }

        @Test
        void testAboutToStartForHearingScreenForUrgentApp_noTimeEstimates() {

            String expectedJudicialTimeEstimateText = "Applicant and respondent have not provided estimates";

            GeneralApplicationCaseData caseData = getCaseDateForUrgentApp();
            caseData = caseData.copy()
                .generalAppHearingDetails(
                    caseData.getGeneralAppHearingDetails().toBuilder()
                        .hearingDuration(null)
                        .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingTimeEstimateText1())
                .isEqualTo(expectedJudicialTimeEstimateText);
        }

        @Test
        void testHearingScreenSupportReqWithNoApplnHearingSupport() {

            String expectedJudicialSupportReqText = "Applicant requires no support";

            GAUrgencyRequirement urgentApp = GAUrgencyRequirement.builder().generalAppUrgency(YES).build();

            CallbackParams params = callbackParamsOf(getCaseDateWithNoSupportReq(
                null,
                urgentApp
            ), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingSupportReqText1())
                .isEqualTo(expectedJudicialSupportReqText);

        }

        @Test
        void testHearingScreenSupportReqWithNoApplnHearingSupportAndRespWithSupportReq() {

            String expectedJudicialSupportReqText = "Applicant requires no support. "
                + "Respondent requires Other support, Hearing loop.";

            GAUrgencyRequirement urgentApp = GAUrgencyRequirement.builder().generalAppUrgency(YES).build();

            List<Element<GARespondentResponse>> respondentResponse = getRespodentResponses(hasRespondentResponseVul);

            CallbackParams params = callbackParamsOf(getCaseDateWithNoApplicantSupportReq(
                respondentResponse,
                urgentApp
            ), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingSupportReqText1())
                .isEqualTo(expectedJudicialSupportReqText);

        }

        @Test
        void shouldMatchHearingReqForDifferentPreferences() {

            String expectedJudicialTimeEstimateText = "Applicant estimates 45 minutes. Respondent estimates 1 hour.";
            String expectedJudicialPreferenceText = "Applicant prefers Video conference hearing. Respondent "
                + "prefers In person.";
            String expectedJudicialSupportReqText = "Applicant requires Disabled access, Sign language interpreter. "
                + "Respondent requires Other support, Hearing loop.";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getCaseDateWithHearingScreeen1V1(types, NO, YES), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingTimeEstimateText1())
                .isEqualTo(expectedJudicialTimeEstimateText);

            assertThat(responseCaseData.getHearingPreferencesPreferredTypeLabel1())
                .isEqualTo(expectedJudicialPreferenceText);

            assertThat(responseCaseData.getJudgeHearingSupportReqText1())
                .isEqualTo(expectedJudicialSupportReqText);

        }

        @Test
        void shouldMatchHearingReqForDifferentPreferences_noTimeEstimates() {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            GeneralApplicationCaseData caseData = getCaseDateWithHearingScreeen1V1(types, NO, YES);
            List<Element<GARespondentResponse>> respondentResponses = new ArrayList<>();
            caseData.getRespondentsResponses().forEach(
                response -> respondentResponses.add(Element.<GARespondentResponse>builder()
                    .id(response.getId())
                    .value(new GARespondentResponse()
                               .setGaHearingDetails(response.getValue().getGaHearingDetails().toBuilder()
                                                        .hearingDuration(null).build())
                               .setGeneralAppRespondent1Representative(
                                   response.getValue().getGeneralAppRespondent1Representative())
                               .setGaRespondentDetails(response.getValue().getGaRespondentDetails())
                               .setGaRespondentResponseReason(response.getValue().getGaRespondentResponseReason()))
                    .build()));
            caseData = caseData.copy()
                .generalAppHearingDetails(
                    caseData.getGeneralAppHearingDetails().toBuilder()
                        .hearingDuration(null)
                        .build())
                .respondentsResponses(respondentResponses)
                .build();

            String expectedJudicialTimeEstimateText = "Applicant and respondent have not provided estimates";

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudgeHearingTimeEstimateText1())
                .isEqualTo(expectedJudicialTimeEstimateText);
        }

        @Test
        void testAboutToStartForNotifiedApplication() {
            String expectedRecitalText = "The Judge considered the application of the claimant dated 15 January 2022\n\n";
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            CallbackParams params = callbackParamsOf(getNotifiedApplication(YES, YES), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(getApplicationIsCloakedStatus(response)).isEqualTo(NO);
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getJudgeRecitalText()).isEqualTo(String.format(
                expectedRecitalText,
                DATE_FORMATTER.format(LocalDate.now())
            ));
            assertThat(makeAnOrder.getDismissalOrderText()).isEqualTo(EXPECTED_DISMISSAL_ORDER);
        }

        @Test
        void testAboutToStartForNotifiedApplicationInitiatedByDefendant() {
            String expectedRecitalText = "The Judge considered the application of the defendant dated 15 January 2022\n\n";
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            CallbackParams params = callbackParamsOf(getNotifiedApplication(YES, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(getApplicationIsCloakedStatus(response)).isEqualTo(NO);
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getJudgeRecitalText()).isEqualTo(String.format(
                expectedRecitalText,
                DATE_FORMATTER.format(LocalDate.now())
            ));
            assertThat(makeAnOrder.getDismissalOrderText()).isEqualTo(EXPECTED_DISMISSAL_ORDER);
        }

        @Test
        void testAboutToStartForCloakedApplicationInitiatedByClaimant() {
            String expectedRecitalText = "The Judge considered the without notice application of the claimant dated 15 January 2022\n\n"
                + "And the Judge considered the information provided by the claimant";
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            CallbackParams params = callbackParamsOf(getCloakedApplication(YES), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(getApplicationIsCloakedStatus(response)).isEqualTo(YES);
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getJudgeRecitalText()).isEqualTo(String.format(
                expectedRecitalText,
                DATE_FORMATTER.format(LocalDate.now())
            ));
            assertThat(makeAnOrder.getDismissalOrderText()).isEqualTo(EXPECTED_DISMISSAL_ORDER);
        }

        @Test
        void testAboutToStartForUnCloakedApplicationInitiatedByDefendant() {
            String expectedRecitalText = "The Judge considered the without notice application of the defendant dated 15 January 2022\n\n"
                + "And the Judge considered the information provided by the defendant";
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            CallbackParams params = callbackParamsOf(getCloakedApplication(NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(getApplicationIsCloakedStatus(response)).isEqualTo(YES);
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getJudgeRecitalText()).isEqualTo(String.format(
                expectedRecitalText,
                DATE_FORMATTER.format(LocalDate.now())
            ));
            assertThat(makeAnOrder.getDismissalOrderText()).isEqualTo(EXPECTED_DISMISSAL_ORDER);
        }

        @Test
        void testAboutToStartForRequestMoreInfoCloakedAppln() {

            // Without notice application
            String judgeRecitalText = "The Judge considered the without notice application of the claimant dated 15 January 2022\n\n"
                + "And the Judge considered the information provided by the claimant";

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            CallbackParams params = callbackParamsOf(getCloakedApplication(YES), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialRequestMoreInfo gaJudicialRequestMoreInfo = getJudicialRequestMoreInfo(response);

            assertThat(gaJudicialRequestMoreInfo.getIsWithNotice()).isEqualTo(NO);
            assertThat(gaJudicialRequestMoreInfo.getJudgeRecitalText()).isEqualTo(judgeRecitalText);
        }

        @Test
        void testJudgeRecitalTextForRequestMoreInfoCloakedApplnByDefendant() {

            // Without Notice application by Civil Defendant
            String judgeRecitalText = "The Judge considered the without notice application of the defendant dated 15 January 2022\n\n"
                + "And the Judge considered the information provided by the defendant";

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            CallbackParams params = callbackParamsOf(getCloakedApplication(NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialRequestMoreInfo gaJudicialRequestMoreInfo = getJudicialRequestMoreInfo(response);

            assertThat(gaJudicialRequestMoreInfo.getJudgeRecitalText()).isEqualTo(judgeRecitalText);
        }

        @Test
        void testAboutToStartForRequestMoreInfoUrgentAppln() {

            // With notice application by Claimant
            String judgeRecitalText = "The Judge considered the application of the claimant dated 15 January 2022\n\n";
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            CallbackParams params = callbackParamsOf(getCaseDateForUrgentApp(), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialRequestMoreInfo gaJudicialRequestMoreInfo = getJudicialRequestMoreInfo(response);

            assertThat(gaJudicialRequestMoreInfo.getIsWithNotice()).isEqualTo(YES);
            assertThat(gaJudicialRequestMoreInfo.getJudgeRecitalText()).isEqualTo(judgeRecitalText);
        }

        @Test
        void testJudgeRecitalTextForRequestMoreInfoWithNoticeByDefendant() {
            String judgeRecitalText = "The Judge considered the application of the defendant dated 15 January 2022\n\n";

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            // isWithNotice = YES
            CallbackParams params = callbackParamsOf(getApplicationByParentCaseDefendant(), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialRequestMoreInfo gaJudicialRequestMoreInfo = getJudicialRequestMoreInfo(response);

            assertThat(gaJudicialRequestMoreInfo.getJudgeRecitalText()).isEqualTo(judgeRecitalText);
        }

        @Test
        void testAboutToStartForDefendant_judgeRecitalText() {
            String expectedRecitalText = "The Judge considered the application of the defendant dated 15 January 2022\n\n";

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            CallbackParams params = callbackParamsOf(getApplicationByParentCaseDefendant(), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(getApplicationIsCloakedStatus(response)).isEqualTo(NO);
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getJudgeRecitalText()).isEqualTo(String.format(
                expectedRecitalText,
                DATE_FORMATTER.format(LocalDate.now())
            ));
            assertThat(makeAnOrder.getDismissalOrderText()).isEqualTo(EXPECTED_DISMISSAL_ORDER);
        }

        @Test
        void testAboutToStartForDefendant_orderText() {
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            CallbackParams params = callbackParamsOf(getApplicationByParentCaseDefendant(), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(getApplicationIsCloakedStatus(response)).isEqualTo(NO);
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getOrderText())
                .isEqualTo("Draft order text entered by applicant.");

        }

        @Test
        void shouldReturnYesForJudgeApproveEditOptionDateIfGATypeIsStayClaim() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STAY_THE_CLAIM));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDate())
                .isEqualTo(YES);

        }

        @Test
        void shouldReturnYesForJudgeApproveEditOptionDateIfGATypeIsStayClaimAndExtendTime() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM),
                (GeneralApplicationTypes.EXTEND_TIME)
            );

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDate())
                .isEqualTo(YES);

        }

        @Test
        void shouldReturnNOForJudgeApproveEditOptionDateIfGATypesIsNotEitherExtendTimeORStayClaim() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDate())
                .isEqualTo(NO);

        }

        @Test
        void shouldReturnYESForjudgeApproveEditOptionDateIfGATypesIsNotEitherExtendTimeORStayClaim() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STAY_THE_CLAIM), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDate())
                .isEqualTo(YES);

        }

        @Test
        void shouldReturnYesForJudgeApproveEditOptionPartyIfGATypeIsExtendTime() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.EXTEND_TIME));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDoc())
                .isEqualTo(YES);

        }

        @Test
        void shouldReturnYesForJudgeApproveEditOptionPartyIfGATypeIsStrikeOut() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDoc())
                .isEqualTo(YES);

        }

        @Test
        void shouldReturnYesForJudgeApproveEditOptionPartyIfGATypeIsStayClaimAndExtendTime() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.STRIKE_OUT),
                (GeneralApplicationTypes.EXTEND_TIME)
            );

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDoc())
                .isEqualTo(YES);

        }

        @Test
        void shouldReturnCorrectDirectionOrderText_whenJudgeMakeDecisionGiveDirection() {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            CallbackParams params = callbackParamsOf(getDirectionOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDirectionsText()).isNull();
            assertThat(makeAnOrder.getOrderWithoutNoticeDate()).isEqualTo(localDatePlus7days);
            assertThat(makeAnOrder.getOrderCourtOwnInitiativeDate()).isEqualTo(localDatePlus7days);
        }

        @Test
        void shouldReturnNOForJudgeApproveEditOptionPartyIfGATypesIsNotEitherExtendTimeORStayClaim() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDoc())
                .isEqualTo(NO);

        }

        @Test
        void shouldReturnNOForjudgeApproveEditOptionDocIfGATypesIsNotEitherExtendTimeORStrikeOut() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDoc())
                .isEqualTo(YES);

        }

        @Test
        void shouldMatchExpectedVulnerabilityText() {

            String expectedVulnerabilityText = """
                Applicant requires support with regards to vulnerability
                dummy

                Respondent requires support with regards to vulnerability
                dummy""";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, YES, YES), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldHaveVulTextWithRespondentRespond() {

            String expectedVulnerabilityText = "Respondent requires support with regards to vulnerability\n"
                + "dummy";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(
                getHearingOrderApplnAndResp(types, NO, YES),
                ABOUT_TO_START
            );
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldHaveVulTextWithRespondent1and2Respond() {

            String expectedVulnerabilityText = "\n\nRespondent 1 requires support with regards to vulnerability\n"
                + "dummy1\n\nRespondent 2 requires support with regards to vulnerability\ndummy2";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(
                getHearingOrderApplnAndResp1and2(types, NO, YES, YES),
                ABOUT_TO_START
            );
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldHaveVulTextWithApplicantRespondent1and2Respond() {

            String expectedVulnerabilityText = "Applicant requires support with regards to vulnerability\ndummy"
                + "\n\nRespondent 1 requires support with regards to vulnerability\n"
                + "dummy1\n\nRespondent 2 requires support with regards to vulnerability\ndummy2";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(
                getHearingOrderApplnAndResp1and2(types, YES, YES, YES),
                ABOUT_TO_START
            );
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldHaveVulTextWithApplicantRespondent1Respond() {

            String expectedVulnerabilityText = "Applicant requires support with regards to vulnerability\ndummy"
                + "\n\nRespondent 1 requires support with regards to vulnerability\n"
                + "dummy1";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(
                getHearingOrderApplnAndResp1and2(types, YES, YES, NO),
                ABOUT_TO_START
            );
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldHaveVulTextWithApplicantRespondent2Respond() {

            String expectedVulnerabilityText = "Applicant requires support with regards to vulnerability\ndummy"
                + "\n\nRespondent 2 requires support with regards to vulnerability\ndummy2";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(
                getHearingOrderApplnAndResp1and2(types, YES, NO, YES),
                ABOUT_TO_START
            );
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldHaveVulTextWithRespondent2Respond() {

            String expectedVulnerabilityText =
                "\n\nRespondent 2 requires support with regards to vulnerability\ndummy2";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(
                getHearingOrderApplnAndResp1and2(types, NO, NO, YES),
                ABOUT_TO_START
            );
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldHaveVulTextWithRespondent1Respond() {

            String expectedVulnerabilityText =
                "\n\nRespondent 1 requires support with regards to vulnerability\ndummy1";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(
                getHearingOrderApplnAndResp1and2(types, NO, YES, NO),
                ABOUT_TO_START
            );
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldHaveVulTextWithApplicantRespond() {

            String expectedVulnerabilityText = "Applicant requires support with regards to vulnerability\n"
                + "dummy";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, YES, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldReturnExpectedTextWithNOVulRespond() {

            String expectedVulnerabilityText = "No support required with regards to vulnerability";

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getJudicialVulnerabilityText())
                .isEqualTo(expectedVulnerabilityText);

        }

        @Test
        void shouldPrepopulateLocationIfApplicantAndRespondentHaveSameLocationPref() {

            List<LocationRefData> locations = new ArrayList<>();
            locations.add(new LocationRefData().setSiteName("siteName").setCourtAddress("court Address")
                              .setPostcode("post code").setCourtName("Court Name").setRegion("Region"));
            when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);

            when(helper.isApplicantAndRespondentLocationPrefSame(any())).thenReturn(true);

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(types, NO, NO), ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudgesHearingListGAspec responseCaseData = getJudicialHearingOrder(response);

            assertThat(responseCaseData.getHearingPreferredLocation()).isNotNull();
            assertThat(responseCaseData.getHearingPreferredLocation().getValue()).isNotNull();
            assertThat(responseCaseData.getHearingPreferredLocation().getListItems().getFirst().getLabel())
                .isEqualTo("siteName - court Address - post code");

        }

        private GAJudgesHearingListGAspec getJudicialHearingOrder(AboutToStartOrSubmitCallbackResponse response) {
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            return responseCaseData.getJudicialListForHearing();
        }

        private GeneralApplicationCaseData getCaseDateForUrgentApp() {

            hasRespondentResponseVul = NO;

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            return new GeneralApplicationCaseData()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppHearingDetails(GAHearingDetails.builder()
                                        .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                        .hearingDuration(GAHearingDuration.HOUR_1)
                                        .supportRequirement(getApplicantResponses())
                                        .build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .applicantPartyName("ApplicantPartyName")
                .generalAppRespondent1Representative(
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppHearingDetails(GAHearingDetails.builder()
                                              .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                              .hearingDuration(GAHearingDuration.HOUR_1)
                                              .supportRequirement(getApplicantResponses())
                                              .build())
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();
        }

        private GeneralApplicationCaseData getCaseDateWithNoSupportReq(List<Element<GARespondentResponse>> respondentResponse,
                                                     GAUrgencyRequirement urgentApp) {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            hasRespondentResponseVul = NO;

            return new GeneralApplicationCaseData()
                .generalAppUrgencyRequirement(urgentApp)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .respondentsResponses(respondentResponse)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .applicantPartyName("ApplicantPartyName")
                .generalAppRespondent1Representative(
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppHearingDetails(GAHearingDetails.builder()
                                              .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                              .hearingDuration(GAHearingDuration.HOUR_1)
                                              .build())
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();
        }

        private GeneralApplicationCaseData getCaseDateWithNoApplicantSupportReq(List<Element<GARespondentResponse>> respondentResponse,
                                                     GAUrgencyRequirement urgentApp) {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            hasRespondentResponseVul = NO;

            return new GeneralApplicationCaseData()
                .generalAppUrgencyRequirement(urgentApp)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .respondentsResponses(respondentResponse)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .applicantPartyName("ApplicantPartyName")
                .generalAppRespondent1Representative(
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppHearingDetails(GAHearingDetails.builder()
                                              .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                              .hearingDuration(GAHearingDuration.HOUR_1)
                                              .build())
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();
        }

        public GeneralApplicationCaseData getCaseDateWithHearingScreeen1V1(List<GeneralApplicationTypes> types, YesOrNo vulQuestion,
                                                         YesOrNo hasRespondentResponseVul) {

            return new GeneralApplicationCaseData()
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .respondentsResponses(getRespodentResponses(hasRespondentResponseVul))
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .applicantPartyName("ApplicantPartyName")
                .generalAppRespondent1Representative(
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppHearingDetails(GAHearingDetails.builder()
                                              .vulnerabilityQuestionsYesOrNo(vulQuestion)
                                              .vulnerabilityQuestion("dummy")
                                              .hearingPreferencesPreferredType(GAHearingType.VIDEO)
                                              .hearingDuration(GAHearingDuration.MINUTES_45)
                                              .supportRequirement(getApplicantResponses1V1())
                                              .hearingPreferredLocation(getLocationDynamicList())
                                              .build())
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();
        }

        public List<GAHearingSupportRequirements> getApplicantResponses1V1() {
            List<GAHearingSupportRequirements> applSupportReq = new ArrayList<>();
            applSupportReq
                .add(GAHearingSupportRequirements.DISABLED_ACCESS);
            applSupportReq
                .add(GAHearingSupportRequirements.SIGN_INTERPRETER);

            return applSupportReq;
        }

        private GeneralApplicationCaseData getCloakedApplication(YesOrNo parentClaimantIsApplicant) {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            hasRespondentResponseVul = NO;

            return new GeneralApplicationCaseData()
                .parentClaimantIsApplicant(parentClaimantIsApplicant)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .respondentsResponses(getRespodentResponses(hasRespondentResponseVul))
                .generalAppHearingDetails(GAHearingDetails.builder()
                                              .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                              .hearingDuration(GAHearingDuration.HOUR_1)
                                              .supportRequirement(getApplicantResponses())
                                              .build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder())
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .applicantPartyName("ApplicantPartyName")
                .generalAppRespondent1Representative(
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();
        }

        private GeneralApplicationCaseData getApplicationByParentCaseDefendant() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            hasRespondentResponseVul = NO;

            return new GeneralApplicationCaseData()
                .parentClaimantIsApplicant(NO)
                .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder())
                .generalAppDetailsOfOrder("Draft order text entered by applicant.")
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .applicantPartyName("ApplicantPartyName")
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .respondentsResponses(getRespodentResponses(hasRespondentResponseVul))
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setIsWithNotice(YES))
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                                                .isWithNotice(YES).build())
                .generalAppHearingDetails(GAHearingDetails.builder()
                                              .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                              .hearingDuration(GAHearingDuration.HOUR_1)
                                              .supportRequirement(getApplicantResponses())
                                              .build())
                .generalAppRespondent1Representative(
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();
        }

    }

    @Nested
    class MidEventForWrittenRepresentation {

        private static final String VALIDATE_WRITTEN_REPRESENTATION_PAGE = "ga-validate-written-representation-date";
        private static final String VALIDATE_HEARING_ORDER_SCREEN = "validate-hearing-order-screen";

        @Test
        void shouldGenerateListingForHearingDocument() {
            when(hearingOrderGenerator.generate(any(), any())).thenReturn(PDFBuilder.HEARING_ORDER_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "populate-hearing-order-doc");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(hearingOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getJudicialListHearingDocPreview())
                .isEqualTo(PDFBuilder.HEARING_ORDER_DOCUMENT.getDocumentLink());
        }

        @Test
        void shouldThrowErrorForListingForHearingCourtOwnInitiative() {
            when(hearingOrderGenerator.generate(any(), any())).thenReturn(PDFBuilder.HEARING_ORDER_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
                .build();
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeListForHearing(GAByCourtsInitiativeGAspec.OPTION_2)
                .orderWithoutNoticeListForHearing(
                    new GAOrderWithoutNoticeGAspec().setOrderWithoutNotice("abcde")
                        .setOrderWithoutNoticeDate(LocalDate.now().minusDays(4))).build();

            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            CallbackParams params = callbackParamsOf(updateData, MID, "populate-hearing-order-doc");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertEquals(1, response.getErrors().size());
            assertThat(response.getErrors().getFirst()).isEqualTo(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
        }

        @Test
        void shouldThrowErrorForListingForHearingCourtOrderWithOutNotice() {
            when(hearingOrderGenerator.generate(any(), any())).thenReturn(PDFBuilder.HEARING_ORDER_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
                .build();
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeListForHearing(GAByCourtsInitiativeGAspec.OPTION_1)
                .orderCourtOwnInitiativeListForHearing(
                    new GAOrderCourtOwnInitiativeGAspec().setOrderCourtOwnInitiative("abcde")
                        .setOrderCourtOwnInitiativeDate(LocalDate.now().minusDays(3))).build();

            GeneralApplicationCaseData updateData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(updateData, MID, "populate-hearing-order-doc");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertEquals(1, response.getErrors().size());
            assertThat(response.getErrors().getFirst()).isEqualTo(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
        }

        @Test
        void shouldGenerateConcurrentApplicationDocument() {
            when(writtenRepresentationConcurrentOrderGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationConcurrentApplication().build()
                .copy().build();
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_2)
                .orderWithoutNoticeForWrittenRep(
                    new GAOrderWithoutNoticeGAspec().setOrderWithoutNotice("abcde")
                        .setOrderWithoutNoticeDate(LocalDate.now())).build();

            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            CallbackParams params = callbackParamsOf(updateData, MID, "populate-written-rep-preview-doc");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationConcurrentOrderGenerator)
                .generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getJudicialWrittenRepDocPreview())
                .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT.getDocumentLink());
        }

        @Test
        void shouldGenerateSequentialApplicationDocument() {
            when(writtenRepresentationSequentialOrderGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationSequentialApplication().build()
                .copy().build();
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_2)
                .orderWithoutNoticeForWrittenRep(
                    new GAOrderWithoutNoticeGAspec().setOrderWithoutNotice("abcde")
                        .setOrderWithoutNoticeDate(LocalDate.now())).build();

            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            CallbackParams params = callbackParamsOf(updateData, MID, "populate-written-rep-preview-doc");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(writtenRepresentationSequentialOrderGenerator)
                .generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getJudicialWrittenRepDocPreview())
                .isEqualTo(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT.getDocumentLink());
        }

        @Test
        void shouldThrowErrorCourtOwnInitiativeForWrittenRepIsPastDate() {
            when(writtenRepresentationSequentialOrderGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationSequentialApplication().build()
                .copy().build();
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_1)
                .orderCourtOwnInitiativeForWrittenRep(
                    new GAOrderCourtOwnInitiativeGAspec().setOrderCourtOwnInitiative("abcde")
                        .setOrderCourtOwnInitiativeDate(LocalDate.now().minusDays(2))).build();

            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            CallbackParams params = callbackParamsOf(updateData, MID, "populate-written-rep-preview-doc");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals(1, response.getErrors().size());
            assertThat(response.getErrors().getFirst()).isEqualTo(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);

        }

        @Test
        void shouldThrowErrorOrderWithoutNoticeForWrittenRepIsPastDate() {
            when(writtenRepresentationSequentialOrderGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationSequentialApplication().build()
                .copy().build();
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_2)
                .orderWithoutNoticeForWrittenRep(
                    new GAOrderWithoutNoticeGAspec().setOrderWithoutNotice("abcde")
                        .setOrderWithoutNoticeDate(LocalDate.now().minusDays(2))).build();

            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            CallbackParams params = callbackParamsOf(updateData, MID, "populate-written-rep-preview-doc");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals(1, response.getErrors().size());
            assertThat(response.getErrors().getFirst()).isEqualTo(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);

        }

        @Test
        void shouldReturnErrors_whenSequentialWrittenRepresentationDateIsInPast() {
            CallbackParams params = callbackParamsOf(
                    getSequentialWrittenRepresentationDecision(LocalDate.now().minusDays(1)),
                    MID,
                    VALIDATE_WRITTEN_REPRESENTATION_PAGE
            );
            when(service.validateWrittenRepresentationsDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
        }

        @Test
        void shouldReturnErrors_whenConcurrentWrittenRepresentationDateIsInPast() {
            CallbackParams params = callbackParamsOf(
                    getConcurrentWrittenRepresentationDecision(LocalDate.now().minusDays(1)),
                    MID,
                    VALIDATE_WRITTEN_REPRESENTATION_PAGE
            );
            when(service.validateWrittenRepresentationsDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(WRITTEN_REPRESENTATION_DATE_CANNOT_BE_IN_PAST);
        }

        @Test
        void shouldNotReturnErrors_whenSequentialWrittenRepresentationDateIsInFuture() {
            when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(), anyInt())).thenReturn(localDatePlus7days);

            String expectedSequentialText = "The defendant should upload any written responses or evidence by 4pm on %s";
            String expectedApplicantSequentialText =
                "The claimant should upload any written responses or evidence in reply by 4pm on %s";

            CallbackParams params = callbackParamsOf(
                getSequentialWrittenRepresentationDecision(LocalDate.now()),
                MID,
                VALIDATE_WRITTEN_REPRESENTATION_PAGE
            );
            when(service.validateWrittenRepresentationsDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(response.getErrors()).isEmpty();
            assertThat(responseCaseData.getJudicialSequentialDateText())
                .isEqualTo(String.format(expectedSequentialText, formatLocalDate(
                    responseCaseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                        .getWrittenSequentailRepresentationsBy(), DATE)));
            assertThat(responseCaseData.getOrderCourtOwnInitiativeForWrittenRep().getOrderCourtOwnInitiativeDate())
                .isEqualTo(localDatePlus7days);
            assertThat(responseCaseData.getOrderWithoutNoticeForWrittenRep().getOrderWithoutNoticeDate())
                .isEqualTo(localDatePlus7days);
            assertThat(responseCaseData.getJudicialApplicanSequentialDateText())
                .isEqualTo(String.format(expectedApplicantSequentialText, formatLocalDate(
                    responseCaseData
                        .getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                        .getSequentialApplicantMustRespondWithin(), DATE)));
        }

        @Test
        void shouldNotReturnErrors_whenConcurrentWrittenRepresentationDateIsInFuture() {

            String expectedConcurrentText =
                "The claimant and defendant should upload any written submissions and evidence by 4pm on %s";

            CallbackParams params = callbackParamsOf(
                getConcurrentWrittenRepresentationDecision(LocalDate.now()),
                MID,
                VALIDATE_WRITTEN_REPRESENTATION_PAGE
            );
            when(service.validateWrittenRepresentationsDates(any())).thenCallRealMethod();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(response.getErrors()).isEmpty();
            assertThat(responseCaseData.getJudicialConcurrentDateText())
                .isEqualTo(String.format(expectedConcurrentText, formatLocalDate(
                    responseCaseData
                        .getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                        .getWrittenConcurrentRepresentationsBy(), DATE)));
        }

        @Test
        void shouldPopulateJudicialGOHearingAndTimeEst() {

            String expectedJudicialHearingTypeText = "The hearing will be %s.";
            String expeceedJudicialTimeEstimateText = "Estimated length of hearing is %s";
            when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(), anyInt())).thenReturn(localDatePlus7days);
            when(timeEstimateHelper.getEstimatedHearingLength(any())).thenReturn("2 hours");

            List<SupportRequirements> judgeSupportReqChoices = new ArrayList<>();

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(judgeSupportReqChoices),
                                                     MID, VALIDATE_HEARING_ORDER_SCREEN);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getJudicialHearingGeneralOrderHearingText())
                .isEqualTo(String.format(expectedJudicialHearingTypeText, responseCaseData
                                   .getJudicialListForHearing().getHearingPreferencesPreferredType()
                    .getDisplayedValue()));

            assertThat(responseCaseData.getJudicialGeneralOrderHearingEstimationTimeText())
                .isEqualTo(String.format(expeceedJudicialTimeEstimateText, responseCaseData
                    .getJudicialListForHearing().getJudicialTimeEstimate().getDisplayedValue()));

            assertThat(responseCaseData.getOrderCourtOwnInitiativeListForHearing().getOrderCourtOwnInitiativeDate())
                .isEqualTo(localDatePlus7days);
            assertThat(responseCaseData.getOrderWithoutNoticeListForHearing().getOrderWithoutNoticeDate())
                .isEqualTo(localDatePlus7days);
        }

        @Test
        void shouldMatchJudgeGOSupportReqWithExpectedTextWithSeperator() {

            List<SupportRequirements> judgeSupportReqChoices = new ArrayList<>();
            judgeSupportReqChoices
                .add(SupportRequirements.HEARING_LOOPS);
            judgeSupportReqChoices
                .add(SupportRequirements.OTHER_SUPPORT);

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(judgeSupportReqChoices),
                                                     MID, VALIDATE_HEARING_ORDER_SCREEN);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            String expecetedJudicialSupportReqText = "Hearing requirements Hearing loop, Other support";

            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(response).isNotNull();

            assertThat(responseCaseData.getJudicialHearingGOHearingReqText())
                .isEqualTo(expecetedJudicialSupportReqText);

        }

        @Test
        void shouldMatchJudgeGOSupportReqWithExpectedText() {

            List<SupportRequirements> judgeSupportReqChoices = new ArrayList<>();
            judgeSupportReqChoices
                .add(SupportRequirements.HEARING_LOOPS);

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(judgeSupportReqChoices),
                                                     MID, VALIDATE_HEARING_ORDER_SCREEN);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            String expecetedJudicialSupportReqText = "Hearing requirements Hearing loop";

            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(response).isNotNull();

            assertThat(responseCaseData.getJudicialHearingGOHearingReqText())
                .isEqualTo(expecetedJudicialSupportReqText);

        }

        @Test
        void shouldReturnErrorWhenInPersonAndLocationIsNull() {

            CallbackParams params = callbackParamsOf(getJudicialListHearingData(),
                                                     MID, VALIDATE_HEARING_ORDER_SCREEN);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            String expectedErrorText = "Select your preferred hearing location.";

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).contains(expectedErrorText);
        }

        @Test
        void shouldReturnNullForJudgeGOSupportRequirement() {

            when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(), anyInt())).thenReturn(localDatePlus7days);

            List<SupportRequirements> judgeSupportReqChoices = null;

            CallbackParams params = callbackParamsOf(getHearingOrderApplnAndResp(judgeSupportReqChoices),
                                                     MID, VALIDATE_HEARING_ORDER_SCREEN);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(response).isNotNull();

            assertThat(responseCaseData.getJudicialHearingGOHearingReqText()).isEmpty();

        }

        private GeneralApplicationCaseData getJudicialListHearingData() {

            return new GeneralApplicationCaseData()
                .judicialListForHearing(new GAJudgesHearingListGAspec()
                                            .setHearingPreferencesPreferredType(GAJudicialHearingType.IN_PERSON)
                                            .setJudicialTimeEstimate(GAHearingDuration.HOURS_2))
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();
        }

        private GeneralApplicationCaseData getHearingOrderApplnAndResp(List<SupportRequirements> judgeSupportReqChoices) {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            return new GeneralApplicationCaseData()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .judicialListForHearing(new GAJudgesHearingListGAspec()
                                            .setHearingPreferredLocation(getLocationDynamicList())
                                            .setJudicialSupportRequirement(judgeSupportReqChoices)
                                            .setHearingPreferencesPreferredType(GAJudicialHearingType.VIDEO)
                                            .setJudicialTimeEstimate(GAHearingDuration.HOURS_2))
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppHearingDetails(GAHearingDetails.builder()
                                        .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                        .hearingPreferredLocation(getLocationDynamicList())
                                        .hearingDuration(GAHearingDuration.HOUR_1)
                                        .supportRequirement(getApplicantResponses())
                                        .build())
                .respondentsResponses(getRespodentResponses(NO))
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .applicantPartyName("ApplicantPartyName")
                .generalAppHearingDetails(GAHearingDetails.builder()
                                              .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                              .hearingPreferredLocation(getLocationDynamicList())
                                              .hearingDuration(GAHearingDuration.HOUR_1)
                                              .supportRequirement(getApplicantResponses())
                                              .build())
                .generalAppRespondent1Representative(
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();
        }

        public GeneralApplicationCaseData getSequentialWrittenRepresentationDecision(LocalDate writtenRepresentationDate) {

            GAJudicialWrittenRepresentations gaJudicialWrittenRepresentations = new GAJudicialWrittenRepresentations()
                .setWrittenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
                .setWrittenSequentailRepresentationsBy(writtenRepresentationDate)
                .setSequentialApplicantMustRespondWithin(writtenRepresentationDate)
                .setWrittenConcurrentRepresentationsBy(null);
            return new GeneralApplicationCaseData()
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(gaJudicialWrittenRepresentations).build();
        }

        public GeneralApplicationCaseData getConcurrentWrittenRepresentationDecision(LocalDate writtenRepresentationDate) {
            GAJudicialWrittenRepresentations gaJudicialWrittenRepresentations = new GAJudicialWrittenRepresentations()
                .setWrittenOption(GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS)
                .setWrittenConcurrentRepresentationsBy(writtenRepresentationDate)
                .setWrittenSequentailRepresentationsBy(null);
            return new GeneralApplicationCaseData()
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(gaJudicialWrittenRepresentations).build();
        }

    }

    @Nested
    class MidEventForMakeAnOrderOption {

        private static final String VALIDATE_MAKE_AN_ORDER = "validate-make-an-order";

        @Test
        void shouldPopulateFreeFormOrderValues_onMidEventCallback() {
            when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(), anyInt())).thenReturn(localDatePlus7days);

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STAY_THE_CLAIM));
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.applicationIsUncloakedOnce(NO);

            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(FREE_FORM_ORDER))
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader");

            caseDataBuilder.claimant1PartyName("Mr. John Rambo").defendant1PartyName("Mr. Sole Trader");

            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();

            assertThat(response.getData()).extracting("orderOnCourtInitiative").extracting("onInitiativeSelectionTextArea")
                .isEqualTo(ON_INITIATIVE_SELECTION_TEST);
            assertThat(response.getData()).extracting("orderOnCourtInitiative").extracting("onInitiativeSelectionDate")
                .isEqualTo(localDatePlus7days.toString());
            assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionTextArea")
                .isEqualTo(WITHOUT_NOTICE_SELECTION_TEXT);
            assertThat(response.getData()).extracting("orderWithoutNotice").extracting("withoutNoticeSelectionDate")
                .isEqualTo(localDatePlus7days.toString());
            assertThat(response.getData().get("caseNameHmctsInternal")
                           .toString()).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
            assertThat(response.getData())
                .extracting("judicialDecisionRequestMoreInfo").extracting("judgeRequestMoreInfoByDate")
                .isEqualTo(localDatePlus7days.toString());
        }

        @Test
        void shouldGenerateFinalOrderPreviewDocumentWhenPopulateFinalOrderPreviewDocIsCalled() {
            when(gaFreeFormOrderGenerator.generate(any(), any())).thenReturn(new CaseDocument().setDocumentLink(new Document()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "populate-final-order-preview-doc");

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(updatedData.getGaFinalOrderDocPreview()).isNotNull();
        }

        @Test
        void shouldReturnErrorForWrittenRepresentationWithOutNoticeApplnForJudgeRevisit() {
            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.applicationIsUncloakedOnce(NO)
                .judicialDecision(new GAJudicialDecision()
                                      .setDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().getFirst())
                .isEqualTo("The application needs to be uncloaked before requesting written representations");
        }

        @Test
        void shouldReturnErrorForWrittenRepresentationWithOutNoticeApplnForJudgeRevisitLipCase() {
            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.applicationIsUncloakedOnce(NO)
                .judicialDecision(new GAJudicialDecision()
                                      .setDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().getFirst())
                .isEqualTo("The application needs to be uncloaked before requesting written representations");
        }

        @Test
        void shouldNotReturnErrorForWrittenRepresentationWithOutNoticeApplnForJudgeRevisitLipCase() {
            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.applicationIsUncloakedOnce(YES)
                .judicialDecision(new GAJudicialDecision()
                                      .setDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoErrorForWrittenRepresentationWhenJudgeRevisit_AfterUncloak() {
            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.applicationIsUncloakedOnce(YES)
                .judicialDecision(new GAJudicialDecision()
                                      .setDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getErrors().size()).isEqualTo(0);
        }

        @Test
        void shouldReturnNoErrorForHearingWhenJudgeRevisit_AfterUncloak() {
            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));

            when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(), anyInt())).thenReturn(localDatePlus7days);
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.applicationIsUncloakedOnce(NO)
                .judicialDecision(new GAJudicialDecision()
                                      .setDecision(LIST_FOR_A_HEARING));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getErrors().size()).isEqualTo(0);
        }

        @Test
        void shouldReturnNoErrorForMakeOrderWithNoticeAppln() {
            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision()
                                                 .setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getErrors().size()).isEqualTo(0);
        }

        @Test
        void shouldReturnErrorForWrittenRepWithOutNoticeAppln() {
            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision()
                                                 .setDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().getFirst())
                .isEqualTo("The application needs to be uncloaked before requesting written representations");
        }

        @Test
        void shouldReturnErrorForWrittenRepWithOutNoticeApplnForLipCase() {
            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision()
                                                 .setDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors().getFirst())
                .isEqualTo("The application needs to be uncloaked before requesting written representations");
        }

        @Test
        void shouldReturnNOForJudgeApproveEditOptionDate() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STRIKE_OUT));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDate())
                .isEqualTo(NO);
        }

        @Test
        void shouldReturnYesForJudgeApproveEditOptionDate() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STAY_THE_CLAIM),
                                                          (GeneralApplicationTypes.EXTEND_TIME));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDate())
                .isEqualTo(YES);
        }

        @Test
        void shouldReturnNoForJudgeApproveEditOptionDate() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.EXTEND_TIME));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDate())
                .isEqualTo(NO);
        }

        @Test
        void shouldReturnYesForJudgeApproveEditOptionDate_UnlessOrder() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.UNLESS_ORDER),
                                                          (GeneralApplicationTypes.EXTEND_TIME));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDateForUnlessOrder())
                .isEqualTo(YES);
        }

        @Test
        void shouldReturnNoForJudgeApproveEditOptionDate_IfNoUnlessOrder() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.EXTEND_TIME));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDateForUnlessOrder())
                .isEqualTo(NO);
        }

        @Test
        void shouldReturnNoForJudgeApproveEditOptionDate_IfNoUnlessOrderWithOtherType() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.EXTEND_TIME),
                                                          (GeneralApplicationTypes.STAY_THE_CLAIM));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDateForUnlessOrder())
                .isEqualTo(NO);
        }

        @Test
        void shouldReturnYesForJudgeApproveEditOptionDate_UnlessOrder_StayClaim() {

            List<GeneralApplicationTypes> types = List.of((GeneralApplicationTypes.STAY_THE_CLAIM),
                                                          (GeneralApplicationTypes.UNLESS_ORDER));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDate())
                .isEqualTo(YES);
            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDateForUnlessOrder())
                .isEqualTo(YES);
        }

        @Test
        void shouldReturnCorrectDirectionsText() {

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);

            GeneralApplicationCaseData caseData = getDirectionsText(NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDirectionsText()).isEqualTo("Test directionText");
        }

        @Test
        void shouldAddCorrectDirectionsText() {
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            GeneralApplicationCaseData caseData = getMakeAnOrder(NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDirectionsText()).isEqualTo(null);
        }

        @Test
        void shouldReturnNOForJudgeApproveEditOptionParty() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDoc())
                .isEqualTo(NO);
        }

        @Test
        void shouldReturnNOForjudgeApproveEditOptionDoc() {

            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.EXTEND_TIME), (GeneralApplicationTypes.SUMMARY_JUDGEMENT));

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);

            GeneralApplicationCaseData caseData = getHearingOrderApplnAndResp(types, NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getDisplayjudgeApproveEditOptionDoc())
                .isEqualTo(YES);
        }

        @Test
        void testAboutToStartForWithOutNotifiedApplicationInitiatedByClaimant() {

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(YES);
            // isWithNotice = No
            GeneralApplicationCaseData caseData = getNotifiedApplication(NO, YES);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            String expectedRecitalText = "The Judge considered the without notice application of the claimant dated 15 January 2022\n\n"
                + "And the Judge considered the information provided by the claimant";

            assertThat(makeAnOrder.getJudgeRecitalText())
                .isEqualTo(String.format(expectedRecitalText, DATE_FORMATTER.format(LocalDate.now())));
            assertThat(makeAnOrder.getDismissalOrderText()).isEqualTo(EXPECTED_DISMISSAL_ORDER);
        }

        @Test
        void testAboutToStartForNotifiedApplicationInitiatedByDefendant() {
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            //isWithNotice = Yes

            GeneralApplicationCaseData caseData = getNotifiedApplication(YES, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            String expectedRecitalText = "The Judge considered the application of the defendant dated 15 January 2022\n\n";

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getJudgeRecitalText())
                .isEqualTo(String.format(expectedRecitalText, DATE_FORMATTER.format(LocalDate.now())));
            assertThat(makeAnOrder.getDismissalOrderText()).isEqualTo(EXPECTED_DISMISSAL_ORDER);
        }

        @Test
        void testAboutToStartForNonNotifiedApplicationByDefendant() {
            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            when(helper.isLipApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            //isWithNotice = No
            GeneralApplicationCaseData caseData = getNotifiedApplication(NO, NO);
            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER));
            CallbackParams params = callbackParamsOf(caseDataBuilder.build(), MID, VALIDATE_MAKE_AN_ORDER);

            String expectedRecitalText = "The Judge considered the application of the defendant dated 15 January 2022\n\n";

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response).isNotNull();
            GAJudicialMakeAnOrder makeAnOrder = getJudicialMakeAnOrder(response);

            assertThat(makeAnOrder.getJudgeRecitalText())
                .isEqualTo(String.format(expectedRecitalText, DATE_FORMATTER.format(LocalDate.now())));
            assertThat(makeAnOrder.getDismissalOrderText()).isEqualTo(EXPECTED_DISMISSAL_ORDER);
        }
    }

    @Nested
    class MidEventForRespondToDirectionsDateValidity {

        private static final String VALIDATE_MAKE_DECISION_SCREEN = "validate-make-decision-screen";
        public static final String RESPOND_TO_DIRECTIONS_DATE_IN_PAST = "The date, by which the response to direction"
                + " should be given, cannot be in past.";

        @Test
        void shouldNotCauseAnyErrors_whenApplicationDetailsNotProvided() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnErrors_whenApplicationIsUrgentButConsiderationDateIsNotProvided() {
            GeneralApplicationCaseData caseData = getApplication_MakeDecision_GiveDirections(GIVE_DIRECTIONS_WITHOUT_HEARING,
                                                                           null, OPTION_3,
                                                                           null, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();

        }

        @Test
        void shouldReturnErrors_whenUrgencyConsiderationDateIsInPastForUrgentApplication() {
            GeneralApplicationCaseData caseData = getApplication_MakeDecision_GiveDirections(GIVE_DIRECTIONS_WITHOUT_HEARING,
                    LocalDate.now().minusDays(1), OPTION_3, null, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(RESPOND_TO_DIRECTIONS_DATE_IN_PAST);
        }

        @Test
        void shouldNotCauseAnyErrors_whenUrgencyConsiderationDateIsInFutureForUrgentApplication() {
            GeneralApplicationCaseData caseData = getApplication_MakeDecision_GiveDirections(GIVE_DIRECTIONS_WITHOUT_HEARING,
                    LocalDate.now().plusDays(1), OPTION_3, null, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
            GeneralApplicationCaseData caseData = getApplication_MakeDecision_GiveDirections(APPROVE_OR_EDIT,
                    LocalDate.now().minusDays(1), OPTION_3, null, null);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnGenerateOrderDocument() {
            when(generalOrderGenerator.generate(any(), any())).thenReturn(PDFBuilder.GENERAL_ORDER_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();

            verify(generalOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getJudicialMakeOrderDocPreview())
                .isEqualTo(PDFBuilder.GENERAL_ORDER_DOCUMENT.getDocumentLink());
        }

        @Test
        void shouldGenerateDirectionOrderDocument() {
            when(directionOrderGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.DIRECTION_ORDER_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().directionOrderApplication()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(directionOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getJudicialMakeOrderDocPreview())
                .isEqualTo(PDFBuilder.DIRECTION_ORDER_DOCUMENT.getDocumentLink());
        }

        @Test
        void shouldGenerateDismissalOrderDocument() {
            when(dismissalOrderGenerator.generate(any(), any())).thenReturn(PDFBuilder.DISMISSAL_ORDER_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().dismissalOrderApplication()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dismissalOrderGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getJudicialMakeOrderDocPreview())
                .isEqualTo(PDFBuilder.DISMISSAL_ORDER_DOCUMENT.getDocumentLink());
        }

        @Test
        void shouldThrowErrors_whenCourtInitiativeDateIsPast() {
            GeneralApplicationCaseData caseData = getApplication_MakeDecision_GiveDirections(APPROVE_OR_EDIT,
                                                                           LocalDate.now().minusDays(1),
                                                                           OPTION_1,
                                                                           LocalDate.now().minusDays(1),
                                                                           LocalDate.now());

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals(1, response.getErrors().size());
            assertThat(response.getErrors().getFirst()).isEqualTo(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
        }

        @Test
        void shouldThrowErrors_whenOrderWithoutNoticeDateIsPast() {
            GeneralApplicationCaseData caseData = getApplication_MakeDecision_GiveDirections(APPROVE_OR_EDIT,
                                                                           LocalDate.now().minusDays(1),
                                                                           OPTION_2,
                                                                           LocalDate.now(),
                                                                           LocalDate.now().minusDays(2));

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_MAKE_DECISION_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals(1, response.getErrors().size());
            assertThat(response.getErrors().getFirst()).isEqualTo(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
        }

        private GeneralApplicationCaseData getApplication_MakeDecision_GiveDirections(GAJudgeMakeAnOrderOption orderOption,
                                                                    LocalDate directionsResponseByDate,
                                                                    GAByCourtsInitiativeGAspec
                                                                        gaByCourtsInitiativeGAspec,
                                                                    LocalDate orderCourtOwnInitiativeDate,
                                                                    LocalDate orderWithoutNoticeDate) {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            return new GeneralApplicationCaseData()
                .parentClaimantIsApplicant(YES)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder())
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .applicantPartyName("ApplicantPartyName")
                .generalAppRespondent1Representative(
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                               .setMakeAnOrder(orderOption)
                                               .setJudicialByCourtsInitiative(gaByCourtsInitiativeGAspec)
                                               .setOrderCourtOwnInitiativeDate(orderCourtOwnInitiativeDate)
                                               .setOrderWithoutNoticeDate(orderWithoutNoticeDate)
                                               .setJudgeApproveEditOptionDate(LocalDate.now().plusDays(1))
                                               .setDirectionsResponseByDate(directionsResponseByDate))
                .build();
        }
    }

    @Nested
    class MidEventForRequestMoreInfoScreenDateValidity {

        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = LocalDateTime.now().plusDays(5);

        private static final String VALIDATE_REQUEST_MORE_INFO_SCREEN = "validate-request-more-info-screen";
        public static final String REQUESTED_MORE_INFO_BY_DATE_REQUIRED = "The date, by which the applicant must "
                + "respond, is required.";
        public static final String REQUESTED_MORE_INFO_BY_DATE_IN_PAST = "The date, by which the applicant must "
                + "respond, cannot be in past.";

        @Test
        void shouldGenerateRequestMoreInfoDocument_JudgeRevisit_Without_Uncloaked() {
            when(requestForInformationGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .applicationIsCloaked(YES)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_REQUEST_MORE_INFO_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

            verify(requestForInformationGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getJudicialRequestMoreInfoDocPreview())
                .isEqualTo(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT.getDocumentLink());
        }

        @Test
        void should_GenerateRequestMoreInfoDocument_JudgeRevisit_Without_Uncloaked() {
            when(requestForInformationGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .applicationIsUncloakedOnce(NO)
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                     .setJudgeRecitalText(JUDICIAL_REQUEST_MORE_INFO_RECITAL_TEXT)
                                                     .setRequestMoreInfoOption(REQUEST_MORE_INFORMATION)
                                                     .setJudgeRequestMoreInfoByDate(LocalDate.now())
                                                     .setIsWithNotice(NO)
                                                     .setJudgeRequestMoreInfoText("test"))
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_REQUEST_MORE_INFO_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

            verify(requestForInformationGenerator).generate(any(GeneralApplicationCaseData.class), eq("BEARER_TOKEN"));

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getShowRequestInfoPreviewDoc()).isEqualTo(YES);
            assertThat(updatedData.getJudicialRequestMoreInfoDocPreview())
                .isEqualTo(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT.getDocumentLink());
        }

        @Test
        void shouldNotGenerateRequestMoreInfoDocumentForSend_App_OtherParty() {
            String judgeRecitalText = "<Title><Name>\n"
                + "Upon reviewing the application made and upon considering the information"
                + "provided by the parties, the court requests more information from the applicant.";

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                     .setJudgeRecitalText(judgeRecitalText)
                                                     .setRequestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                     .setJudgeRequestMoreInfoByDate(LocalDate.now())
                                                     .setJudgeRequestMoreInfoText("test"))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_REQUEST_MORE_INFO_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            verifyNoInteractions(requestForInformationGenerator);

            assertThat(updatedData.getJudicialRequestMoreInfoDocPreview())
                .isEqualTo(null);
        }

        @Test
        void shouldNotGenerateRequestMoreInfoDocumentForSend_App_OtherParty_JudgeRevisit_Cloaked() {
            String judgeRecitalText = "<Title><Name>\n"
                + "Upon reviewing the application made and upon considering the information "
                + "provided by the parties,the court requests more information from the applicant.";

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
                .applicationIsCloaked(YES)
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                     .setJudgeRecitalText(judgeRecitalText)
                                                     .setRequestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                                                     .setJudgeRequestMoreInfoByDate(LocalDate.now())
                                                     .setJudgeRequestMoreInfoText("test"))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_REQUEST_MORE_INFO_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            verifyNoInteractions(requestForInformationGenerator);

            assertThat(updatedData.getJudicialRequestMoreInfoDocPreview())
                .isEqualTo(null);
        }

        @Test
        void shouldGenerateRequestMoreInfoDocumentWithNotice() {
            when(requestForInformationGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);

            String judgeRecitalText = "<Title><Name>\n"
                + "Upon reviewing the application made and upon considering the information "
                + "provided by the parties,the court requests more information from the applicant.";

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                     .setJudgeRecitalText(judgeRecitalText)
                                                     .setIsWithNotice(YES)
                                                     .setJudgeRequestMoreInfoByDate(LocalDate.now())
                                                     .setJudgeRequestMoreInfoText("test"))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_REQUEST_MORE_INFO_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

            GeneralApplicationCaseData updatedData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(updatedData.getJudicialRequestMoreInfoDocPreview()).isNotNull();
            assertThat(updatedData.getJudicialRequestMoreInfoDocPreview())
                .isEqualTo(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT.getDocumentLink());

        }

        @Test
        void shouldNotReturnErrors_whenRequestedMoreInfoAndTheDateIsInFuture() {
            when(requestForInformationGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);

            GeneralApplicationCaseData caseData = getApplication_RequestMoreInformation(REQUEST_MORE_INFORMATION,
                                                                      LocalDate.now().plusDays(1), YES);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_REQUEST_MORE_INFO_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnErrors_whenRequestedMoreInfoAndTheDateIsNull() {
            when(requestForInformationGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);

            GeneralApplicationCaseData caseData = getApplication_RequestMoreInformation2(REQUEST_MORE_INFORMATION, null, NO);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_REQUEST_MORE_INFO_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(REQUESTED_MORE_INFO_BY_DATE_REQUIRED);
        }

        @Test
        void shouldReturnErrors_whenRequestedMoreInfoAndTheDateIsInPast() {
            when(requestForInformationGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);

            GeneralApplicationCaseData caseData = getApplication_RequestMoreInformation2(REQUEST_MORE_INFORMATION,
                    LocalDate.now().minusDays(1), NO);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_REQUEST_MORE_INFO_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains(REQUESTED_MORE_INFO_BY_DATE_IN_PAST);
        }

        @Test
        void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
            when(requestForInformationGenerator.generate(any(), any()))
                .thenReturn(PDFBuilder.REQUEST_FOR_INFORMATION_DOCUMENT);

            GeneralApplicationCaseData caseData = getApplication_RequestMoreInformation2(null,
                    LocalDate.now(), YES);

            CallbackParams params = callbackParamsOf(caseData, MID, VALIDATE_REQUEST_MORE_INFO_SCREEN);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        private GeneralApplicationCaseData getApplication_RequestMoreInformation2(GAJudgeRequestMoreInfoOption option,
                                                                LocalDate judgeRequestMoreInfoByDate,
                                                                YesOrNo hasAgree) {

            return new GeneralApplicationCaseData()
                .judicialDecision(new GAJudicialDecision().setDecision(REQUEST_MORE_INFO))
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(hasAgree).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                     .setRequestMoreInfoOption(option)
                                                     .setJudgeRequestMoreInfoByDate(judgeRequestMoreInfoByDate)
                                                     .setJudgeRequestMoreInfoText("Test")
                                                     .setIsWithNotice(YES)
                                                     .setDeadlineForMoreInfoSubmission(LocalDateTime.now().plusDays(5))
                                                     )
                .build();
        }

        private GeneralApplicationCaseData getApplication_RequestMoreInformation(GAJudgeRequestMoreInfoOption option,
                                                               LocalDate judgeRequestMoreInfoByDate, YesOrNo hasAgree) {
            List<GeneralApplicationTypes> types = List.of(
                (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            return new GeneralApplicationCaseData()
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .judicialDecision(new GAJudicialDecision().setDecision(REQUEST_MORE_INFO))
                .parentClaimantIsApplicant(YES)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(hasAgree).build())
                .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder())
                .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                .applicantPartyName("ApplicantPartyName")
                .generalAppRespondent1Representative(
                    new GARespondentRepresentative()
                        .setGeneralAppRespondent1Representative(YES)
                        )
                .generalAppType(
                    GAApplicationType
                        .builder()
                        .types(types).build())
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.STARTED)
                                     .setActivityId(ACTIVITY_ID))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                               .setMakeAnOrder(APPROVE_OR_EDIT))
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                     .setRequestMoreInfoOption(option)
                                                     .setJudgeRequestMoreInfoByDate(judgeRequestMoreInfoByDate)
                                                     .setJudgeRequestMoreInfoText("Test")
                                                     .setDeadlineForMoreInfoSubmission(LocalDateTime.now().plusDays(5))
                                                     )
                .build();
        }
    }

    @Nested
    class GetAllPartyNames {
        @Test
        void oneVOne() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .build().copy()
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            String title = JudicialDecisionHandler.getAllPartyNames(caseData);
            assertThat(title).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
        }

        @Test
        void oneVTwoSameSol() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .defendant2PartyName("Mr. John Rambo")
                .build();

            String title = JudicialDecisionHandler.getAllPartyNames(caseData);
            assertThat(title).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
        }

        @Test
        void oneVTwo() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .defendant2PartyName("Mr. John Rambo")
                .build();

            String title = JudicialDecisionHandler.getAllPartyNames(caseData);
            assertThat(title).isEqualTo("Mr. John Rambo v Mr. Sole Trader, Mr. John Rambo");
        }
    }

    @Nested
    class AboutToSubmitHandling {

        @Test
        void shouldSetUpReadyBusinessProcess() {
            GeneralApplicationCaseData caseData = getApplicationBusinessProcess().copy().isMultiParty(NO)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                              .email("test@gmail.com").organisationIdentifier("org1").build())
                .isGaRespondentOneLip(NO)
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getMakeAppVisibleToRespondents().getMakeAppAvailableCheck()).isNotNull();
            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo("MAKE_DECISION");
        }

        @Test
        void shouldSetUpReadyWhenPreferredTypeNotInPerson() {
            GeneralApplicationCaseData caseData = getApplicationWithPreferredTypeNotInPerson().copy()
                .isMultiParty(NO)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                              .email("test@gmail.com").organisationIdentifier("org1").build())
                .isGaRespondentOneLip(NO)
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO).build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getJudicialListForHearing().getHearingPreferredLocation()).isNull();
            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo("MAKE_DECISION");
        }

        @Test
        void shouldClearJudicialHearingLabelText() {

            GeneralApplicationCaseData caseData = getApplicationBusinessProcess()
                .copy().isMultiParty(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                              .email("test@gmail.com").organisationIdentifier("org1").build())
                .judicialListForHearing(new GAJudgesHearingListGAspec()
                                                        .setJudgeHearingCourtLocationText1("test")
                                                        .setJudgeHearingTimeEstimateText1("test")
                                                        .setHearingPreferencesPreferredTypeLabel1("test")
                                                        .setJudgeHearingSupportReqText1("test"))
                .isGaRespondentOneLip(NO)
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO).build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertNull(responseCaseData.getJudicialListForHearing().getJudgeHearingCourtLocationText1());
            assertNull(responseCaseData.getJudicialListForHearing().getJudgeHearingTimeEstimateText1());
            assertNull(responseCaseData.getJudicialListForHearing().getHearingPreferencesPreferredTypeLabel1());
            assertNull(responseCaseData.getJudicialListForHearing().getJudgeHearingSupportReqText1());
        }

        @Test
        void shouldUncloakApplication_WhenJudgeUncloaked_RequestMoreInformationApplication() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY, NO, YES)
                .generalAppRespondentSolicitors(getRespondentSolicitors())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                              .email("test@gmail.com").organisationIdentifier("org1").build())
                .isGaRespondentOneLip(NO)
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(responseCaseData.getApplicationIsCloaked()).isEqualTo(NO);
        }

        @Test
        void shouldCallAssignCase_3Times() {
            String caseId = GeneralApplicationCaseDataBuilder.CASE_ID.toString();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY, NO, YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                              .email("test@gmail.com").organisationIdentifier("org1").build())
                .generalAppRespondentSolicitors(getRespondentSolicitors())
                .isGaRespondentOneLip(NO)
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(responseCaseData.getApplicationIsCloaked()).isEqualTo(NO);
            verify(assignCaseToResopondentSolHelper).assignCaseToRespondentSolicitor(caseData, caseId);
        }

        @Test
        void shouldThrowExceptionIfSolicitorsAreNull() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY, NO, YES)
                .isMultiParty(NO).build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            try {
                handler.handle(params);
            } catch (Exception e) {
                assertThat(e.toString()).contains("java.lang.NullPointerException");
            }
        }

        @Test
        void shouldApplicationRemainSame_WhenJudgeNotUncloaked_RequestMoreInformationApplication() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(REQUEST_MORE_INFORMATION, NO, YES)
                .isMultiParty(NO)
                .generalAppRespondentSolicitors(getRespondentSolicitors()).build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(responseCaseData.getApplicationIsCloaked()).isEqualTo(YES);
        }

        @Test
        void shouldAssignToRespondent_WhenJudgeNotUncloaked_RequestMoreInformationApplicationLipRespondent() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(REQUEST_MORE_INFORMATION, NO, YES)
                .isMultiParty(NO)
                .generalAppRespondentSolicitors(getRespondentSolicitors())
                .isGaRespondentOneLip(YES).build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(responseCaseData.getApplicationIsCloaked()).isEqualTo(YES);
        }

        @Test
        void shouldBeUncloaked_WhenRequestMoreInformation_WithNoticeApplication() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(REQUEST_MORE_INFORMATION, YES, null)
                .isMultiParty(NO)
                .generalAppRespondentSolicitors(getRespondentSolicitors())
                .build();

            when(helper.isApplicationCreatedWithoutNoticeByApplicant(any())).thenReturn(NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(responseCaseData.getApplicationIsCloaked()).isEqualTo(NO);
        }

        @Test
        void shouldUncloakApplication_WhenJudgeUncloaked_OrderMadeApplication() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialOrderMadeWithUncloakApplication(YES)
                .generalAppRespondentSolicitors(getRespondentSolicitors())
                .isMultiParty(NO)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                              .email("test@gmail.com").organisationIdentifier("org1").build())
                .makeAppVisibleToRespondents(new GAMakeApplicationAvailableCheck()
                                                 .setMakeAppAvailableCheck(getMakeAppVisible()))
                .isGaRespondentOneLip(NO)
                .isGaApplicantLip(NO)
                .isGaRespondentTwoLip(NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
            assertThat(responseCaseData.getApplicationIsCloaked()).isEqualTo(NO);
        }

        public GeneralApplicationCaseData getApplicationBusinessProcess() {

            return new GeneralApplicationCaseData()
                .generalAppRespondentSolicitors(getRespondentSolicitors())
                .ccdCaseReference(123345689L)
                .judicialDecision(new GAJudicialDecision()
                                      .setDecision(LIST_FOR_A_HEARING))
                .makeAppVisibleToRespondents(new GAMakeApplicationAvailableCheck()
                                                 .setMakeAppAvailableCheck(getMakeAppVisible()))
                .judicialListForHearing(new GAJudgesHearingListGAspec()
                                            .setHearingPreferencesPreferredType(GAJudicialHearingType.IN_PERSON)
                                            .setHearingPreferredLocation(getLocationDynamicList()))
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.FINISHED)
                                     .setActivityId(ACTIVITY_ID))
                .build();
        }

        private GeneralApplicationCaseData getApplicationWithPreferredTypeNotInPerson() {

            return new GeneralApplicationCaseData()
                .generalAppRespondentSolicitors(getRespondentSolicitors())
                .ccdCaseReference(123345689L)
                .judicialDecision(new GAJudicialDecision()
                                      .setDecision(LIST_FOR_A_HEARING))
                .makeAppVisibleToRespondents(new GAMakeApplicationAvailableCheck()
                                                 .setMakeAppAvailableCheck(getMakeAppVisible()))
                .judicialListForHearing(new GAJudgesHearingListGAspec()
                                            .setHearingPreferencesPreferredType(GAJudicialHearingType.TELEPHONE))
                .businessProcess(new BusinessProcess()
                                     .setCamundaEvent(CAMUNDA_EVENT)
                                     .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                     .setStatus(BusinessProcessStatus.FINISHED)
                                     .setActivityId(ACTIVITY_ID))
                .build();
        }
    }

    @Nested
    class SubmittedCallbackHandling {

        @Test
        void callbackHandlingShouldResultInErrorIfTheGAJudicialDecisionIsNull() {
            GeneralApplicationCaseData caseData = getApplication(null, null);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            Assertions.assertThrows(IllegalArgumentException.class, () -> handler.handle(params));
        }

        @Test
        void callbackHandlingForMakeAnOrder() {
            GeneralApplicationCaseData caseData = getApplication(new GAJudicialDecision().setDecision(MAKE_AN_ORDER), null);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            var response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader()).isEqualTo("# Your order has been made");
            assertThat(response.getConfirmationBody()).isEqualTo("<br/><br/>");
        }

        @Test
        void callbackHandlingForListForHearing() {
            GeneralApplicationCaseData caseData = getApplication(new GAJudicialDecision()
                    .setDecision(LIST_FOR_A_HEARING), null);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            var response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader()).isEqualTo("# Your order has been made");
            assertThat(response.getConfirmationBody()).isEqualTo("<br/><br/>");
        }

        @Test
        void callbackHandlingForWrittenRepresentaion() {
            GeneralApplicationCaseData caseData = getApplication(new GAJudicialDecision()
                    .setDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS), null);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            var response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader()).isEqualTo("# Your order has been made");
            assertThat(response.getConfirmationBody()).isEqualTo("<br/><br/>");
        }

        @Test
        void callbackHandlingForRequestInfoFromApplicant() {
            GeneralApplicationCaseData caseData = getApplication(
                new GAJudicialDecision().setDecision(REQUEST_MORE_INFO),
                new GAJudicialRequestMoreInfo()
                    .setRequestMoreInfoOption(REQUEST_MORE_INFORMATION)
                    .setJudgeRequestMoreInfoText("Test")
                    .setJudgeRequestMoreInfoByDate(LocalDate.now())
            );
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            var response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader()).isEqualTo("# You have requested more information");
            assertThat(response.getConfirmationBody()).isEqualTo("<br/><p>The applicant will be notified. "
                                                                     + "They will need to provide a response by "
                                                                     + DATE_FORMATTER_SUBMIT_CALLBACK
                .format(LocalDate.now()) + "</p>");
        }

        @Test
        void callbackHandlingForRequestHearingDetailsFromOtherParty() {
            GeneralApplicationCaseData caseData = getApplication(
                    new GAJudicialDecision().setDecision(REQUEST_MORE_INFO),
                    new GAJudicialRequestMoreInfo()
                            .setRequestMoreInfoOption(SEND_APP_TO_OTHER_PARTY)
                            .setDeadlineForMoreInfoSubmission(LocalDateTime.now())
                            );

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            var response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader()).isEqualTo("# You have requested a response");
            assertThat(response.getConfirmationBody()).isEqualTo("<br/><p>The parties will be notified.</p>");
        }

        @Test
        void callbackHandlingForRequestMoreInfoWithNullGAJudicialRequestMoreInfo() {
            GeneralApplicationCaseData caseData = getApplication(
                    new GAJudicialDecision().setDecision(REQUEST_MORE_INFO),
                    null);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            Assertions.assertThrows(IllegalArgumentException.class, () -> handler.handle(params));
        }

        private GeneralApplicationCaseData getApplication(GAJudicialDecision decision, GAJudicialRequestMoreInfo moreInfo) {
            List<GeneralApplicationTypes> types = List.of(
                    (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
            GeneralApplicationCaseData builder = new GeneralApplicationCaseData();
            if (decision != null && REQUEST_MORE_INFO.equals(decision.getDecision())) {
                builder.judicialDecisionRequestMoreInfo(moreInfo);
            }
            return builder
                    .parentClaimantIsApplicant(YES)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                    .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
                    .applicantPartyName("ApplicantPartyName")
                    .generalAppRespondent1Representative(
                            new GARespondentRepresentative()
                                    .setGeneralAppRespondent1Representative(YES)
                                    )
                    .generalAppType(
                            GAApplicationType
                                    .builder()
                                    .types(types).build())
                    .businessProcess(new BusinessProcess()
                            .setCamundaEvent(CAMUNDA_EVENT)
                            .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                            .setStatus(BusinessProcessStatus.STARTED)
                            .setActivityId(ACTIVITY_ID))
                    .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                    .judicialDecision(decision)
                    .build();
        }
    }

    public GeneralApplicationCaseData getDirectionOrderApplnAndResp(List<GeneralApplicationTypes> types, YesOrNo vulQuestion,
                                                YesOrNo hasRespondentResponseVul) {

        return new GeneralApplicationCaseData()
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.HOUR_1)
                                          .supportRequirement(getApplicantResponses())
                                          .build())
            .respondentsResponses(getRespodentResponses(hasRespondentResponseVul))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .vulnerabilityQuestionsYesOrNo(vulQuestion)
                                          .vulnerabilityQuestion("dummy")
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.HOUR_1)
                                          .supportRequirement(getApplicantResponses())
                                          .hearingPreferredLocation(getLocationDynamicList())
                                          .build())
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    public GeneralApplicationCaseData getDirectionsText(YesOrNo hasRespondentResponseVul) {

        return new GeneralApplicationCaseData()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.HOUR_1)
                                          .supportRequirement(getApplicantResponses())
                                          .build())
            .respondentsResponses(getRespodentResponses(hasRespondentResponseVul))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING)
                                           .setDirectionsText("Test directionText"))
            .build();
    }

    public GeneralApplicationCaseData getMakeAnOrder(YesOrNo hasRespondentResponseVul) {

        return new GeneralApplicationCaseData()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.HOUR_1)
                                          .supportRequirement(getApplicantResponses())
                                          .build())
            .respondentsResponses(getRespodentResponses(hasRespondentResponseVul))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING))
            .build();
    }

    public GeneralApplicationCaseData getGaCaseAppln(List<GeneralApplicationTypes> types, YesOrNo isWithNotice,
                                                YesOrNo hasAgreed) {

        return new GeneralApplicationCaseData()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(hasAgreed).build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.HOUR_1)
                                          .supportRequirement(getApplicantResponses())
                                          .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    public GeneralApplicationCaseData getHearingOrderApplnAndResp(List<GeneralApplicationTypes> types, YesOrNo vulQuestion,
                                                 YesOrNo hasRespondentResponseVul) {

        return new GeneralApplicationCaseData()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .hearingDuration(GAHearingDuration.HOUR_1)
                                    .supportRequirement(getApplicantResponses())
                                    .build())
            .respondentsResponses(getRespodentResponses(hasRespondentResponseVul))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .vulnerabilityQuestionsYesOrNo(vulQuestion)
                                          .vulnerabilityQuestion("dummy")
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.HOUR_1)
                                          .supportRequirement(getApplicantResponses())
                                          .hearingPreferredLocation(getLocationDynamicList())
                                          .build())
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    public GeneralApplicationCaseData getHearingOrderApplnAndResp1and2(List<GeneralApplicationTypes> types, YesOrNo vulQuestion,
                                                YesOrNo hasRespondentResponseVul, YesOrNo hasRespondentResponseVul2) {
        List<Element<GASolicitorDetailsGAspec>> respondentSolicitors = new ArrayList<>();
        respondentSolicitors
            .add(element(GASolicitorDetailsGAspec.builder().id("1L").build()));
        respondentSolicitors
            .add(element(GASolicitorDetailsGAspec.builder().id("2L").build()));

        return new GeneralApplicationCaseData()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .hearingDuration(GAHearingDuration.HOUR_1)
                                    .supportRequirement(getApplicantResponses())
                                    .build())
            .respondentsResponses(getRespondentResponses1nad2(hasRespondentResponseVul, hasRespondentResponseVul2, YES,
                                                              YES))
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .generalAppRespondentSolicitors(respondentSolicitors)
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .vulnerabilityQuestionsYesOrNo(vulQuestion)
                                          .vulnerabilityQuestion("dummy")
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.HOUR_1)
                                          .supportRequirement(getApplicant1Responses())
                                          .hearingPreferredLocation(getLocationDynamicList())
                                          .build())
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    public GeneralApplicationCaseData getHearingOrderAppForCourtLocationPreference(List<GeneralApplicationTypes> types,
                                                                 YesOrNo hasApplPreferLocation,
                                                                 YesOrNo hasResp1PreferLocation,
                                                                 YesOrNo hasResp2PreferLocation) {
        List<Element<GASolicitorDetailsGAspec>> respondentSolicitors = new ArrayList<>();
        respondentSolicitors
            .add(element(GASolicitorDetailsGAspec.builder().id("1L").build()));
        respondentSolicitors
            .add(element(GASolicitorDetailsGAspec.builder().id("2L").build()));

        return new GeneralApplicationCaseData()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .hearingDuration(GAHearingDuration.HOUR_1)
                                    .supportRequirement(getApplicantResponses())
                                    .build())
            .respondentsResponses(getRespondentResponses1nad2(YES, YES, hasResp1PreferLocation, hasResp2PreferLocation))
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .generalAppRespondentSolicitors(respondentSolicitors)
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .vulnerabilityQuestionsYesOrNo(YES)
                                          .vulnerabilityQuestion("dummy")
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.HOUR_1)
                                          .supportRequirement(getApplicant1Responses())
                                          .hearingPreferredLocation(hasApplPreferLocation == YES
                                                                        ? getLocationDynamicList() : null)
                                          .build())
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    public List<Element<GARespondentResponse>> getRespodentResponses(YesOrNo vulQuestion) {

        List<GAHearingSupportRequirements> respSupportReq = new ArrayList<>();
        respSupportReq
            .add(GAHearingSupportRequirements.OTHER_SUPPORT);
        respSupportReq
            .add(GAHearingSupportRequirements.HEARING_LOOPS);

        List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
        respondentsResponses
            .add(element(new GARespondentResponse()
                             .setGaHearingDetails(GAHearingDetails.builder()
                                                   .vulnerabilityQuestionsYesOrNo(vulQuestion)
                                                   .vulnerabilityQuestion("dummy")
                                                   .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                   .hearingDuration(GAHearingDuration.HOUR_1)
                                                   .supportRequirement(respSupportReq)
                                                   .hearingPreferredLocation(getLocationDynamicList())
                                                   .build())
            ));

        return respondentsResponses;
    }

    public List<Element<GARespondentResponse>> getRespondentResponses1nad2(YesOrNo vulQuestion1, YesOrNo vulQuestion2,
                                                                          YesOrNo hasResp1PreferLocation,
                                                                          YesOrNo hasResp2PreferLocation) {

        List<GAHearingSupportRequirements> respSupportReq1 = new ArrayList<>();
        respSupportReq1
            .add(GAHearingSupportRequirements.OTHER_SUPPORT);

        List<GAHearingSupportRequirements> respSupportReq2 = new ArrayList<>();
        respSupportReq2
            .add(GAHearingSupportRequirements.LANGUAGE_INTERPRETER);

        List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
        respondentsResponses
            .add(element(new GARespondentResponse()
                             .setGaHearingDetails(GAHearingDetails.builder()
                                                   .vulnerabilityQuestionsYesOrNo(vulQuestion1)
                                                   .vulnerabilityQuestion("dummy1")
                                                   .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                   .hearingDuration(GAHearingDuration.HOUR_1)
                                                   .supportRequirement(respSupportReq1)
                                                   .hearingPreferredLocation(hasResp1PreferLocation == YES
                                                                                 ? getLocationDynamicList() : null)
                                                   .build())
                             .setGaRespondentDetails("1L")));
        respondentsResponses
            .add(element(new GARespondentResponse()
                             .setGaHearingDetails(GAHearingDetails.builder()
                                                   .vulnerabilityQuestionsYesOrNo(vulQuestion2)
                                                   .vulnerabilityQuestion("dummy2")
                                                   .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                   .hearingDuration(GAHearingDuration.MINUTES_30)
                                                   .supportRequirement(respSupportReq2)
                                                   .hearingPreferredLocation(hasResp2PreferLocation == YES
                                                                                 ? getLocationDynamicList() : null)
                                                   .build())
                             .setGaRespondentDetails("2L")));

        return respondentsResponses;
    }

    public List<MakeAppAvailableCheckGAspec> getMakeAppVisible() {
        List<MakeAppAvailableCheckGAspec> applMakeVisible = new ArrayList<>();
        applMakeVisible
            .add(MakeAppAvailableCheckGAspec.CONSENT_AGREEMENT_CHECKBOX);
        return applMakeVisible;
    }

    public List<GAHearingSupportRequirements> getApplicantResponses() {
        List<GAHearingSupportRequirements> applSupportReq = new ArrayList<>();
        applSupportReq
            .add(GAHearingSupportRequirements.HEARING_LOOPS);
        applSupportReq
            .add(GAHearingSupportRequirements.OTHER_SUPPORT);

        return applSupportReq;
    }

    public List<GAHearingSupportRequirements> getApplicant1Responses() {
        List<GAHearingSupportRequirements> applSupportReq = new ArrayList<>();
        applSupportReq
            .add(GAHearingSupportRequirements.HEARING_LOOPS);

        return applSupportReq;
    }

    public GeneralApplicationCaseData getNotifiedApplication(YesOrNo isWithNotice, YesOrNo parentClaimantIsApplicant) {

        List<GeneralApplicationTypes> types = List.of(
            (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        return new GeneralApplicationCaseData()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .parentClaimantIsApplicant(parentClaimantIsApplicant)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
            .generalAppDetailsOfOrder("Draft order text entered by applicant.")
            .createdDate(LocalDateTime.of(2022, 1, 15, 0, 0, 0))
            .applicantPartyName("ApplicantPartyName")
            .respondentsResponses(getRespodentResponses(YES))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.HOUR_1)
                                          .supportRequirement(getApplicantResponses())
                                          .build())
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .businessProcess(new BusinessProcess()
                                 .setCamundaEvent(CAMUNDA_EVENT)
                                 .setProcessInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .setStatus(BusinessProcessStatus.STARTED)
                                 .setActivityId(ACTIVITY_ID))
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();
    }

    public GAJudicialMakeAnOrder getJudicialMakeAnOrder(AboutToStartOrSubmitCallbackResponse response) {
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        return responseCaseData.getJudicialDecisionMakeOrder();
    }

    public GAJudicialRequestMoreInfo getJudicialRequestMoreInfo(AboutToStartOrSubmitCallbackResponse response) {
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        return responseCaseData.getJudicialDecisionRequestMoreInfo();
    }

    public YesOrNo getApplicationIsCloakedStatus(AboutToStartOrSubmitCallbackResponse response) {
        GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        return responseCaseData.getApplicationIsCloaked();
    }

    public DynamicList getLocationDynamicList() {
        DynamicListElement location1 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("ABCD - RG0 0AL").build();
        DynamicListElement location2 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("PQRS - GU0 0EE").build();
        DynamicListElement location3 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("WXYZ - EW0 0HE").build();
        DynamicListElement location4 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("LMNO - NE0 0BH").build();

        return DynamicList.builder()
            .listItems(List.of(location1, location2, location3, location4))
            .value(location1).build();
    }

    public List<Element<GASolicitorDetailsGAspec>> getRespondentSolicitors() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email("test@gmail.com").organisationIdentifier("org2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
            .email("test@gmail.com").organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        return respondentSols;
    }
}

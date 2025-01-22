package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SetOrderDetailsFlags;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder.GenerateSdoOrder;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateorderdetailspages.PrePopulateOrderDetailsPages;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.SubmitSDO;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.CreateSDOCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SDOHearingNotes;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsAddNewDirections;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_HEADER_SDO;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_SUMMARY_1_V_1;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_SUMMARY_1_V_2;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_SUMMARY_2_V_1;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.FEEDBACK_LINK;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.ADDENDUM_REPORT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.DEADLINE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.IMPORTANT_NOTES;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.INSPECTION;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.JUDGE_RECITAL;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.PECUNIARY_LOSS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.REPLIES;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.SERVICE_OF_ORDER;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.SERVICE_REPORT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.STATEMENT_WITNESS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate.FIVE_HOURS;
import static uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions.OPEN_DATE;

@SpringBootTest(classes = {
    CreateSDOCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimUrlsConfiguration.class,
    MockDatabaseConfiguration.class,
    WorkingDayIndicator.class,
    DeadlinesCalculator.class,
    ValidationAutoConfiguration.class,
    LocationHelper.class,
    AssignCategoryId.class,
    CreateSDOCallbackHandlerTestConfig.class},
    properties = {"reference.database.enabled=false"})
public class CreateSDOCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000DC001";
    private static final DynamicList options = DynamicList.builder()
        .listItems(List.of(
            DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
            DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
            DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
        )).build();

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CreateSDOCallbackHandler handler;

    @Autowired
    private AssignCategoryId assignCategoryId;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private SdoGeneratorService sdoGeneratorService;

    @MockBean
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @MockBean
    private CategoryService categoryService;

    @Mock
    private LocationHelper locationHelper;

    @Mock
    private GenerateSdoOrder generateSdoOrder;

    @Mock
    private PrePopulateOrderDetailsPages prePopulateOrderDetailsPages;

    @Mock
    private SubmitSDO submitSDO;

    @Mock
    private SetOrderDetailsFlags setOrderDetailsFlags;

    @MockBean
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    @Nested
    class AboutToStartCallback extends LocationRefSampleDataBuilder {
        @BeforeEach
        void setup() {
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(workingDayIndicator.getNextWorkingDay(LocalDate.now())).thenReturn(LocalDate.now().plusDays(1));
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourt() {
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                    .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
                LocationRefData.builder().epimmsId(preSelectedCourt).courtLocationCode(preSelectedCourt)
                    .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
                LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                    .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .caseAccessCategory(UNSPEC_CLAIM)
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList expected = DynamicList.builder()
                .listItems(List.of(
                               DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                               DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build(),
                               DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                           )
                )
                .value(DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build())
                .build();

            assertThat(responseCaseData.getDisposalHearingMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getFastTrackMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getSmallClaimsMethodInPerson()).isEqualTo(expected);
        }

        @Test
        void shouldGenerateDynamicListsCorrectly() {
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList hearingMethodValuesFastTrack = responseCaseData.getHearingMethodValuesFastTrack();
            DynamicList hearingMethodValuesDisposalHearing = responseCaseData.getHearingMethodValuesDisposalHearing();
            DynamicList hearingMethodValuesSmallClaims = responseCaseData.getHearingMethodValuesSmallClaims();

            List<String> hearingMethodValuesFastTrackActual = hearingMethodValuesFastTrack.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .toList();

            List<String> hearingMethodValuesDisposalHearingActual = hearingMethodValuesDisposalHearing.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .toList();

            List<String> hearingMethodValuesSmallClaimsActual = hearingMethodValuesSmallClaims.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .toList();

            assertThat(hearingMethodValuesFastTrackActual).containsOnly("In Person");
            assertThat(hearingMethodValuesDisposalHearingActual).containsOnly("In Person");
            assertThat(hearingMethodValuesSmallClaimsActual).containsOnly("In Person");
        }

        @Test
        void shouldClearDataIfStateIsCaseProgression() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            List<FastTrack> directions = List.of(FastTrack.fastClaimBuildingDispute);
            List<SmallTrack> smallDirections = List.of(SmallTrack.smallClaimCreditHire);
            DisposalHearingAddNewDirections disposalHearingAddNewDirections = DisposalHearingAddNewDirections.builder()
                .directionComment("test")
                .build();
            Element<DisposalHearingAddNewDirections> disposalHearingAddNewDirectionsElement =
                Element.<DisposalHearingAddNewDirections>builder()
                    .value(disposalHearingAddNewDirections)
                    .build();
            SmallClaimsAddNewDirections smallClaimsAddNewDirections = SmallClaimsAddNewDirections.builder()
                .directionComment("test")
                .build();

            Element<SmallClaimsAddNewDirections> smallClaimsAddNewDirectionsElement =
                Element.<SmallClaimsAddNewDirections>builder()
                    .value(smallClaimsAddNewDirections)
                    .build();

            FastTrackAddNewDirections fastTrackAddNewDirections = FastTrackAddNewDirections.builder()
                .directionComment("test")
                .build();

            Element<FastTrackAddNewDirections> fastTrackAddNewDirectionsElement =
                Element.<FastTrackAddNewDirections>builder()
                    .value(fastTrackAddNewDirections)
                    .build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .drawDirectionsOrderRequired(YES)
                .drawDirectionsOrderSmallClaims(YES)
                .fastClaims(directions)
                .smallClaims(smallDirections)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .orderType(OrderType.DECIDE_DAMAGES)
                .trialAdditionalDirectionsForFastTrack(directions)
                .drawDirectionsOrderSmallClaimsAdditionalDirections(smallDirections)
                .fastTrackAllocation(FastTrackAllocation.builder().assignComplexityBand(YES).build())
                .disposalHearingAddNewDirections(List.of(disposalHearingAddNewDirectionsElement))
                .smallClaimsAddNewDirections(List.of(smallClaimsAddNewDirectionsElement))
                .fastTrackAddNewDirections(List.of(fastTrackAddNewDirectionsElement))
                .sdoHearingNotes(SDOHearingNotes.builder().input("TEST").build())
                .fastTrackHearingNotes(FastTrackHearingNotes.builder().input("TEST").build())
                .disposalHearingHearingNotes("TEST")
                .ccdState(CASE_PROGRESSION)
                .decisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_SDO)
                .isSdoR2NewScreen(NO)
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getDrawDirectionsOrderRequired()).isNull();
            assertThat(responseCaseData.getDrawDirectionsOrderSmallClaims()).isNull();
            assertThat(responseCaseData.getFastClaims()).isNull();
            assertThat(responseCaseData.getSmallClaims()).isNull();
            assertThat(responseCaseData.getClaimsTrack()).isNull();
            assertThat(responseCaseData.getOrderType()).isNull();
            assertThat(responseCaseData.getTrialAdditionalDirectionsForFastTrack()).isNull();
            assertThat(responseCaseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections()).isNull();
            assertThat(responseCaseData.getFastTrackAllocation()).isNull();
            assertThat(responseCaseData.getDisposalHearingAddNewDirections()).isNull();
            assertThat(responseCaseData.getSmallClaimsAddNewDirections()).isNull();
            assertThat(responseCaseData.getFastTrackAddNewDirections()).isNull();
            assertThat(responseCaseData.getSdoHearingNotes()).isNull();
            assertThat(responseCaseData.getFastTrackHearingNotes()).isNull();
            assertThat(responseCaseData.getDisposalHearingHearingNotes()).isNull();
        }

        @Test
        void shouldPopulateHearingCourtLocationForNihl() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                    .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
                LocationRefData.builder().epimmsId(preSelectedCourt).courtLocationCode(preSelectedCourt)
                    .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
                LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                    .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(List.of(FastTrack.fastClaimNoiseInducedHearingLoss))
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList altExpected = DynamicList.builder()
                .listItems(List.of(
                               DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                               DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build(),
                               DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                           )
                )
                .build();

            DynamicList expected = DynamicList.builder()
                .value(
                    DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build()
                )
                .listItems(List.of(
                               DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build(),
                               DynamicListElement.builder().code("OTHER_LOCATION").label("Other location").build()
                           )
                )
                .build();

            assertThat(responseCaseData.getSdoR2Trial().getHearingCourtLocationList()).isEqualTo(expected);
            assertThat(responseCaseData.getSdoR2Trial().getAltHearingCourtLocationList()).isEqualTo(altExpected);
        }

        @Test
        void shouldPopulateDefaultFieldsForNihl() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                    .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
                LocationRefData.builder().epimmsId(preSelectedCourt).courtLocationCode(preSelectedCourt)
                    .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
                LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                    .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);

            Category category = Category.builder().categoryKey("HearingChannel").key(HearingSubChannel.INTER.name())
                .valueEn(HearingMethod.IN_PERSON.getLabel()).activeFlag("Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(fastTrackList)
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("sdoFastTrackJudgesRecital")
                .extracting("input").asString().isEqualTo(JUDGE_RECITAL);
            assertThat(response.getData()).extracting("sdoR2DisclosureOfDocuments")
                .extracting("standardDisclosureTxt").asString().isEqualTo(STANDARD_DISCLOSURE);
            assertThat(response.getData()).extracting("sdoR2DisclosureOfDocuments")
                .extracting("standardDisclosureDate").asString().isEqualTo(LocalDate.now().plusDays(28).toString());
            assertThat(response.getData()).extracting("sdoR2DisclosureOfDocuments")
                .extracting("inspectionTxt").asString().isEqualTo(INSPECTION);
            assertThat(response.getData()).extracting("sdoR2DisclosureOfDocuments")
                .extracting("inspectionDate").asString().isEqualTo(LocalDate.now().plusDays(42).toString());
            assertThat(response.getData()).extracting("sdoR2DisclosureOfDocuments")
                .extracting("requestsWillBeCompiledLabel").asString().isEqualTo(REQUEST_COMPILED_WITH);
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoStatementOfWitness").asString().isEqualTo(
                STATEMENT_WITNESS);
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoR2RestrictWitness").extracting(
                "isRestrictWitness").asString().isEqualTo("No");
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoR2RestrictWitness")
                .extracting("restrictNoOfWitnessDetails").extracting("noOfWitnessClaimant").asString().isEqualTo("3");
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoR2RestrictWitness")
                .extracting("restrictNoOfWitnessDetails").extracting("noOfWitnessDefendant").asString().isEqualTo("3");
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoR2RestrictWitness")
                .extracting("restrictNoOfWitnessDetails").extracting("partyIsCountedAsWitnessTxt").asString().isEqualTo(
                    RESTRICT_WITNESS_TEXT);
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoRestrictPages")
                .extracting("isRestrictPages").asString().isEqualTo("No");
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoRestrictPages")
                .extracting("isRestrictPages").asString().isEqualTo("No");
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoRestrictPages")
                .extracting("restrictNoOfPagesDetails").extracting("witnessShouldNotMoreThanTxt").asString().isEqualTo(
                    RESTRICT_NUMBER_PAGES_TEXT1);
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoRestrictPages")
                .extracting("restrictNoOfPagesDetails").extracting("noOfPages").asString().isEqualTo("12");
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoRestrictPages")
                .extracting("restrictNoOfPagesDetails").extracting("fontDetails").asString().isEqualTo(
                    RESTRICT_NUMBER_PAGES_TEXT2);
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact").extracting("sdoWitnessDeadline").asString().isEqualTo(
                DEADLINE);
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact")
                .extracting("sdoWitnessDeadlineDate").asString().isEqualTo(LocalDate.now().plusDays(70).toString());
            assertThat(response.getData()).extracting("sdoR2WitnessesOfFact")
                .extracting("sdoWitnessDeadlineText").asString().isEqualTo(DEADLINE_EVIDENCE);
            assertThat(response.getData()).extracting("sdoR2ScheduleOfLoss")
                .extracting("sdoR2ScheduleOfLossClaimantText").asString().isEqualTo(SCHEDULE_OF_LOSS_CLAIMANT);
            assertThat(response.getData()).extracting("sdoR2ScheduleOfLoss")
                .extracting("isClaimForPecuniaryLoss").asString().isEqualTo("No");
            assertThat(response.getData()).extracting("sdoR2ScheduleOfLoss")
                .extracting("sdoR2ScheduleOfLossClaimantDate").asString().isEqualTo(LocalDate.now().plusDays(364).toString());
            assertThat(response.getData()).extracting("sdoR2ScheduleOfLoss")
                .extracting("sdoR2ScheduleOfLossDefendantText").asString().isEqualTo(SCHEDULE_OF_LOSS_DEFENDANT);
            assertThat(response.getData()).extracting("sdoR2ScheduleOfLoss")
                .extracting("sdoR2ScheduleOfLossDefendantDate").asString().isEqualTo(LocalDate.now().plusDays(378).toString());
            assertThat(response.getData()).extracting("sdoR2ScheduleOfLoss")
                .extracting("sdoR2ScheduleOfLossPecuniaryLossTxt").asString().isEqualTo(PECUNIARY_LOSS);
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("trialOnOptions").asString().isEqualTo(OPEN_DATE.toString());
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("lengthList").asString().isEqualTo(FIVE_HOURS.toString());
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("methodOfHearing").extracting("value").extracting("label").asString().isEqualTo(
                    HearingMethod.IN_PERSON.getLabel());
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("physicalBundleOptions").asString().isEqualTo(PhysicalTrialBundleOptions.PARTY.toString());
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("sdoR2TrialFirstOpenDateAfter").extracting("listFrom").asString().isEqualTo(LocalDate.now().plusDays(
                    434).toString());
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("sdoR2TrialWindow").extracting("listFrom").asString().isEqualTo(LocalDate.now().plusDays(434).toString());
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("sdoR2TrialWindow").extracting("dateTo").asString().isEqualTo(LocalDate.now().plusDays(455).toString());
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("hearingCourtLocationList").asString().isEqualTo(
                    "{value={code=214320, label=court 2 - 2 address - Y02 7RB}, list_items=[{code=214320, " +
                        "label=court 2 - 2 address - Y02 7RB}, {code=OTHER_LOCATION, label=Other location}]}");
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("altHearingCourtLocationList").asString()
                .isEqualTo("{value={}, list_items=[{code=00001, label=court 1 - 1 address - Y01 7RB}, " +
                               "{code=214320, label=court 2 - 2 address - Y02 7RB}, {code=00003, label=court 3 - 3 address - Y03 7RB}]}");
            assertThat(response.getData()).extracting("sdoR2Trial")
                .extracting("physicalBundlePartyTxt").asString().isEqualTo(PHYSICAL_TRIAL_BUNDLE);
            assertThat(response.getData()).extracting("sdoR2ImportantNotesTxt").asString().isEqualTo(IMPORTANT_NOTES);
            assertThat(response.getData()).extracting("sdoR2ImportantNotesDate").asString().isEqualTo(LocalDate.now().plusDays(
                7).toString());
            assertThat(response.getData()).extracting("sdoR2ExpertEvidence")
                .extracting("sdoClaimantPermissionToRelyTxt").asString().isEqualTo(CLAIMANT_PERMISSION_TO_RELY);
            assertThat(response.getData()).extracting("sdoR2AddendumReport")
                .extracting("sdoAddendumReportTxt").asString().isEqualTo(ADDENDUM_REPORT);
            assertThat(response.getData()).extracting("sdoR2AddendumReport")
                .extracting("sdoAddendumReportDate").asString().isEqualTo(LocalDate.now().plusDays(56).toString());
            assertThat(response.getData()).extracting("sdoR2FurtherAudiogram")
                .extracting("sdoClaimantShallUndergoTxt").asString().isEqualTo(CLAIMANT_SHALL_UNDERGO);
            assertThat(response.getData()).extracting("sdoR2FurtherAudiogram")
                .extracting("sdoServiceReportTxt").asString().isEqualTo(SERVICE_REPORT);
            assertThat(response.getData()).extracting("sdoR2FurtherAudiogram")
                .extracting("sdoClaimantShallUndergoDate").asString().isEqualTo(LocalDate.now().plusDays(42).toString());
            assertThat(response.getData()).extracting("sdoR2FurtherAudiogram")
                .extracting("sdoServiceReportDate").asString().isEqualTo(LocalDate.now().plusDays(98).toString());
            assertThat(response.getData()).extracting("sdoR2QuestionsClaimantExpert")
                .extracting("sdoDefendantMayAskTxt").asString().isEqualTo(DEFENDANT_MAY_ASK);
            assertThat(response.getData()).extracting("sdoR2QuestionsClaimantExpert")
                .extracting("sdoDefendantMayAskDate").asString().isEqualTo(LocalDate.now().plusDays(126).toString());
            assertThat(response.getData()).extracting("sdoR2QuestionsClaimantExpert")
                .extracting("sdoQuestionsShallBeAnsweredTxt").asString().isEqualTo(QUESTIONS_SHALL_BE_ANSWERED);
            assertThat(response.getData()).extracting("sdoR2QuestionsClaimantExpert")
                .extracting("sdoQuestionsShallBeAnsweredDate").asString().isEqualTo(LocalDate.now().plusDays(147).toString());
            assertThat(response.getData()).extracting("sdoR2QuestionsClaimantExpert")
                .extracting("sdoUploadedToDigitalPortalTxt").asString().isEqualTo(UPLOADED_TO_DIGITAL_PORTAL);
            assertThat(response.getData()).extracting("sdoR2QuestionsClaimantExpert")
                .extracting("sdoApplicationToRelyOnFurther").extracting("doRequireApplicationToRely").asString().isEqualTo(
                    "No");
            assertThat(response.getData()).extracting("sdoR2QuestionsClaimantExpert")
                .extracting("sdoApplicationToRelyOnFurther")
                .extracting("applicationToRelyOnFurtherDetails").extracting("applicationToRelyDetailsTxt").asString().isEqualTo(
                    APPLICATION_TO_RELY_DETAILS);
            assertThat(response.getData()).extracting("sdoR2QuestionsClaimantExpert")
                .extracting("sdoApplicationToRelyOnFurther").extracting("applicationToRelyOnFurtherDetails")
                .extracting("applicationToRelyDetailsDate").asString().isEqualTo(LocalDate.now().plusDays(161).toString());
            assertThat(response.getData()).extracting("sdoR2PermissionToRelyOnExpert")
                .extracting("sdoPermissionToRelyOnExpertTxt").asString().isEqualTo(PERMISSION_TO_RELY_ON_EXPERT);
            assertThat(response.getData()).extracting("sdoR2PermissionToRelyOnExpert")
                .extracting("sdoPermissionToRelyOnExpertDate").asString().isEqualTo(LocalDate.now().plusDays(119).toString());
            assertThat(response.getData()).extracting("sdoR2PermissionToRelyOnExpert")
                .extracting("sdoJointMeetingOfExpertsTxt").asString().isEqualTo(JOINT_MEETING_OF_EXPERTS);
            assertThat(response.getData()).extracting("sdoR2PermissionToRelyOnExpert")
                .extracting("sdoJointMeetingOfExpertsDate").asString().isEqualTo(LocalDate.now().plusDays(147).toString());
            assertThat(response.getData()).extracting("sdoR2PermissionToRelyOnExpert")
                .extracting("sdoUploadedToDigitalPortalTxt").asString().isEqualTo(UPLOADED_TO_DIGITAL_PORTAL_7_DAYS);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoEvidenceAcousticEngineerTxt").asString().isEqualTo(EVIDENCE_ACOUSTIC_ENGINEER);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoInstructionOfTheExpertTxt").asString().isEqualTo(INSTRUCTION_OF_EXPERT);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoInstructionOfTheExpertDate").asString().isEqualTo(LocalDate.now().plusDays(42).toString());
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoInstructionOfTheExpertTxtArea").asString().isEqualTo(INSTRUCTION_OF_EXPERT_TA);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoExpertReportTxt").asString().isEqualTo(EXPERT_REPORT);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoExpertReportDate").asString().isEqualTo(LocalDate.now().plusDays(280).toString());
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoExpertReportDigitalPortalTxt").asString().isEqualTo(EXPERT_REPORT_DIGITAL_PORTAL);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoWrittenQuestionsTxt").asString().isEqualTo(WRITTEN_QUESTIONS);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoWrittenQuestionsDate").asString().isEqualTo(LocalDate.now().plusDays(294).toString());
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoWrittenQuestionsDigitalPortalTxt").asString().isEqualTo(WRITTEN_QUESTIONS_DIGITAL_PORTAL);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoRepliesTxt").asString().isEqualTo(REPLIES);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoRepliesDate").asString().isEqualTo(LocalDate.now().plusDays(315).toString());
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoRepliesDigitalPortalTxt").asString().isEqualTo(REPLIES_DIGITAL_PORTAL);
            assertThat(response.getData()).extracting("sdoR2EvidenceAcousticEngineer")
                .extracting("sdoServiceOfOrderTxt").asString().isEqualTo(SERVICE_OF_ORDER);
            assertThat(response.getData()).extracting("sdoR2QuestionsToEntExpert")
                .extracting("sdoWrittenQuestionsTxt").asString().isEqualTo(ENT_WRITTEN_QUESTIONS);
            assertThat(response.getData()).extracting("sdoR2QuestionsToEntExpert")
                .extracting("sdoWrittenQuestionsDate").asString().isEqualTo(LocalDate.now().plusDays(336).toString());
            assertThat(response.getData()).extracting("sdoR2QuestionsToEntExpert")
                .extracting("sdoWrittenQuestionsDigPortalTxt").asString().isEqualTo(ENT_WRITTEN_QUESTIONS_DIG_PORTAL);
            assertThat(response.getData()).extracting("sdoR2QuestionsToEntExpert")
                .extracting("sdoQuestionsShallBeAnsweredTxt").asString().isEqualTo(ENT_QUESTIONS_SHALL_BE_ANSWERED);
            assertThat(response.getData()).extracting("sdoR2QuestionsToEntExpert")
                .extracting("sdoQuestionsShallBeAnsweredDate").asString().isEqualTo(LocalDate.now().plusDays(350).toString());
            assertThat(response.getData()).extracting("sdoR2QuestionsToEntExpert")
                .extracting("sdoShallBeUploadedTxt").asString().isEqualTo(ENT_SHALL_BE_UPLOADED);
            assertThat(response.getData()).extracting("sdoR2UploadOfDocuments")
                .extracting("sdoUploadOfDocumentsTxt").asString().isEqualTo(UPLOAD_OF_DOCUMENTS);

            assertThat(response.getData()).extracting("sdoAltDisputeResolution").extracting("includeInOrderToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoVariationOfDirections").extracting("includeInOrderToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2Settlement").extracting("includeInOrderToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2DisclosureOfDocumentsToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2SeparatorWitnessesOfFactToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2SeparatorExpertEvidenceToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2SeparatorAddendumReportToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2SeparatorFurtherAudiogramToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2SeparatorQuestionsClaimantExpertToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2SeparatorPermissionToRelyOnExpertToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2SeparatorEvidenceAcousticEngineerToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2SeparatorQuestionsToEntExpertToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2ScheduleOfLossToggle").asString().isEqualTo("[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2SeparatorUploadOfDocumentsToggle").asString().isEqualTo(
                "[INCLUDE]");
            assertThat(response.getData()).extracting("sdoR2TrialToggle").asString().isEqualTo("[INCLUDE]");
        }

        @Test
        void shouldPrePopulateUpdatedWitnessSectionsForSDOR2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();

            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getText()).isEqualTo(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getSdoStatementOfWitness())
                .isEqualTo(
                    "Each party must upload to the Digital Portal copies of all witness statements of the witnesses"
                        + " upon whose evidence they intend to rely at the hearing not less than 21 days before"
                        + " the hearing.");
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getIsRestrictWitness()).isEqualTo(NO);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getIsRestrictPages()).isEqualTo(NO);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getSdoR2SmallClaimsRestrictWitness()
                           .getPartyIsCountedAsWitnessTxt()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther()
                           .getSdoR2SmallClaimsRestrictPages().getFontDetails()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther()
                           .getSdoR2SmallClaimsRestrictPages().getWitnessShouldNotMoreThanTxt()).isEqualTo(
                SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther()
                           .getSdoR2SmallClaimsRestrictPages().getNoOfPages()).isEqualTo(12);

            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoStatementOfWitness()).isEqualTo(
                SdoR2UiConstantFastTrack.STATEMENT_WITNESS);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoR2RestrictWitness().getIsRestrictWitness()).isEqualTo(
                NO);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessClaimant()).isEqualTo(
                3);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessDefendant()).isEqualTo(
                3);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getPartyIsCountedAsWitnessTxt())
                .isEqualTo(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoRestrictPages().getIsRestrictPages()).isEqualTo(NO);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoRestrictPages().getRestrictNoOfPagesDetails().getWitnessShouldNotMoreThanTxt())
                .isEqualTo(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoRestrictPages().getRestrictNoOfPagesDetails().getNoOfPages()).isEqualTo(
                12);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoRestrictPages().getRestrictNoOfPagesDetails().getFontDetails())
                .isEqualTo(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoWitnessDeadline()).isEqualTo(SdoR2UiConstantFastTrack.DEADLINE);
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoWitnessDeadlineDate()).isEqualTo(LocalDate.now().plusDays(
                70));
            assertThat(data.getSdoR2FastTrackWitnessOfFact().getSdoWitnessDeadlineText()).isEqualTo(
                SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldPopulateWelshSectionForSDOR2(boolean valid) {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO).build();

            when(featureToggleService.isSdoR2Enabled()).thenReturn(valid);
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
            if (valid) {
                assertThat(response.getData()).extracting("sdoR2FastTrackUseOfWelshLanguage")
                    .extracting("description").isEqualTo(WELSH_LANG_DESCRIPTION);
                assertThat(response.getData()).extracting("sdoR2SmallClaimsUseOfWelshLanguage")
                    .extracting("description").isEqualTo(WELSH_LANG_DESCRIPTION);
                assertThat(response.getData()).extracting("sdoR2DisposalHearingUseOfWelshLanguage")
                    .extracting("description").isEqualTo(WELSH_LANG_DESCRIPTION);
                assertThat(response.getData()).extracting("sdoR2DrhUseOfWelshLanguage")
                    .extracting("description").isEqualTo(WELSH_LANG_DESCRIPTION);
                assertThat(response.getData()).extracting("sdoR2NihlUseOfWelshLanguage")
                    .extracting("description").isEqualTo(WELSH_LANG_DESCRIPTION);
            } else {
                assertThat(responseCaseData.getSdoR2FastTrackUseOfWelshLanguage()).isNull();
                assertThat(responseCaseData.getSdoR2SmallClaimsUseOfWelshLanguage()).isNull();
                assertThat(responseCaseData.getSdoR2DisposalHearingUseOfWelshLanguage()).isNull();
                assertThat(responseCaseData.getSdoR2DrhUseOfWelshLanguage()).isNull();
                assertThat(responseCaseData.getSdoR2NihlUseOfWelshLanguage()).isNull();
            }
        }

        @Test
        void shouldUpdateCaseManagementLocation_whenUnder1000SpecCcmcc() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation(handler.ccmccEpimsId).region(
                    "ccmcRegion").build())
                .totalClaimAmount(BigDecimal.valueOf(999))
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .applicant1(Party.builder()
                                .type(Party.Type.INDIVIDUAL)
                                .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQRequestedCourt(
                                      RequestedCourt.builder()
                                          .caseLocation(CaseLocationCivil.builder()
                                                            .baseLocation("app court requested epimm")
                                                            .region("app court request region").build())
                                          .responseCourtCode("123")
                                          .build()
                                  )
                                  .build())
                .respondent1(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQRequestedCourt(
                                       RequestedCourt.builder()
                                           .caseLocation(CaseLocationCivil.builder()
                                                             .baseLocation("def court requested epimm")
                                                             .region("def court request region").build())
                                           .responseCourtCode("321")
                                           .build()
                                   )
                                   .build())
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsExactly("def court request region", "def court requested epimm");
        }

        @Test
        void shouldNotUpdateCaseManagementLocation_whenNotUnder1000SpecCcmcc() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("1010101").region("orange").build())
                .totalClaimAmount(BigDecimal.valueOf(1999))
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .applicant1(Party.builder()
                                .type(Party.Type.INDIVIDUAL)
                                .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQRequestedCourt(
                                      RequestedCourt.builder()
                                          .caseLocation(CaseLocationCivil.builder()
                                                            .baseLocation("app court requested epimm")
                                                            .region("app court request region").build())
                                          .responseCourtCode("123")
                                          .build()
                                  )
                                  .build())
                .respondent1(Party.builder()
                                 .type(Party.Type.INDIVIDUAL)
                                 .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQRequestedCourt(
                                       RequestedCourt.builder()
                                           .caseLocation(CaseLocationCivil.builder()
                                                             .baseLocation("def court requested epimm")
                                                             .region("def court request region").build())
                                           .responseCourtCode("321")
                                           .build()
                                   )
                                   .build())
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsExactly("orange", "1010101");
        }

    }

    @Nested
    class AboutToSubmitCallback {

        private CallbackParams params;
        private CaseData caseData;
        private String userId;

        private static final String EMAIL = "example@email.com";
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            List<String> items = List.of("label 1", "label 2", "label 3");
            DynamicList localOptions = DynamicList.fromList(items, Object::toString, items.get(0), false);
            caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .build().toBuilder()
                .disposalHearingMethodInPerson(localOptions)
                .fastTrackMethodInPerson(localOptions)
                .smallClaimsMethodInPerson(localOptions)
                .setFastTrackFlag(YES)
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            userId = UUID.randomUUID().toString();

            given(time.now()).willReturn(submittedDate);

            given(featureToggleService.isLocationWhiteListedForCaseProgression(anyString())).willReturn(true);
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                params.toBuilder()
                    .caseData(params.getCaseData().toBuilder()
                                  .claimsTrack(ClaimsTrack.smallClaimsTrack)
                                  .drawDirectionsOrderRequired(NO)
                                  .build())
                    .build()
            );

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(CREATE_SDO.name(), "READY");
        }
    }

    @Nested
    class AboutToSubmitCallbackVariableCase {

        private String userId;

        private static final String EMAIL = "example@email.com";
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            userId = UUID.randomUUID().toString();

            given(time.now()).willReturn(submittedDate);
        }

        @Test
        void shouldReturnNullDocument_whenInvokedAboutToSubmit() {
            List<String> items = List.of("label 1", "label 2", "label 3");
            DynamicList localOptions = DynamicList.fromList(
                items,
                Object::toString,
                Object::toString,
                items.get(0),
                false
            );
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .build().toBuilder()
                .fastTrackMethodInPerson(localOptions)
                .disposalHearingMethodInPerson(localOptions)
                .smallClaimsMethodInPerson(localOptions)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("sdoOrderDocument");
        }
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSetEarlyAdoptersFlagToFalse_WhenLiP(Boolean isLocationWhiteListed) {
        DynamicList localOptions = DynamicList.builder()
            .listItems(List.of(
                           DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                           DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                           DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                       )
            )
            .build();

        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson)
            .disposalHearingMethodInPerson(localOptions.toBuilder().value(selectedCourt).build())
            .fastTrackMethodInPerson(localOptions)
            .smallClaimsMethodInPerson(localOptions)
            .disposalHearingMethodInPerson(localOptions.toBuilder().value(selectedCourt).build())
            .disposalHearingMethodToggle(Collections.singletonList(OrderDetailsPagesSectionsToggle.SHOW))
            .orderType(OrderType.DISPOSAL)
            .respondent1Represented(NO)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(featureToggleService.isLocationWhiteListedForCaseProgression(selectedCourt.getCode()))
            .thenReturn(isLocationWhiteListed);
        when(locationRefDataService.getLocationMatchingLabel(selectedCourt.getCode(), params.getParams().get(
            CallbackParams.Params.BEARER_TOKEN).toString()))
            .thenReturn(Optional.of(LocationRefData.builder()
                                        .regionId("region id")
                                        .epimmsId("epimms id")
                                        .siteName("site name")
                                        .build()));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getEaCourtLocation()).isEqualTo(NO);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldPopulateHmcEarlyAdoptersFlag_whenHmcIsEnabled(Boolean isLocationWhiteListed) {
        DynamicList options = DynamicList.builder()
            .listItems(List.of(
                           DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                           DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                           DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                       )
            )
            .build();

        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson)
            .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
            .fastTrackMethodInPerson(options)
            .smallClaimsMethodInPerson(options)
            .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
            .disposalHearingMethodToggle(Collections.singletonList(OrderDetailsPagesSectionsToggle.SHOW))
            .orderType(OrderType.DISPOSAL)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(featureToggleService.isLocationWhiteListedForCaseProgression(eq(selectedCourt.getCode()))).thenReturn(
            isLocationWhiteListed);
        when(locationRefDataService.getLocationMatchingLabel(selectedCourt.getCode(), params.getParams().get(
            CallbackParams.Params.BEARER_TOKEN).toString()))
            .thenReturn(Optional.of(LocationRefData.builder()
                                        .regionId("region id")
                                        .epimmsId("epimms id")
                                        .siteName("site name")
                                        .build()));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getHmcEaCourtLocation()).isEqualTo(isLocationWhiteListed ? YES : NO);
    }

    @ParameterizedTest
    @CsvSource({
        "true, NO, NO, YES",
        "false, NO, NO, NO",
        "true, YES, NO, YES"
    })
    void shouldSetEaCourtLocationBasedOnConditions(boolean isLocationWhiteListed, YesOrNo applicant1Represented, YesOrNo respondent1Represented, YesOrNo expectedEaCourtLocation) {
        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .respondent1Represented(respondent1Represented)
            .applicant1Represented(applicant1Represented)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(isLocationWhiteListed);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertEquals(expectedEaCourtLocation, responseCaseData.getEaCourtLocation());
    }

    @Test
    void shouldNotPopulateHmcEarlyAdoptersFlag_whenLiP() {
        DynamicList options = DynamicList.builder()
            .listItems(List.of(
                           DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                           DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                           DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                       )
            )
            .build();

        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(selectedCourt.getCode()).build())
            .disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson)
            .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
            .fastTrackMethodInPerson(options)
            .smallClaimsMethodInPerson(options)
            .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
            .disposalHearingMethodToggle(Collections.singletonList(OrderDetailsPagesSectionsToggle.SHOW))
            .orderType(OrderType.DISPOSAL)
            .build();

        when(featureToggleService.isLocationWhiteListedForCaseProgression(eq(selectedCourt.getCode()))).thenReturn(true);

        CallbackParams params = callbackParamsOf(caseData.toBuilder()
                                                     .applicant1Represented(YesOrNo.NO)
                                                     .build(), ABOUT_TO_SUBMIT);
        when(locationRefDataService.getLocationMatchingLabel(selectedCourt.getCode(), params.getParams().get(
            CallbackParams.Params.BEARER_TOKEN).toString()))
            .thenReturn(Optional.of(LocationRefData.builder()
                                        .regionId("region id")
                                        .epimmsId("epimms id")
                                        .siteName("site name")
                                        .build()));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getHmcEaCourtLocation()).isNull();

        params = callbackParamsOf(caseData.toBuilder()
                                      .respondent1Represented(YesOrNo.NO)
                                      .build(), ABOUT_TO_SUBMIT);

        response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getHmcEaCourtLocation()).isNull();
    }

    @Test
    void shouldNotCallUpdateWaCourtLocationsServiceWhenNotPresent_AndMintiEnabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CreateSDOCallbackHandler createSDOCallbackHandler =
                new CreateSDOCallbackHandler(
                        generateSdoOrder,
                        prePopulateOrderDetailsPages,
                        new SubmitSDO(objectMapper, List.of(), featureToggleService, Optional.empty()),
                        setOrderDetailsFlags
                );

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("123456").build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, CREATE_SDO, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) createSDOCallbackHandler.handle(params);

        verifyNoInteractions(updateWaCourtLocationsService);

        assertThat(response.getData()).doesNotContainKey("sdoOrderDocument");
        assertThat(response.getData()).containsEntry("businessProcess", Map.of(
                "camundaEvent", CREATE_SDO.name(),
                "status", "READY",
                "readyOn", ((Map<?, ?>) response.getData().get("businessProcess")).get("readyOn")
        ));
    }

    @Test
    void shouldCallUpdateWaCourtLocationsServiceWhenPresent_AndMintiEnabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("123456").build())
                .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        verify(updateWaCourtLocationsService).updateCourtListingWALocations(any(), any());

        assertThat(responseCaseData.getCaseManagementLocation().getBaseLocation()).isEqualTo("123456");
        assertThat(responseCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo(CREATE_SDO.name());
        assertThat(responseCaseData.getBusinessProcess().getStatus()).hasToString("READY");
    }

    @Nested
    class MidEventDisposalHearingLocationRefDataCallback extends LocationRefSampleDataBuilder {

        @Test
        void shouldPrePopulateDisposalHearingPage() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "Site 1 - Adr 1 - AAA 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        /**
         * spec claim, but no preferred court location.
         */
        @Test
        void shouldPrePopulateDisposalHearingPageSpec1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                .toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .build();
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "Site 1 - Adr 1 - AAA 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        /**
         * spec claim, specified no preference for court.
         */
        @Test
        void shouldPrePopulateDisposalHearingPageSpec2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                .toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQRequestedCourt(
                                      RequestedCourt.builder()
                                          .build()
                                  )
                                  .build())
                .build();
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "Site 1 - Adr 1 - AAA 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        /**
         * spec claim, preferred court specified.
         */
        @Test
        void shouldPrePopulateDisposalHearingPageSpec3() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build()
                .toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQRequestedCourt(
                                      RequestedCourt.builder()
                                          .responseCourtCode("court3")
                                          .caseLocation(
                                              CaseLocationCivil.builder()
                                                  .baseLocation("dummy base")
                                                  .region("dummy region")
                                                  .build()
                                          ).build()
                                  ).build()
                ).build();

            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "Site 1 - Adr 1 - AAA 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
            assertThat(dynamicList.getValue().getLabel()).isEqualTo("Site 3 - Adr 3 - CCC 333");
        }
    }

    @ParameterizedTest
    @MethodSource("testDataUnspec")
    void whenClaimUnspecAndJudgeSelects_changeTrackOrMaintainAllocatedTrack(CaseData caseData, AllocatedTrack expectedAllocatedTrack) {
        // When judge selects a different track to which the claim is currently on, update allocatedTrack to match selection
        // or maintain allocatedTrack if selection already corresponds with selection made.
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData()).containsEntry("allocatedTrack", expectedAllocatedTrack.name());
    }

    static Stream<Arguments> testDataUnspec() {
        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .fastTrackMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(NO)
                    .claimsTrack(ClaimsTrack.fastTrack)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                AllocatedTrack.FAST_CLAIM
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .fastTrackMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .build(),
                AllocatedTrack.FAST_CLAIM
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                    .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .orderType(OrderType.DISPOSAL)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                AllocatedTrack.FAST_CLAIM
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                    .smallClaimsMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(NO)
                    .claimsTrack(ClaimsTrack.smallClaimsTrack)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                AllocatedTrack.SMALL_CLAIM
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                    .smallClaimsMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(YES)
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                AllocatedTrack.SMALL_CLAIM
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(UNSPEC_CLAIM)
                    .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                    .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .orderType(OrderType.DISPOSAL)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                AllocatedTrack.SMALL_CLAIM
            )
        );
    }

    @ParameterizedTest
    @MethodSource("testDataSpec")
    void whenClaimSpecAndJudgeSelects_changeTrackOrMaintainClaimResponseTrack(CaseData caseData, String expectedClaimResponseTrack) {
        // When judge selects a different track to which the claim is currently on, update ClaimResponseTrack to match selection
        // or maintain ClaimResponseTrack if selection already corresponds with selection made.
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData()).containsEntry("responseClaimTrack", expectedClaimResponseTrack);
    }

    static Stream<Arguments> testDataSpec() {
        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack("SMALL_CLAIM")
                    .fastTrackMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(NO)
                    .claimsTrack(ClaimsTrack.fastTrack)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                "FAST_CLAIM"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack("SMALL_CLAIM")
                    .fastTrackMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                "FAST_CLAIM"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack("SMALL_CLAIM")
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .build(),
                "FAST_CLAIM"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack("FAST_CLAIM")
                    .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .orderType(OrderType.DISPOSAL)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                "FAST_CLAIM"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack("FAST_CLAIM")
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .orderType(OrderType.DISPOSAL)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                "FAST_CLAIM"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack("FAST_CLAIM")
                    .smallClaimsMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(NO)
                    .claimsTrack(ClaimsTrack.smallClaimsTrack)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                "SMALL_CLAIM"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack("FAST_CLAIM")
                    .smallClaimsMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(YES)
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                "SMALL_CLAIM"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack("FAST_CLAIM")
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(YES)
                    .orderType(OrderType.DECIDE_DAMAGES)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .build(),
                "SMALL_CLAIM"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                    .caseAccessCategory(SPEC_CLAIM)
                    .responseClaimTrack("SMALL_CLAIM")
                    .disposalHearingMethodInPerson(options.toBuilder().value(selectedCourt).build())
                    .drawDirectionsOrderRequired(YES)
                    .drawDirectionsOrderSmallClaims(NO)
                    .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                    .orderType(OrderType.DISPOSAL)
                    .build(),
                "SMALL_CLAIM"
            )
        );
    }

    @Nested
    class MidEventPrePopulateOrderDetailsPagesCallback extends LocationRefSampleDataBuilder {
        private LocalDate newDate;
        private LocalDate nextWorkingDayDate;
        private LocalDateTime localDateTime;

        @BeforeEach
        void setup() {
            newDate = LocalDate.of(2020, 1, 15);
            nextWorkingDayDate = LocalDate.of(2023, 12, 15);
            localDateTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            when(time.now()).thenReturn(localDateTime);
            when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(nextWorkingDayDate);
            when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), anyInt())).thenReturn(newDate);
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class))).thenReturn(
                newDate);
        }

        private final LocalDate date = LocalDate.of(2020, 1, 15);

        @Test
        void shouldPrePopulateOrderDetailsPagesCarmNotEnabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(15000))
                .applicant1DQWithLocation().build();
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(false);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotHaveToString("smallClaimsMediationSectionToggle");

            assertThat(response.getData()).doesNotHaveToString("smallClaimsMediationSectionStatement");

            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("No");
        }

        @Test
        void shouldPrePopulateOrderDetailsPagesWithSmallClaimFlightDelay() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(15000))
                .applicant1DQWithLocation().build();
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("smallClaimsFlightDelay").extracting("relatedClaimsInput")
                .isEqualTo("In the event that the Claimant(s) or Defendant(s) are aware if other \n"
                               + "claims relating to the same flight they must notify the court \n"
                               + "where the claim is being managed within 14 days of receipt of \n"
                               + "this Order providing all relevant details of those claims including \n"
                               + "case number(s), hearing date(s) and copy final substantive order(s) \n"
                               + "if any, to assist the Court with ongoing case management which may \n"
                               + "include the cases being heard together.");
            assertThat(response.getData()).extracting("smallClaimsFlightDelay").extracting("legalDocumentsInput")
                .isEqualTo("Any arguments as to the law to be applied to this claim, together with \n"
                               + "copies of legal authorities or precedents relied on, shall be uploaded \n"
                               + "to the Digital Portal not later than 3 full working days before the \n"
                               + "final hearing date.");

        }

        @Test
        void shouldPrePopulateOrderDetailsPagesWithUpdatedExpertEvidenceDataForR2() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(15000))
                .applicant1DQWithLocation().build();
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input1")
                .isEqualTo(
                    "The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                        + " Digital Portal with the particulars of claim");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").doesNotHaveToString("date1");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input2")
                .isEqualTo("The Defendant(s) may ask questions of the Claimant's " +
                               "expert which must be sent to the expert directly and uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date2")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input3")
                .isEqualTo("The answers to the questions shall be answered by the Expert by");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date3")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("input4")
                .isEqualTo("and uploaded to the Digital Portal by the party who has asked the question by");
            assertThat(response.getData()).extracting("fastTrackPersonalInjury").extracting("date4")
                .isEqualTo(nextWorkingDayDate.toString());

        }

        @Test
        void shouldPrePopulateDRHFields() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                    .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
                LocationRefData.builder().epimmsId(preSelectedCourt).courtLocationCode(preSelectedCourt)
                    .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
                LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                    .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);

            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            DynamicList expected = DynamicList.builder()
                .listItems(List.of(
                               DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                               DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build(),
                               DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                           )
                )
                .build();
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("Yes");

            assertThat(data.getSdoR2SmallClaimsJudgesRecital().getInput()).isEqualTo(SdoR2UiConstantSmallClaim.JUDGE_RECITAL);
            assertThat(data.getSdoR2SmallClaimsPPI().getPpiDate()).isEqualTo(LocalDate.now().plusDays(21));
            assertThat(data.getSdoR2SmallClaimsPPI().getText()).isEqualTo(SdoR2UiConstantSmallClaim.PPI_DESCRIPTION);
            assertThat(data.getSdoR2SmallClaimsUploadDoc().getSdoUploadOfDocumentsTxt()).isEqualTo(
                SdoR2UiConstantSmallClaim.UPLOAD_DOC_DESCRIPTION);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getText()).isEqualTo(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getSdoStatementOfWitness()).isEqualTo(
                SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getIsRestrictWitness()).isEqualTo(NO);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getIsRestrictPages()).isEqualTo(NO);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness()
                           .getPartyIsCountedAsWitnessTxt()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements()
                           .getSdoR2SmallClaimsRestrictPages().getFontDetails()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements()
                           .getSdoR2SmallClaimsRestrictPages().getWitnessShouldNotMoreThanTxt()).isEqualTo(
                SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1);
            assertThat(data.getSdoR2SmallClaimsWitnessStatements()
                           .getSdoR2SmallClaimsRestrictPages().getNoOfPages()).isEqualTo(12);
            assertThat(data.getSdoR2SmallClaimsHearing().getTrialOnOptions()).isEqualTo(HearingOnRadioOptions.OPEN_DATE);
            DynamicList hearingMethodValuesDRH = data.getSdoR2SmallClaimsHearing().getMethodOfHearing();
            List<String> hearingMethodValuesDRHActual = hearingMethodValuesDRH.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .toList();
            assertThat(hearingMethodValuesDRHActual).containsOnly(HearingMethod.IN_PERSON.getLabel());
            assertThat(data.getSdoR2SmallClaimsHearing().getLengthList()).isEqualTo(SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES);
            assertThat(data.getSdoR2SmallClaimsHearing().getPhysicalBundleOptions()).isEqualTo(
                SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY);
            assertThat(data.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingFirstOpenDateAfter().getListFrom()).isEqualTo(
                LocalDate.now().plusDays(56));
            assertThat(data.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getListFrom()).isEqualTo(
                LocalDate.now().plusDays(56));
            assertThat(data.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getDateTo()).isEqualTo(
                LocalDate.now().plusDays(70));
            assertThat(data.getSdoR2SmallClaimsHearing().getAltHearingCourtLocationList()).isEqualTo(expected);
            assertThat(data.getSdoR2SmallClaimsHearing().getHearingCourtLocationList().getValue().getCode()).isEqualTo(
                preSelectedCourt);
            assertThat(data.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsBundleOfDocs().getPhysicalBundlePartyTxt()).isEqualTo(
                SdoR2UiConstantSmallClaim.BUNDLE_TEXT);
            assertThat(data.getSdoR2SmallClaimsImpNotes().getText()).isEqualTo(SdoR2UiConstantSmallClaim.IMP_NOTES_TEXT);
            assertThat(data.getSdoR2SmallClaimsImpNotes().getDate()).isEqualTo(LocalDate.now().plusDays(7));
            assertThat(data.getSdoR2SmallClaimsMediationSectionStatement().getInput()).isEqualTo(
                SdoR2UiConstantSmallClaim.CARM_MEDIATION_TEXT);
        }

        @Test
        void shouldPrePopulateDRHFields_CarmNotEnabled() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();

            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotHaveToString("sdoR2SmallClaimsMediationSectionStatement");

            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("No");
        }

        @Test
        void shouldPrePopulateOrderDetailsPagesWithUpdatedDisclosureOfDocumentDataForR2() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(15000))
                .applicant1DQWithLocation().build();
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input1")
                .isEqualTo("Standard disclosure shall be provided by the parties by uploading to the Digital "
                               + "Portal their list of documents by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date1")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input2")
                .isEqualTo("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                               + "the other party by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date2")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input3")
                .isEqualTo("Requests will be complied with within 7 days of the receipt of the request.");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("input4")
                .isEqualTo("Each party must upload to the Digital Portal copies of those documents on which they "
                               + "wish to rely at trial by 4pm on");
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocuments").extracting("date3")
                .isEqualTo(nextWorkingDayDate.toString());

        }

        @Test
        void shouldPrePopulateToggleDRHFields_CarmEnabled() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                    .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
                LocationRefData.builder().epimmsId(preSelectedCourt).courtLocationCode(preSelectedCourt)
                    .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
                LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                    .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);

            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(data.getSdoR2SmallClaimsUploadDocToggle()).isEqualTo(Collections.singletonList(
                IncludeInOrderToggle.INCLUDE));
            assertThat(data.getSdoR2SmallClaimsHearingToggle()).isEqualTo(Collections.singletonList(IncludeInOrderToggle.INCLUDE));
            assertThat(data.getSdoR2SmallClaimsWitnessStatementsToggle()).isEqualTo(Collections.singletonList(
                IncludeInOrderToggle.INCLUDE));
            assertThat(data.getSdoR2SmallClaimsPPIToggle()).isNull();
            assertThat(response.getData()).extracting("sdoR2SmallClaimsMediationSectionToggle").isNotNull();
        }

        @Test
        void testSDOSortsLocationListThroughOrganisationPartyType() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .respondent1DQWithLocation().applicant1DQWithLocation().applicant1(Party.builder()
                                                                                       .type(Party.Type.ORGANISATION)
                                                                                       .individualTitle("Mr.")
                                                                                       .individualFirstName("Alex")
                                                                                       .individualLastName("Richards")
                                                                                       .partyName("Mr. Alex Richards")
                                                                                       .build()).build();
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "A Site 3 - Adr 3 - AAA 111",
                "Site 1 - Adr 1 - VVV 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        @Test
        void testSDOSortsLocationListThroughDecideDamagesOrderType() {
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());
            CaseData caseData = CaseDataBuilder.builder().respondent1DQWithLocation().applicant1DQWithLocation()
                .setClaimTypeToSpecClaim().atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .build().toBuilder().orderType(OrderType.DECIDE_DAMAGES).applicant1(Party.builder()
                                                                                        .type(Party.Type.ORGANISATION)
                                                                                        .individualTitle("Mr.")
                                                                                        .individualFirstName("Alex")
                                                                                        .individualLastName("Richards")
                                                                                        .partyName("Mr. Alex Richards")
                                                                                        .build()).build();

            // .respondent1DQWithLocation().applicant1DQWithLocation()

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            DynamicList dynamicList = getLocationDynamicListInPersonHearing(data);

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "A Site 3 - Adr 3 - AAA 111",
                "Site 1 - Adr 1 - VVV 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333"
            );
        }

        @Test
        void shouldPrePopulateDisposalHearingJudgementDeductionValueWhenDrawDirectionsOrderIsNotNull() {
            JudgementSum tempJudgementSum = JudgementSum.builder()
                .judgementSum(12.0)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrder(tempJudgementSum)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("disposalHearingJudgementDeductionValue").extracting("value")
                .isEqualTo("12.0%");
            assertThat(response.getData()).extracting("fastTrackJudgementDeductionValue").extracting("value")
                .isEqualTo("12.0%");
            assertThat(response.getData()).extracting("smallClaimsJudgementDeductionValue").extracting("value")
                .isEqualTo("12.0%");
        }
    }

    @Nested
    class MidEventSetOrderDetailsFlags {
        private static final String PAGE_ID = "order-details-navigation";

        @Test
        void smallClaimsFlagAndFastTrackFlagSetToNo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void smallClaimsFlagSetToYesPathOne() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void smallClaimsFlagSetToYesPathTwo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.YES)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void fastTrackFlagSetToYesPathOne() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(List.of(FastTrack.fastClaimBuildingDispute))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        }

        @Test
        void fastTrackFlagSetToYesPathTwo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
                .orderType(OrderType.DECIDE_DAMAGES)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(List.of(FastTrack.fastClaimBuildingDispute))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        }

        @Test
        void fastTRackSdoR2NihlPathTwo() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimBuildingDispute);
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.NO)
                .orderType(OrderType.DECIDE_DAMAGES)
                .claimsTrack(ClaimsTrack.fastTrack)
                .trialAdditionalDirectionsForFastTrack(fastTrackList)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
        }

        @Test
        void fastTrackFlagSetToYesNihlPathOne() {

            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimBuildingDispute);
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(fastTrackList)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
        }

        @Test
        void smallClaimsSdoR2FlagSetToYesPathOne() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
        }

        @Test
        void smallClaimsSdoR2FlagSetToYesPathTwo() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.YES)
                .drawDirectionsOrderSmallClaims(YesOrNo.YES)
                .drawDirectionsOrderSmallClaimsAdditionalDirections(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
        }
    }

    @Nested
    class MidEventGenerateSdoOrderCallback {
        private static final String PAGE_ID = "generate-sdo-order";

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedDisposalHearingSDOInPersonHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedDisposalHearingSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOTelephoneHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedDisposalHearingSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOVideoHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedFastTrackSDOInPersonHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedFastTrackSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedFastTrackSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedFastTrackSDOTelephoneHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedFastTrackSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedFastTrackSDOVideoHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedSmallClaimsSDOInPersonHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedSmallClaimsSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedSmallClaimsSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedSmallClaimsSDOTelephoneHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedSmallClaimsSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedSmallClaimsSDOVideoHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldAssignCategoryId_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSdoOrderDocument().getDocumentLink().getCategoryID()).isEqualTo(
                "caseManagementOrders");
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenNihl() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(YesOrNo.NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(fastTrackList)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSdoOrderDocument().getDocumentLink().getCategoryID()).isEqualTo(
                "caseManagementOrders");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldValidateFieldsForNihl(boolean valid) {

            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

            List<FastTrack> fastTrackList = new ArrayList<FastTrack>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            LocalDate testDate = valid ? LocalDate.now().plusDays(1) : LocalDate.now();
            Integer testWitnesses = valid ? 0 : -1;

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .drawDirectionsOrderRequired(NO)
                .claimsTrack(ClaimsTrack.fastTrack)
                .fastClaims(fastTrackList)
                .sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder().standardDisclosureDate(testDate).inspectionDate(
                    testDate).build())
                .sdoR2AddendumReport(SdoR2AddendumReport.builder().sdoAddendumReportDate(testDate).build())
                .sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder().sdoClaimantShallUndergoDate(testDate).sdoServiceReportDate(
                    testDate).build())
                .sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder().sdoDefendantMayAskDate(testDate).sdoQuestionsShallBeAnsweredDate(
                        testDate)
                                                  .sdoApplicationToRelyOnFurther(SdoR2ApplicationToRelyOnFurther.builder().applicationToRelyOnFurtherDetails(
                                                      SdoR2ApplicationToRelyOnFurtherDetails.builder().applicationToRelyDetailsDate(
                                                          testDate).build()).build()).build())
                .sdoR2PermissionToRelyOnExpert(SdoR2PermissionToRelyOnExpert.builder().sdoPermissionToRelyOnExpertDate(
                    testDate).sdoJointMeetingOfExpertsDate(testDate).build())
                .sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder().sdoInstructionOfTheExpertDate(
                        testDate).sdoExpertReportDate(testDate)
                                                   .sdoWrittenQuestionsDate(testDate).sdoRepliesDate(testDate).build())
                .sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder().sdoQuestionsShallBeAnsweredDate(testDate).sdoWrittenQuestionsDate(
                    testDate).build())
                .sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder().sdoR2ScheduleOfLossClaimantDate(testDate).sdoR2ScheduleOfLossDefendantDate(
                    testDate).build())
                .sdoR2Trial(SdoR2Trial.builder().sdoR2TrialFirstOpenDateAfter(SdoR2TrialFirstOpenDateAfter.builder().listFrom(
                        testDate).build())
                                .sdoR2TrialWindow(SdoR2TrialWindow.builder().listFrom(testDate).dateTo(testDate).build()).build())
                .sdoR2ImportantNotesDate(testDate)
                .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder().sdoWitnessDeadlineDate(testDate)
                                          .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                                                    .restrictNoOfWitnessDetails(
                                                                        SdoR2RestrictNoOfWitnessDetails.builder().noOfWitnessClaimant(
                                                                                testWitnesses)
                                                                            .noOfWitnessDefendant(testWitnesses).build()).build()).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (valid) {
                assertThat(response.getErrors()).size().isZero();
            } else {
                assertThat(response.getErrors()).size().isEqualTo(25);
                assertThat(response.getErrors()).contains(ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE);
                assertThat(response.getErrors()).contains(ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
            }
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenDrhIsSelected() {
            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = CaseDocument.builder().documentLink(
                    Document.builder().documentUrl("url").build())
                .build();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER_SDO,
                REFERENCE_NUMBER
            );

            String body = format(
                CONFIRMATION_SUMMARY_1_V_1,
                "Mr. John Rambo",
                "Mr. Sole Trader"
            ) + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_1v2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER_SDO,
                REFERENCE_NUMBER
            );

            String body = format(
                CONFIRMATION_SUMMARY_1_V_2,
                "Mr. John Rambo",
                "Mr. Sole Trader",
                "Mr. John Rambo"
            ) + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_2v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoApplicants()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = format(
                CONFIRMATION_HEADER_SDO,
                REFERENCE_NUMBER
            );

            String body = format(
                CONFIRMATION_SUMMARY_2_V_1,
                "Mr. John Rambo",
                "Mr. Jason Rambo",
                "Mr. Sole Trader"
            ) + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(header)
                    .confirmationBody(body)
                    .build());
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_SDO);
    }

    @Nested
    class MidEventNegativeNumberOfWitness {
        private static final String PAGE_ID = "generate-sdo-order";

        @Test
        void shouldThrowErrorWhenEnteringNegativeNumberOfWitnessSmallClaim() {

            CaseData caseData = CaseDataBuilder.builder()
                .atSmallClaimsWitnessStatementWithNegativeInputs()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("The number entered cannot be less than zero");
        }

        @Test
        void shouldThrowErrorWhenEnteringNegativeNumberOfWitnessFastTrack() {

            CaseData caseData = CaseDataBuilder.builder()
                .atFastTrackWitnessOfFactWithNegativeInputs()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.fastTrack)
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("The number entered cannot be less than zero");
        }

        @Test
        void shouldNotThrowErrorWhenEnteringPositiveNumberOfWitnessSmallClaim() {

            CaseData caseData = CaseDataBuilder.builder()
                .atSmallClaimsWitnessStatementWithPositiveInputs()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotThrowErrorWhenEnteringPositiveNumberOfWitnessFastTrack() {

            CaseData caseData = CaseDataBuilder.builder()
                .atFastTrackWitnessOfFactWithPositiveInputs()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.fastTrack)
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();

        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldValidateDRHFields(boolean valid) {
            LocalDate testDate = valid ? LocalDate.now().plusDays(1) : LocalDate.now().minusDays(2);
            int countWitness = valid ? 0 : -1;
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsPPI(SdoR2SmallClaimsPPI.builder().ppiDate(testDate).build())
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder().trialOnOptions(HearingOnRadioOptions.OPEN_DATE)
                                             .sdoR2SmallClaimsHearingFirstOpenDateAfter(
                                                 SdoR2SmallClaimsHearingFirstOpenDateAfter.builder().listFrom(testDate).build()).build())
                .sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder().date(testDate).build())
                .sdoR2SmallClaimsWitnessStatements(SdoR2SmallClaimsWitnessStatements.builder()
                                                       .isRestrictWitness(YES)
                                                       .sdoR2SmallClaimsRestrictWitness(SdoR2SmallClaimsRestrictWitness
                                                                                            .builder().noOfWitnessClaimant(
                                                               countWitness)
                                                                                            .noOfWitnessDefendant(
                                                                                                countWitness).build())
                                                       .build())

                .build();

            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            if (valid) {
                assertThat(response.getErrors()).isEmpty();
            } else {
                assertThat(response.getErrors()).hasSize(5);
            }
        }

        @Test
        void shouldNotThrowErrorIfDRHOptionalFieldsAreNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder().date(LocalDate.now().plusDays(2)).build())
                .build();

            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldThrowValidationErrorForDRHHearingWindow(boolean valid) {
            LocalDate testDate = valid ? LocalDate.now().plusDays(1) : LocalDate.now().minusDays(2);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO)
                .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
                .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder().trialOnOptions(HearingOnRadioOptions.HEARING_WINDOW)
                                             .sdoR2SmallClaimsHearingWindow(
                                                 SdoR2SmallClaimsHearingWindow.builder().listFrom(testDate)
                                                     .dateTo(testDate).build()).build())
                .build();

            when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            if (valid) {
                assertThat(response.getErrors()).isEmpty();
            } else {
                assertThat(response.getErrors()).hasSize(2);
            }
        }
    }
}

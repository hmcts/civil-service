package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.pipeline.DirectionsOrderCallbackPipeline;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoOrderDetailsTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoPrePopulateTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoConfirmationTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoDocumentTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoSubmissionTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoValidationTask;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SDOHearingNotes;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
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
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDocumentService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoOrderDetailsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoPrePopulateService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNarrativeService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSubmissionService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoValidationService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
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
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate.FIVE_HOURS;
import static uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions.OPEN_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.ERROR_MINTI_DISPOSAL_NOT_ALLOWED;

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
    SdoLocationService.class,
    SdoCaseClassificationService.class,
    SdoFeatureToggleService.class,
    SdoOrderDetailsService.class,
    SdoPrePopulateService.class,
    SdoNarrativeService.class,
    SdoValidationService.class,
    SdoDocumentService.class,
    SdoSubmissionService.class,
    DirectionsOrderCallbackPipeline.class,
    SdoPrePopulateTask.class,
    SdoOrderDetailsTask.class,
    SdoValidationTask.class,
    SdoDocumentTask.class,
    SdoSubmissionTask.class,
    SdoConfirmationTask.class},
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
    private ObjectMapper objectMapper;

    @Autowired
    private DirectionsOrderCallbackPipeline directionsOrderCallbackPipeline;

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

    @MockBean
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    @Value("${court-location.unspecified-claim.epimms-id}")
    private String ccmccEpimsId;

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
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
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
        void shouldPopulateLocationListsWithPreselectedCourtAndEnableWelshFlagWithClaimantLanguagePreference() {
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
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .claimantBilingualLanguagePreference("BOTH")
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
            assertThat(responseCaseData.getBilingualHint()).isEqualTo(YES);
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourtAndEnableWelshFlagWithRespondentLanguagePreference() {
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
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .caseDataLip(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage("BOTH").build()).build())
                .atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .claimantBilingualLanguagePreference("ENGLISH")
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
            assertThat(responseCaseData.getBilingualHint()).isEqualTo(YES);
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourtAndEnableWelshFlagWithNoClaimantAndRespondentLanguagePreference() {
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
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .caseDataLip(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage("BOTH").build()).build())
                .atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .claimantBilingualLanguagePreference("BOTH")
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
            assertThat(responseCaseData.getBilingualHint()).isEqualTo(YES);
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourtAndEnableWelshFlagWithClaimantAndRespondentLanguagePreference() {
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
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .caseDataLip(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage("ENGLISH").build()).build())
                .atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .claimantBilingualLanguagePreference("ENGLISH")
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
            assertThat(responseCaseData.getBilingualHint()).isNull();
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

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);
            LocalDate today = LocalDate.now();

            FastTrackJudgesRecital judgesRecital = data.getSdoFastTrackJudgesRecital();
            assertThat(judgesRecital).isNotNull();
            assertThat(judgesRecital.getInput()).isEqualTo(JUDGE_RECITAL);

            SdoR2DisclosureOfDocuments expectedDisclosure = SdoR2DisclosureOfDocuments.builder()
                .standardDisclosureTxt(STANDARD_DISCLOSURE)
                .standardDisclosureDate(today.plusDays(28))
                .inspectionTxt(INSPECTION)
                .inspectionDate(today.plusDays(42))
                .requestsWillBeCompiledLabel(REQUEST_COMPILED_WITH)
                .build();
            assertThat(data.getSdoR2DisclosureOfDocuments()).isEqualTo(expectedDisclosure);

            SdoR2WitnessOfFact expectedWitnesses = SdoR2WitnessOfFact.builder()
                .sdoStatementOfWitness(STATEMENT_WITNESS)
                .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                    .isRestrictWitness(YesOrNo.NO)
                    .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                        .noOfWitnessClaimant(3)
                        .noOfWitnessDefendant(3)
                        .partyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT)
                        .build())
                    .build())
                .sdoRestrictPages(SdoR2RestrictPages.builder()
                    .isRestrictPages(YesOrNo.NO)
                    .restrictNoOfPagesDetails(SdoR2RestrictNoOfPagesDetails.builder()
                        .witnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1)
                        .noOfPages(12)
                        .fontDetails(RESTRICT_NUMBER_PAGES_TEXT2)
                        .build())
                    .build())
                .sdoWitnessDeadline(DEADLINE)
                .sdoWitnessDeadlineDate(today.plusDays(70))
                .sdoWitnessDeadlineText(DEADLINE_EVIDENCE)
                .build();
            assertThat(data.getSdoR2WitnessesOfFact()).isEqualTo(expectedWitnesses);

            SdoR2ScheduleOfLoss expectedSchedule = SdoR2ScheduleOfLoss.builder()
                .sdoR2ScheduleOfLossClaimantText(SCHEDULE_OF_LOSS_CLAIMANT)
                .sdoR2ScheduleOfLossClaimantDate(today.plusDays(364))
                .sdoR2ScheduleOfLossDefendantText(SCHEDULE_OF_LOSS_DEFENDANT)
                .sdoR2ScheduleOfLossDefendantDate(today.plusDays(378))
                .isClaimForPecuniaryLoss(YesOrNo.NO)
                .sdoR2ScheduleOfLossPecuniaryLossTxt(PECUNIARY_LOSS)
                .build();
            assertThat(data.getSdoR2ScheduleOfLoss()).isEqualTo(expectedSchedule);

            SdoR2Trial trial = data.getSdoR2Trial();
            assertThat(trial).isNotNull();
            assertThat(trial.getTrialOnOptions()).isEqualTo(TrialOnRadioOptions.OPEN_DATE);
            assertThat(trial.getLengthList()).isEqualTo(FastTrackHearingTimeEstimate.FIVE_HOURS);

            DynamicList methodOfHearing = trial.getMethodOfHearing();
            assertThat(methodOfHearing).isNotNull();
            assertThat(methodOfHearing.getValue()).isNotNull();
            assertThat(methodOfHearing.getValue().getLabel()).isEqualTo(HearingMethod.IN_PERSON.getLabel());

            DynamicList expectedHearingCourtList = DynamicList.builder()
                .value(DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build())
                .listItems(List.of(
                    DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build(),
                    DynamicListElement.builder().code("OTHER_LOCATION").label("Other location").build()
                ))
                .build();
            assertThat(trial.getHearingCourtLocationList()).isEqualTo(expectedHearingCourtList);

            List<DynamicListElement> expectedAltCourtItems = List.of(
                DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                DynamicListElement.builder().code(preSelectedCourt).label("court 2 - 2 address - Y02 7RB").build(),
                DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
            );
            assertThat(trial.getAltHearingCourtLocationList().getListItems()).isEqualTo(expectedAltCourtItems);
            assertThat(trial.getAltHearingCourtLocationList().getValue()).isNull();

            assertThat(trial.getPhysicalBundleOptions()).isEqualTo(PhysicalTrialBundleOptions.PARTY);
            assertThat(trial.getPhysicalBundlePartyTxt()).isEqualTo(PHYSICAL_TRIAL_BUNDLE);
            assertThat(trial.getSdoR2TrialFirstOpenDateAfter().getListFrom()).isEqualTo(today.plusDays(434));
            assertThat(trial.getSdoR2TrialWindow().getListFrom()).isEqualTo(today.plusDays(434));
            assertThat(trial.getSdoR2TrialWindow().getDateTo()).isEqualTo(today.plusDays(455));

            assertThat(data.getSdoR2ImportantNotesTxt()).isEqualTo(IMPORTANT_NOTES);
            assertThat(data.getSdoR2ImportantNotesDate()).isEqualTo(today.plusDays(7));

            assertThat(data.getSdoR2ExpertEvidence()).isEqualTo(SdoR2ExpertEvidence.builder()
                .sdoClaimantPermissionToRelyTxt(CLAIMANT_PERMISSION_TO_RELY)
                .build());

            assertThat(data.getSdoR2AddendumReport()).isEqualTo(SdoR2AddendumReport.builder()
                .sdoAddendumReportTxt(ADDENDUM_REPORT)
                .sdoAddendumReportDate(today.plusDays(56))
                .build());

            assertThat(data.getSdoR2FurtherAudiogram()).isEqualTo(SdoR2FurtherAudiogram.builder()
                .sdoClaimantShallUndergoTxt(CLAIMANT_SHALL_UNDERGO)
                .sdoClaimantShallUndergoDate(today.plusDays(42))
                .sdoServiceReportTxt(SERVICE_REPORT)
                .sdoServiceReportDate(today.plusDays(98))
                .build());

            SdoR2QuestionsClaimantExpert expectedQuestionsClaimant = SdoR2QuestionsClaimantExpert.builder()
                .sdoDefendantMayAskTxt(DEFENDANT_MAY_ASK)
                .sdoDefendantMayAskDate(today.plusDays(126))
                .sdoQuestionsShallBeAnsweredTxt(QUESTIONS_SHALL_BE_ANSWERED)
                .sdoQuestionsShallBeAnsweredDate(today.plusDays(147))
                .sdoUploadedToDigitalPortalTxt(UPLOADED_TO_DIGITAL_PORTAL)
                .sdoApplicationToRelyOnFurther(SdoR2ApplicationToRelyOnFurther.builder()
                    .doRequireApplicationToRely(YesOrNo.NO)
                    .applicationToRelyOnFurtherDetails(SdoR2ApplicationToRelyOnFurtherDetails.builder()
                        .applicationToRelyDetailsTxt(APPLICATION_TO_RELY_DETAILS)
                        .applicationToRelyDetailsDate(today.plusDays(161))
                        .build())
                    .build())
                .build();
            assertThat(data.getSdoR2QuestionsClaimantExpert()).isEqualTo(expectedQuestionsClaimant);

            assertThat(data.getSdoR2PermissionToRelyOnExpert()).isEqualTo(SdoR2PermissionToRelyOnExpert.builder()
                .sdoPermissionToRelyOnExpertTxt(PERMISSION_TO_RELY_ON_EXPERT)
                .sdoPermissionToRelyOnExpertDate(today.plusDays(119))
                .sdoJointMeetingOfExpertsTxt(JOINT_MEETING_OF_EXPERTS)
                .sdoJointMeetingOfExpertsDate(today.plusDays(147))
                .sdoUploadedToDigitalPortalTxt(UPLOADED_TO_DIGITAL_PORTAL_7_DAYS)
                .build());

            assertThat(data.getSdoR2EvidenceAcousticEngineer()).isEqualTo(SdoR2EvidenceAcousticEngineer.builder()
                .sdoEvidenceAcousticEngineerTxt(EVIDENCE_ACOUSTIC_ENGINEER)
                .sdoInstructionOfTheExpertTxt(INSTRUCTION_OF_EXPERT)
                .sdoInstructionOfTheExpertDate(today.plusDays(42))
                .sdoInstructionOfTheExpertTxtArea(INSTRUCTION_OF_EXPERT_TA)
                .sdoExpertReportTxt(EXPERT_REPORT)
                .sdoExpertReportDate(today.plusDays(280))
                .sdoExpertReportDigitalPortalTxt(EXPERT_REPORT_DIGITAL_PORTAL)
                .sdoWrittenQuestionsTxt(WRITTEN_QUESTIONS)
                .sdoWrittenQuestionsDate(today.plusDays(294))
                .sdoWrittenQuestionsDigitalPortalTxt(WRITTEN_QUESTIONS_DIGITAL_PORTAL)
                .sdoRepliesTxt(REPLIES)
                .sdoRepliesDate(today.plusDays(315))
                .sdoRepliesDigitalPortalTxt(REPLIES_DIGITAL_PORTAL)
                .sdoServiceOfOrderTxt(SERVICE_OF_ORDER)
                .build());

            assertThat(data.getSdoR2QuestionsToEntExpert()).isEqualTo(SdoR2QuestionsToEntExpert.builder()
                .sdoWrittenQuestionsTxt(ENT_WRITTEN_QUESTIONS)
                .sdoWrittenQuestionsDate(today.plusDays(336))
                .sdoWrittenQuestionsDigPortalTxt(ENT_WRITTEN_QUESTIONS_DIG_PORTAL)
                .sdoQuestionsShallBeAnsweredTxt(ENT_QUESTIONS_SHALL_BE_ANSWERED)
                .sdoQuestionsShallBeAnsweredDate(today.plusDays(350))
                .sdoShallBeUploadedTxt(ENT_SHALL_BE_UPLOADED)
                .build());

            assertThat(data.getSdoR2UploadOfDocuments()).isEqualTo(SdoR2UploadOfDocuments.builder()
                .sdoUploadOfDocumentsTxt(UPLOAD_OF_DOCUMENTS)
                .build());

            List<IncludeInOrderToggle> includeToggle = List.of(IncludeInOrderToggle.INCLUDE);
            assertThat(data.getSdoAltDisputeResolution().getIncludeInOrderToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoVariationOfDirections().getIncludeInOrderToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2Settlement().getIncludeInOrderToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2DisclosureOfDocumentsToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2SeparatorWitnessesOfFactToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2SeparatorExpertEvidenceToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2SeparatorAddendumReportToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2SeparatorFurtherAudiogramToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2SeparatorQuestionsClaimantExpertToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2SeparatorPermissionToRelyOnExpertToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2SeparatorEvidenceAcousticEngineerToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2SeparatorQuestionsToEntExpertToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2ScheduleOfLossToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2SeparatorUploadOfDocumentsToggle()).isEqualTo(includeToggle);
            assertThat(data.getSdoR2TrialToggle()).isEqualTo(includeToggle);
        }



        @Test
        void shouldPrePopulateUpdatedWitnessSectionsForSDOR2() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();

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

        @Test
        void shouldPopulateWelshSectionForSDOR2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .claimsTrack(ClaimsTrack.smallClaimsTrack)
                .drawDirectionsOrderRequired(NO).build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getSdoR2FastTrackUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);
            assertThat(data.getSdoR2SmallClaimsUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);
            assertThat(data.getSdoR2DisposalHearingUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);
            assertThat(data.getSdoR2DrhUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);
            assertThat(data.getSdoR2NihlUseOfWelshLanguage().getDescription()).isEqualTo(WELSH_LANG_DESCRIPTION);

        }

        @Test
        void shouldUpdateCaseManagementLocation_whenUnder1000SpecCcmcc() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation(ccmccEpimsId).region(
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

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getCaseManagementLocation().getRegion()).isEqualTo("def court request region");
            assertThat(data.getCaseManagementLocation().getBaseLocation()).isEqualTo("def court requested epimm");
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

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getCaseManagementLocation().getRegion()).isEqualTo("orange");
            assertThat(data.getCaseManagementLocation().getBaseLocation()).isEqualTo("1010101");
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

    @Nested
    class AboutToSubmitCallbackWelshParty {

        private String userId;

        private static final String EMAIL = "example@email.com";
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            userId = UUID.randomUUID().toString();

            given(time.now()).willReturn(submittedDate);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        }

        @Test
        void shouldSaveDocumentToTempList_whenClaimantIsWelsh() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .sdoOrderDocument(CaseDocument.builder().documentLink(
                        Document.builder().documentUrl("url").build())
                                      .build())
                .claimantBilingualLanguagePreference("WELSH")
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .build().toBuilder()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("sdoOrderDocument");
            assertThat(response.getData()).doesNotContainKey("systemGeneratedCaseDocuments");
            assertThat(response.getData()).containsKey("preTranslationDocuments");
        }

        @Test
        void shouldSaveDocumentToTempList_whenDefendantIsWelsh() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
                .sdoOrderDocument(CaseDocument.builder().documentLink(
                        Document.builder().documentUrl("url").build())
                                      .build())
                .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .build().toBuilder()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("sdoOrderDocument");
            assertThat(response.getData()).doesNotContainKey("systemGeneratedCaseDocuments");
            assertThat(response.getData()).containsKey("preTranslationDocuments");
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
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            assertThat(responseCaseData.getEaCourtLocation()).isEqualTo(NO);
        } else {
            assertThat(responseCaseData.getEaCourtLocation()).isNull();
        }
    }

    @ParameterizedTest
    @CsvSource({
        //LR scenarios trigger and ignore hmcLipEnabled
        "true, YES, YES, true, YES",
        "false, YES, YES, true, YES",
        // LiP vs LR - ignore HMC court
        "true,  NO, YES, false, NO",
        "true,  NO, YES, TRUE, YES",
        "false,  NO, YES, true, NO",
        //LR vs LiP - ignore HMC court
        "true, YES, NO, true, YES",
        "false, YES, NO, true, NO",
        //LiP vs LiP - ignore HMC court
        "true, NO, NO, true, YES",
        "false, NO, NO, true, NO"
    })
    void shouldPopulateHmcLipEnabled_whenLiPAndHmcLipEnabled(boolean isCPAndWhitelisted, YesOrNo applicantRepresented,
                                                             YesOrNo respondent1Represented, boolean defendantNocOnline,
                                                             YesOrNo eaCourtLocation) {

        if (NO.equals(respondent1Represented) || NO.equals(applicantRepresented)) {
            when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(isCPAndWhitelisted);
        } else {
            when(featureToggleService.isLocationWhiteListedForCaseProgression(any())).thenReturn(isCPAndWhitelisted);
        }
        when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(defendantNocOnline);
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .region("2")
                                        .baseLocation("111")
                                        .build())
            .transferCourtLocationList(DynamicList.builder().value(DynamicListElement.builder()
                                                                       .label("Site 1 - Adr 1 - AAA 111").build()).build()).build();

        CallbackParams params = callbackParamsOf(caseData.toBuilder()
                                                     .applicant1Represented(applicantRepresented)
                                                     .respondent1Represented(respondent1Represented)
                                                     .build(), ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            assertEquals(eaCourtLocation, responseCaseData.getEaCourtLocation());
        } else {
            assertThat(responseCaseData.getEaCourtLocation()).isNull();
        }
    }

    @Test
    void shouldNotCallUpdateWaCourtLocationsServiceWhenNotPresent_AndMintiEnabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        handler = new CreateSDOCallbackHandler(
            objectMapper,
            featureToggleService,
            directionsOrderCallbackPipeline,
            Optional.empty()
        );

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("123456").build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        verifyNoInteractions(updateWaCourtLocationsService);
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
                "Site 3 - Adr 3 - CCC 333",
                "Site 5 - Adr 5 - YYY 111"
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
                "Site 3 - Adr 3 - CCC 333",
                "Site 5 - Adr 5 - YYY 111"
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
                "Site 3 - Adr 3 - CCC 333",
                "Site 5 - Adr 5 - YYY 111"
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
                                                  .baseLocation("00000")
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
                "Site 3 - Adr 3 - CCC 333",
                "Site 5 - Adr 5 - YYY 111"
            );
            assertThat(dynamicList.getValue().getLabel()).isEqualTo("Site 5 - Adr 5 - YYY 111");
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
        void shouldPrePopulateOrderDetailsPages() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(15000))
                .applicant1DQWithLocation()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").build())
                .build();
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());
            Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag(
                "Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            DynamicList dynamicList = objectMapper.convertValue(
                response.getData().get("disposalHearingMethodInPerson"),
                DynamicList.class
            );

            assertThat(dynamicList).isNotNull();
            assertThat(locationsFromDynamicList(dynamicList)).containsExactly(
                "A Site 3 - Adr 3 - AAA 111",
                "Site 1 - Adr 1 - VVV 111",
                "Site 2 - Adr 2 - BBB 222",
                "Site 3 - Adr 3 - CCC 333",
                "Site 5 - Adr 5 - YYY 111"
            );
            Optional<LocationRefData> shouldBeSelected = getSampleCourLocationsRefObjectToSort().stream()
                .filter(locationRefData -> locationRefData.getEpimmsId().equals(
                    caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().getCaseLocation().getBaseLocation()))
                .findFirst();
            assertThat(shouldBeSelected).isPresent();
            assertThat(dynamicList.getValue()).isNotNull()
                .extracting("label").isEqualTo(LocationReferenceDataService.getDisplayEntry(shouldBeSelected.get()));

            YesOrNo showCarmFields = toYesOrNo(response.getData().get("showCarmFields"));
            assertThat(showCarmFields).isEqualTo(YesOrNo.YES);

            List<OrderDetailsPagesSectionsToggle> expectedToggle = List.of(OrderDetailsPagesSectionsToggle.SHOW);
            List<OrderDetailsPagesSectionsToggle> fastTrackAdrToggle = objectMapper.convertValue(
                response.getData().get("fastTrackAltDisputeResolutionToggle"),
                new TypeReference<List<OrderDetailsPagesSectionsToggle>>() {}
            );
            List<OrderDetailsPagesSectionsToggle> disposalDocsToggle = objectMapper.convertValue(
                response.getData().get("disposalHearingDisclosureOfDocumentsToggle"),
                new TypeReference<List<OrderDetailsPagesSectionsToggle>>() {}
            );
            List<OrderDetailsPagesSectionsToggle> smallClaimsHearingToggle = objectMapper.convertValue(
                response.getData().get("smallClaimsHearingToggle"),
                new TypeReference<List<OrderDetailsPagesSectionsToggle>>() {}
            );

            assertThat(fastTrackAdrToggle).isEqualTo(expectedToggle);
            assertThat(disposalDocsToggle).isEqualTo(expectedToggle);
            assertThat(smallClaimsHearingToggle).isEqualTo(expectedToggle);

            DisposalHearingDisclosureOfDocuments disposalDocs = objectMapper.convertValue(
                response.getData().get("disposalHearingDisclosureOfDocuments"),
                DisposalHearingDisclosureOfDocuments.class
            );
            assertThat(disposalDocs.getInput1()).isEqualTo("The parties shall serve on each other copies of the documents upon which reliance is to be placed at the disposal hearing by 4pm on");
            assertThat(disposalDocs.getDate1()).isEqualTo(nextWorkingDayDate);

            FastTrackTrial fastTrackTrial = objectMapper.convertValue(
                response.getData().get("fastTrackTrial"),
                FastTrackTrial.class
            );
            assertThat(fastTrackTrial.getInput1()).isEqualTo("The time provisionally allowed for this trial is");
            assertThat(fastTrackTrial.getDate1()).isEqualTo(LocalDate.now().plusWeeks(22));

            FastTrackNotes fastTrackNotes = objectMapper.convertValue(
                response.getData().get("fastTrackNotes"),
                FastTrackNotes.class
            );
            assertThat(fastTrackNotes.getInput()).startsWith("This Order has been made without a hearing");
            assertThat(fastTrackNotes.getDate()).isEqualTo(nextWorkingDayDate);

            FastTrackBuildingDispute buildingDispute = objectMapper.convertValue(
                response.getData().get("fastTrackBuildingDispute"),
                FastTrackBuildingDispute.class
            );
            assertThat(buildingDispute.getInput1()).isEqualTo("The claimant must prepare a Scott Schedule of the defects, items of damage, or any other relevant matters");
            assertThat(buildingDispute.getDate1()).isEqualTo(nextWorkingDayDate);

            SdoR2FastTrackCreditHire creditHire = objectMapper.convertValue(
                response.getData().get("sdoR2FastTrackCreditHire"),
                SdoR2FastTrackCreditHire.class
            );
            assertThat(creditHire.getInput1()).contains("If impecuniosity is alleged by the claimant");
            assertThat(creditHire.getDate3()).isEqualTo(nextWorkingDayDate);

            FastTrackPersonalInjury personalInjury = objectMapper.convertValue(
                response.getData().get("fastTrackPersonalInjury"),
                FastTrackPersonalInjury.class
            );
            assertThat(personalInjury.getInput1()).startsWith("The Claimant has permission to rely upon the written expert evidence");

            FastTrackRoadTrafficAccident fastTrackRoadTrafficAccident = objectMapper.convertValue(
                response.getData().get("fastTrackRoadTrafficAccident"),
                FastTrackRoadTrafficAccident.class
            );
            assertThat(fastTrackRoadTrafficAccident.getInput()).startsWith("Photographs and/or a plan of the accident location");

            SmallClaimsHearing smallClaimsHearing = objectMapper.convertValue(
                response.getData().get("smallClaimsHearing"),
                SmallClaimsHearing.class
            );
            assertThat(smallClaimsHearing.getInput1()).startsWith("The hearing of the claim will be on a date to be notified");

            SmallClaimsDocuments smallClaimsDocuments = objectMapper.convertValue(
                response.getData().get("smallClaimsDocuments"),
                SmallClaimsDocuments.class
            );
            assertThat(smallClaimsDocuments.getInput1()).startsWith("Each party must upload to the Digital Portal");
        }

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

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getSmallClaimsMediationSectionToggle()).isNull();
            assertThat(data.getSmallClaimsMediationSectionStatement()).isNull();
            YesOrNo showCarmFields = toYesOrNo(response.getData().get("showCarmFields"));
            assertThat(showCarmFields).isEqualTo(YesOrNo.NO);
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

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            SmallClaimsFlightDelay flightDelay = objectMapper.convertValue(
                response.getData().get("smallClaimsFlightDelay"),
                SmallClaimsFlightDelay.class
            );

            assertThat(flightDelay).isNotNull();
            assertThat(flightDelay.getRelatedClaimsInput())
                .isEqualTo("In the event that the Claimant(s) or Defendant(s) are aware if other \n"
                               + "claims relating to the same flight they must notify the court \n"
                               + "where the claim is being managed within 14 days of receipt of \n"
                               + "this Order providing all relevant details of those claims including \n"
                               + "case number(s), hearing date(s) and copy final substantive order(s) \n"
                               + "if any, to assist the Court with ongoing case management which may \n"
                               + "include the cases being heard together.");
            assertThat(flightDelay.getLegalDocumentsInput())
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

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            FastTrackPersonalInjury personalInjury = objectMapper.convertValue(
                response.getData().get("fastTrackPersonalInjury"),
                FastTrackPersonalInjury.class
            );

            assertThat(personalInjury).isNotNull();
            assertThat(personalInjury.getInput1())
                .isEqualTo(
                    "The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                        + " Digital Portal with the particulars of claim");
            assertThat(personalInjury.getDate1()).isNull();
            assertThat(personalInjury.getInput2())
                .isEqualTo("The Defendant(s) may ask questions of the Claimant's " +
                               "expert which must be sent to the expert directly and uploaded to the Digital Portal by 4pm on");
            assertThat(personalInjury.getDate2()).isEqualTo(nextWorkingDayDate);
            assertThat(personalInjury.getInput3())
                .isEqualTo("The answers to the questions shall be answered by the Expert by");
            assertThat(personalInjury.getDate3()).isEqualTo(nextWorkingDayDate);
            assertThat(personalInjury.getInput4())
                .isEqualTo("and uploaded to the Digital Portal by the party who has asked the question by");
            assertThat(personalInjury.getDate4()).isEqualTo(nextWorkingDayDate);

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
            YesOrNo showCarmFields = toYesOrNo(response.getData().get("showCarmFields"));
            assertThat(showCarmFields).isEqualTo(YesOrNo.YES);

            SdoR2SmallClaimsJudgesRecital judgesRecital = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsJudgesRecital"),
                SdoR2SmallClaimsJudgesRecital.class
            );
            assertThat(judgesRecital.getInput()).isEqualTo(SdoR2UiConstantSmallClaim.JUDGE_RECITAL);

            SdoR2SmallClaimsPPI ppi = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsPPI"),
                SdoR2SmallClaimsPPI.class
            );
            assertThat(ppi.getPpiDate()).isEqualTo(LocalDate.now().plusDays(21));
            assertThat(ppi.getText()).isEqualTo(SdoR2UiConstantSmallClaim.PPI_DESCRIPTION);

            SdoR2SmallClaimsUploadDoc uploadDoc = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsUploadDoc"),
                SdoR2SmallClaimsUploadDoc.class
            );
            assertThat(uploadDoc.getSdoUploadOfDocumentsTxt()).isEqualTo(
                SdoR2UiConstantSmallClaim.UPLOAD_DOC_DESCRIPTION);

            SdoR2SmallClaimsWitnessStatements witnessStatements = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsWitnessStatements"),
                SdoR2SmallClaimsWitnessStatements.class
            );
            assertThat(witnessStatements.getText()).isEqualTo(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT);
            assertThat(witnessStatements.getSdoStatementOfWitness()).isEqualTo(
                SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT);
            assertThat(witnessStatements.getIsRestrictWitness()).isEqualTo(NO);
            assertThat(witnessStatements.getIsRestrictPages()).isEqualTo(NO);
            assertThat(witnessStatements.getSdoR2SmallClaimsRestrictWitness()
                           .getPartyIsCountedAsWitnessTxt()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT);
            assertThat(witnessStatements
                           .getSdoR2SmallClaimsRestrictPages().getFontDetails()).isEqualTo(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2);
            assertThat(witnessStatements
                           .getSdoR2SmallClaimsRestrictPages().getWitnessShouldNotMoreThanTxt()).isEqualTo(
                SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1);
            assertThat(witnessStatements
                           .getSdoR2SmallClaimsRestrictPages().getNoOfPages()).isEqualTo(12);

            SdoR2SmallClaimsHearing smallClaimsHearing = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsHearing"),
                SdoR2SmallClaimsHearing.class
            );
            assertThat(smallClaimsHearing.getTrialOnOptions()).isEqualTo(HearingOnRadioOptions.OPEN_DATE);
            DynamicList hearingMethodValuesDRH = smallClaimsHearing.getMethodOfHearing();
            List<String> hearingMethodValuesDRHActual = hearingMethodValuesDRH.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .toList();
            assertThat(hearingMethodValuesDRHActual).containsOnly(HearingMethod.IN_PERSON.getLabel());
            assertThat(smallClaimsHearing.getLengthList()).isEqualTo(SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES);
            assertThat(smallClaimsHearing.getPhysicalBundleOptions()).isEqualTo(
                SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY);
            assertThat(smallClaimsHearing.getSdoR2SmallClaimsHearingFirstOpenDateAfter().getListFrom()).isEqualTo(
                LocalDate.now().plusDays(56));
            assertThat(smallClaimsHearing.getSdoR2SmallClaimsHearingWindow().getListFrom()).isEqualTo(
                LocalDate.now().plusDays(56));
            assertThat(smallClaimsHearing.getSdoR2SmallClaimsHearingWindow().getDateTo()).isEqualTo(
                LocalDate.now().plusDays(70));
            assertThat(smallClaimsHearing.getAltHearingCourtLocationList()).isEqualTo(expected);
            assertThat(smallClaimsHearing.getHearingCourtLocationList().getValue().getCode()).isEqualTo(
                preSelectedCourt);
            assertThat(smallClaimsHearing.getSdoR2SmallClaimsBundleOfDocs().getPhysicalBundlePartyTxt()).isEqualTo(
                SdoR2UiConstantSmallClaim.BUNDLE_TEXT);
            SdoR2SmallClaimsImpNotes impNotes = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsImpNotes"),
                SdoR2SmallClaimsImpNotes.class
            );
            assertThat(impNotes.getText()).isEqualTo(SdoR2UiConstantSmallClaim.IMP_NOTES_TEXT);
            assertThat(impNotes.getDate()).isEqualTo(LocalDate.now().plusDays(7));
            SdoR2SmallClaimsMediation mediationStatement = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsMediationSectionStatement"),
                SdoR2SmallClaimsMediation.class
            );
            assertThat(mediationStatement.getInput()).isEqualTo(
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
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            SdoR2SmallClaimsMediation mediationStatement = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsMediationSectionStatement"),
                SdoR2SmallClaimsMediation.class
            );
            assertThat(mediationStatement).isNull();
            YesOrNo showCarmFields = toYesOrNo(response.getData().get("showCarmFields"));
            assertThat(showCarmFields).isEqualTo(YesOrNo.NO);
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

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            FastTrackDisclosureOfDocuments disclosure = objectMapper.convertValue(
                response.getData().get("fastTrackDisclosureOfDocuments"),
                FastTrackDisclosureOfDocuments.class
            );

            assertThat(disclosure).isNotNull();
            assertThat(disclosure.getInput1())
                .isEqualTo("Standard disclosure shall be provided by the parties by uploading to the Digital "
                               + "Portal their list of documents by 4pm on");
            assertThat(disclosure.getDate1()).isEqualTo(nextWorkingDayDate);
            assertThat(disclosure.getInput2())
                .isEqualTo("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                               + "the other party by 4pm on");
            assertThat(disclosure.getDate2()).isEqualTo(nextWorkingDayDate);
            assertThat(disclosure.getInput3())
                .isEqualTo("Requests will be complied with within 7 days of the receipt of the request.");
            assertThat(disclosure.getInput4())
                .isEqualTo("Each party must upload to the Digital Portal copies of those documents on which they "
                               + "wish to rely at trial by 4pm on");
            assertThat(disclosure.getDate3()).isEqualTo(nextWorkingDayDate);

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
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            List<IncludeInOrderToggle> uploadDocToggle = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsUploadDocToggle"),
                new TypeReference<List<IncludeInOrderToggle>>() {}
            );
            List<IncludeInOrderToggle> hearingToggle = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsHearingToggle"),
                new TypeReference<List<IncludeInOrderToggle>>() {}
            );
            List<IncludeInOrderToggle> witnessToggle = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsWitnessStatementsToggle"),
                new TypeReference<List<IncludeInOrderToggle>>() {}
            );
            List<IncludeInOrderToggle> mediationToggle = objectMapper.convertValue(
                response.getData().get("sdoR2SmallClaimsMediationSectionToggle"),
                new TypeReference<List<IncludeInOrderToggle>>() {}
            );

            assertThat(uploadDocToggle).containsExactly(IncludeInOrderToggle.INCLUDE);
            assertThat(hearingToggle).containsExactly(IncludeInOrderToggle.INCLUDE);
            assertThat(witnessToggle).containsExactly(IncludeInOrderToggle.INCLUDE);
            assertThat(response.getData().get("sdoR2SmallClaimsPPIToggle")).isNull();
            assertThat(mediationToggle).isNotNull();
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
                "Site 3 - Adr 3 - CCC 333",
                "Site 5 - Adr 5 - YYY 111"
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
                "Site 3 - Adr 3 - CCC 333",
                "Site 5 - Adr 5 - YYY 111"
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

            DisposalHearingJudgementDeductionValue disposalValue = objectMapper.convertValue(
                response.getData().get("disposalHearingJudgementDeductionValue"),
                DisposalHearingJudgementDeductionValue.class
            );
            DisposalHearingJudgementDeductionValue fastTrackValue = objectMapper.convertValue(
                response.getData().get("fastTrackJudgementDeductionValue"),
                DisposalHearingJudgementDeductionValue.class
            );
            DisposalHearingJudgementDeductionValue smallClaimsValue = objectMapper.convertValue(
                response.getData().get("smallClaimsJudgementDeductionValue"),
                DisposalHearingJudgementDeductionValue.class
            );

            assertThat(disposalValue).isNotNull();
            assertThat(fastTrackValue).isNotNull();
            assertThat(smallClaimsValue).isNotNull();
            assertThat(disposalValue.getValue()).isEqualTo("12.0%");
            assertThat(fastTrackValue.getValue()).isEqualTo("12.0%");
            assertThat(smallClaimsValue.getValue()).isEqualTo("12.0%");
        }
    }

    @Nested
    class MidEventSetOrderDetailsFlags {
        private static final String PAGE_ID = "order-details-navigation";

        @Test
        void showReturnError_whenUnspecIntermediateTrack() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build().toBuilder()
                .allocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM)
                .orderType(OrderType.DISPOSAL)
                .ccdState(JUDICIAL_REFERRAL)
                .build();

            when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsOnly(ERROR_MINTI_DISPOSAL_NOT_ALLOWED);
        }

        @Test
        void showReturnError_whenUnspecMultiTrack() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build().toBuilder()
                .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
                .orderType(OrderType.DISPOSAL)
                .ccdState(JUDICIAL_REFERRAL)
                .build();

            when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsOnly(ERROR_MINTI_DISPOSAL_NOT_ALLOWED);
        }

        @Test
        void showReturnError_whenSpecIntermediateTrack() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .setClaimTypeToSpecClaim()
                .build().toBuilder()
                .responseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name())
                .orderType(OrderType.DISPOSAL)
                .ccdState(JUDICIAL_REFERRAL)
                .build();

            when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsOnly(ERROR_MINTI_DISPOSAL_NOT_ALLOWED);
        }

        @Test
        void showReturnError_whenSpecMultiTrack() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .setClaimTypeToSpecClaim()
                .build().toBuilder()
                .responseClaimTrack(AllocatedTrack.MULTI_CLAIM.name())
                .orderType(OrderType.DISPOSAL)
                .ccdState(JUDICIAL_REFERRAL)
                .build();

            when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsOnly(ERROR_MINTI_DISPOSAL_NOT_ALLOWED);
        }

        @Test
        void smallClaimsFlagAndFastTrackFlagSetToNo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.NO);
            assertThat(updated.getSetFastTrackFlag()).isEqualTo(YesOrNo.NO);
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

            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.YES);
            assertThat(updated.getSetFastTrackFlag()).isEqualTo(YesOrNo.NO);
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

            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.YES);
            assertThat(updated.getSetFastTrackFlag()).isEqualTo(YesOrNo.NO);
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

            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.NO);
            assertThat(updated.getSetFastTrackFlag()).isEqualTo(YesOrNo.YES);
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

            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.NO);
            assertThat(updated.getSetFastTrackFlag()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void fastTRackSdoR2NihlPathTwo() {

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

            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.NO);
            assertThat(updated.getSetFastTrackFlag()).isEqualTo(YesOrNo.YES);
            assertThat(updated.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void fastTrackFlagSetToYesNihlPathOne() {
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

            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.NO);
            assertThat(updated.getSetFastTrackFlag()).isEqualTo(YesOrNo.YES);
            assertThat(updated.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void smallClaimsSdoR2FlagSetToYesPathOne() {
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

            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.YES);
            assertThat(updated.getSetFastTrackFlag()).isEqualTo(YesOrNo.NO);
            assertThat(updated.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void smallClaimsSdoR2FlagSetToYesPathTwo() {
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

            CaseData updated = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getSetSmallClaimsFlag()).isEqualTo(YesOrNo.YES);
            assertThat(updated.getSetFastTrackFlag()).isEqualTo(YesOrNo.NO);
            assertThat(updated.getIsSdoR2NewScreen()).isEqualTo(YesOrNo.YES);
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

        private final SdoNarrativeService narrativeService = new SdoNarrativeService();

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_1v1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String header = narrativeService.buildConfirmationHeader(caseData);
            String body = narrativeService.buildConfirmationBody(caseData);

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

            String header = narrativeService.buildConfirmationHeader(caseData);
            String body = narrativeService.buildConfirmationBody(caseData);

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

            String header = narrativeService.buildConfirmationHeader(caseData);
            String body = narrativeService.buildConfirmationBody(caseData);

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

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            if (valid) {
                assertThat(response.getErrors()).isEmpty();
            } else {
                assertThat(response.getErrors()).hasSize(2);
            }
        }
    }
    private YesOrNo toYesOrNo(Object value) {
        if (value instanceof YesOrNo) {
            return (YesOrNo) value;
        }
        return YesOrNo.valueOf(String.valueOf(value).toUpperCase());
    }

}

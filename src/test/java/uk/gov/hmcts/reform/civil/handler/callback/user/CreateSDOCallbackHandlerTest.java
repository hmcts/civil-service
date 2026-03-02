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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
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
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
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
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.DirectionsOrderStageExecutor;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.pipeline.DirectionsOrderCallbackPipeline;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoConfirmationTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoDocumentTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoOrderDetailsTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl.SdoPrePopulateTask;
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
import uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderCaseProgressionService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoChecklistService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDeadlineService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisclosureOfDocumentsFieldsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisposalGuardService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisposalNarrativeService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisposalOrderDefaultsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDocumentService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDrhFieldsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoExpertEvidenceFieldsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackNarrativeService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackOrderDefaultsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackSpecialistDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoHearingPreparationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoJourneyToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoJudgementDeductionService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNarrativeService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNihlFieldsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNihlOrderService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoOrderDetailsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoPrePopulateService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsNarrativeService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsOrderDefaultsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSubmissionService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoTrackDefaultsService;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
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
    DirectionsOrderCaseProgressionService.class,
    SdoDeadlineService.class,
    SdoJourneyToggleService.class,
    SdoChecklistService.class,
    SdoDisposalGuardService.class,
    SdoDisposalOrderDefaultsService.class,
    SdoDisposalNarrativeService.class,
    SdoFastTrackNarrativeService.class,
    SdoFastTrackOrderDefaultsService.class,
    SdoFastTrackSpecialistDirectionsService.class,
    SdoSmallClaimsOrderDefaultsService.class,
    SdoSmallClaimsNarrativeService.class,
    SdoExpertEvidenceFieldsService.class,
    SdoDisclosureOfDocumentsFieldsService.class,
    SdoJudgementDeductionService.class,
    SdoTrackDefaultsService.class,
    SdoOrderDetailsService.class,
    SdoPrePopulateService.class,
    SdoHearingPreparationService.class,
    SdoDrhFieldsService.class,
    SdoNihlOrderService.class,
    SdoNihlFieldsService.class,
    SdoNarrativeService.class,
    SdoValidationService.class,
    SdoDocumentService.class,
    SdoSubmissionService.class,
    DirectionsOrderCallbackPipeline.class,
    DirectionsOrderStageExecutor.class,
    SdoPrePopulateTask.class,
    SdoOrderDetailsTask.class,
    SdoValidationTask.class,
    SdoDocumentTask.class,
    SdoSubmissionTask.class,
    SdoConfirmationTask.class},
    properties = {"reference.database.enabled=false"})
public class CreateSDOCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String REFERENCE_NUMBER = "000DC001";
    private static final DynamicList options = buildOptions();

    private static DynamicList buildOptions() {
        DynamicListElement element1 = createDynamicListElement("00001", "court 1 - 1 address - Y01 7RB");
        DynamicListElement element2 = createDynamicListElement("00002", "court 2 - 2 address - Y02 7RB");
        DynamicListElement element3 = createDynamicListElement("00003", "court 3 - 3 address - Y03 7RB");
        return createDynamicList(List.of(element1, element2, element3), null);
    }

    private static DynamicList createDynamicList(List<DynamicListElement> items, DynamicListElement value) {
        DynamicList dynamicList = new DynamicList();
        dynamicList.setListItems(items);
        dynamicList.setValue(value);
        return dynamicList;
    }

    private static DynamicListElement createDynamicListElement(String code, String label) {
        return new DynamicListElement(code, label);
    }

    private static DynamicList createStandardCourtList(String preSelectedCourt) {
        DynamicListElement element1 = createDynamicListElement("00001", "court 1 - 1 address - Y01 7RB");
        DynamicListElement element2 = createDynamicListElement(preSelectedCourt, "court 2 - 2 address - Y02 7RB");
        DynamicListElement element3 = createDynamicListElement("00003", "court 3 - 3 address - Y03 7RB");
        return createDynamicList(List.of(element1, element2, element3), element2);
    }

    private static CaseDocument createCaseDocument() {
        Document document = new Document("url", null, null, null, null, null);
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentLink(document);
        return caseDocument;
    }

    private static CaseLocationCivil createCaseLocation(String baseLocation, String region) {
        CaseLocationCivil location = new CaseLocationCivil();
        location.setBaseLocation(baseLocation);
        location.setRegion(region);
        return location;
    }

    private static CaseData withCaseManagementLocation(CaseData caseData) {
        caseData.setCaseManagementLocation(createCaseLocation("00000", null));
        return caseData;
    }

    private static DynamicList cloneDynamicListWithValue(DynamicList source, DynamicListElement value) {
        DynamicList clone = new DynamicList();
        clone.setListItems(source.getListItems());
        clone.setValue(value);
        return clone;
    }

    private SdoR2WitnessOfFact createWitnessesOfFact(LocalDate testDate, boolean valid) {
        Integer testWitnesses = valid ? 0 : -1;
        SdoR2RestrictNoOfWitnessDetails witnessDetails = new SdoR2RestrictNoOfWitnessDetails();
        witnessDetails.setNoOfWitnessClaimant(testWitnesses);
        witnessDetails.setNoOfWitnessDefendant(testWitnesses);

        SdoR2RestrictWitness restrictWitness = new SdoR2RestrictWitness();
        restrictWitness.setRestrictNoOfWitnessDetails(witnessDetails);

        SdoR2WitnessOfFact witnessesOfFact = new SdoR2WitnessOfFact();
        witnessesOfFact.setSdoWitnessDeadlineDate(testDate);
        witnessesOfFact.setSdoR2RestrictWitness(restrictWitness);
        return witnessesOfFact;
    }

    private SdoR2SmallClaimsWitnessStatements createWitnessStatements(boolean valid) {
        int countWitness = valid ? 0 : -1;
        SdoR2SmallClaimsRestrictWitness restrictWitness = new SdoR2SmallClaimsRestrictWitness();
        restrictWitness.setNoOfWitnessClaimant(countWitness);
        restrictWitness.setNoOfWitnessDefendant(countWitness);

        SdoR2SmallClaimsWitnessStatements witnessStatements = new SdoR2SmallClaimsWitnessStatements();
        witnessStatements.setIsRestrictWitness(YES);
        witnessStatements.setSdoR2SmallClaimsRestrictWitness(restrictWitness);
        return witnessStatements;
    }

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CreateSDOCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SdoNarrativeService sdoNarrativeService;

    @MockBean
    protected LocationReferenceDataService locationRefDataService;

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private SdoGeneratorService sdoGeneratorService;

    @MockBean
    private CategoryService categoryService;

    @Value("${court-location.unspecified-claim.epimms-id}")
    private String ccmccEpimsId;

    @Nested
    class AboutToStartCallback extends LocationRefSampleDataBuilder {
        @BeforeEach
        void setup() {
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObject());
            when(workingDayIndicator.getNextWorkingDay(LocalDate.now())).thenReturn(LocalDate.now().plusDays(1));
            when(deadlinesCalculator.calculateFirstWorkingDay(any(LocalDate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            when(deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5))
                .thenReturn(LocalDate.now().plusDays(5));
            when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(LocalDate.now().plusDays(7));
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourt() {
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .caseAccessCategory(UNSPEC_CLAIM)
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList expected = createStandardCourtList(preSelectedCourt);

            assertThat(responseCaseData.getDisposalHearingMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getFastTrackMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getSmallClaimsMethodInPerson()).isEqualTo(expected);
        }

        @Test
        void shouldPopulateLocationListsWhenTransferredOnlineHearingLocationShouldBeNewCaseManagementLocation() {
            Category category = new Category();
            category.setCategoryKey("HearingChannel");
            category.setKey("INTER");
            category.setValueEn("In Person");
            category.setActiveFlag("Y");

            CategorySearchResult categorySearchResult = new CategorySearchResult();
            categorySearchResult.setCategories(List.of(category));
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

            CaseLocationCivil civil = new CaseLocationCivil();
            civil.setRegion("1");
            civil.setBaseLocation("214320");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .reasonForTransfer("Court Closed")
                .transferCourtLocationList(DynamicList.builder()
                                               .value(DynamicListElement.builder()
                                                          .code("97c6385d-dc61-4a46-b58f-2992e5ecb4f4")
                                                          .label("Central London County Court - Thomas More Building, Royal Courts of Justice, Strand, London - WC2A 2LL")
                                                          .build()).build())
                .caseManagementLocation(civil)
                .caseAccessCategory(UNSPEC_CLAIM)
                .build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList expected = createStandardCourtList(preSelectedCourt);

            assertThat(responseCaseData.getDisposalHearingMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getFastTrackMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getSmallClaimsMethodInPerson()).isEqualTo(expected);
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourtAndEnableWelshFlagWithClaimantLanguagePreference() {
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
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
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList expected = createStandardCourtList(preSelectedCourt);

            assertThat(responseCaseData.getDisposalHearingMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getFastTrackMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getSmallClaimsMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getBilingualHint()).isEqualTo(YES);
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourtAndEnableWelshFlagWithRespondentLanguagePreference() {
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");

            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .claimantBilingualLanguagePreference("ENGLISH")
                .caseAccessCategory(UNSPEC_CLAIM)
                .build();
            caseData.setCaseDataLiP(caseDataLiP);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList expected = createStandardCourtList(preSelectedCourt);

            assertThat(responseCaseData.getDisposalHearingMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getFastTrackMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getSmallClaimsMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getBilingualHint()).isEqualTo(YES);
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourtAndEnableWelshFlagWithNoClaimantAndRespondentLanguagePreference() {
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");

            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .claimantBilingualLanguagePreference("BOTH")
                .caseAccessCategory(UNSPEC_CLAIM)
                .build();
            caseData.setCaseDataLiP(caseDataLiP);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList expected = createStandardCourtList(preSelectedCourt);

            assertThat(responseCaseData.getDisposalHearingMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getFastTrackMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getSmallClaimsMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getBilingualHint()).isEqualTo(YES);
        }

        @Test
        void shouldPopulateLocationListsWithPreselectedCourtAndEnableWelshFlagWithClaimantAndRespondentLanguagePreference() {
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            RespondentLiPResponse lipResponse = new RespondentLiPResponse();
            lipResponse.setRespondent1ResponseLanguage("ENGLISH");

            CaseDataLiP respondentLipData = new CaseDataLiP();
            respondentLipData.setRespondent1LiPResponse(lipResponse);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing()
                .claimantBilingualLanguagePreference("ENGLISH")
                .caseAccessCategory(UNSPEC_CLAIM)
                .build();
            caseData.setCaseDataLiP(respondentLipData);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicList expected = createStandardCourtList(preSelectedCourt);

            assertThat(responseCaseData.getDisposalHearingMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getFastTrackMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getSmallClaimsMethodInPerson()).isEqualTo(expected);
            assertThat(responseCaseData.getBilingualHint()).isNull();
        }

        @Test
        void shouldGenerateDynamicListsCorrectly() {
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            CaseDocument order = createCaseDocument();
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
            DisposalHearingAddNewDirections disposalHearingAddNewDirections = new DisposalHearingAddNewDirections();
            disposalHearingAddNewDirections.setDirectionComment("test");
            Element<DisposalHearingAddNewDirections> disposalHearingAddNewDirectionsElement = new Element<>();
            disposalHearingAddNewDirectionsElement.setValue(disposalHearingAddNewDirections);
            SmallClaimsAddNewDirections smallClaimsAddNewDirections = new SmallClaimsAddNewDirections();
            smallClaimsAddNewDirections.setDirectionComment("test");

            Element<SmallClaimsAddNewDirections> smallClaimsAddNewDirectionsElement = new Element<>();
            smallClaimsAddNewDirectionsElement.setValue(smallClaimsAddNewDirections);

            FastTrackAddNewDirections fastTrackAddNewDirections = new FastTrackAddNewDirections();
            fastTrackAddNewDirections.setDirectionComment("test");

            Element<FastTrackAddNewDirections> fastTrackAddNewDirectionsElement = new Element<>();
            fastTrackAddNewDirectionsElement.setValue(fastTrackAddNewDirections);

            List<FastTrack> directions = List.of(FastTrack.fastClaimBuildingDispute);
            List<SmallTrack> smallDirections = List.of(SmallTrack.smallClaimCreditHire);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setDrawDirectionsOrderRequired(YES);
            caseData.setDrawDirectionsOrderSmallClaims(YES);
            caseData.setFastClaims(directions);
            caseData.setSmallClaims(smallDirections);
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            caseData.setOrderType(OrderType.DECIDE_DAMAGES);
            caseData.setTrialAdditionalDirectionsForFastTrack(directions);
            caseData.setDrawDirectionsOrderSmallClaimsAdditionalDirections(smallDirections);
            FastTrackAllocation fastTrackAllocation = new FastTrackAllocation();
            fastTrackAllocation.setAssignComplexityBand(YES);
            caseData.setFastTrackAllocation(fastTrackAllocation);
            caseData.setDisposalHearingAddNewDirections(List.of(disposalHearingAddNewDirectionsElement));
            caseData.setSmallClaimsAddNewDirections(List.of(smallClaimsAddNewDirectionsElement));
            caseData.setFastTrackAddNewDirections(List.of(fastTrackAddNewDirectionsElement));
            SDOHearingNotes sdoHearingNotes = new SDOHearingNotes();
            sdoHearingNotes.setInput("TEST");
            caseData.setSdoHearingNotes(sdoHearingNotes);
            FastTrackHearingNotes fastTrackHearingNotes = new FastTrackHearingNotes();
            fastTrackHearingNotes.setInput("TEST");
            caseData.setFastTrackHearingNotes(fastTrackHearingNotes);
            caseData.setDisposalHearingHearingNotes("TEST");
            caseData.setCcdState(CASE_PROGRESSION);
            caseData.setDecisionOnRequestReconsiderationOptions(DecisionOnRequestReconsiderationOptions.CREATE_SDO);
            caseData.setIsSdoR2NewScreen(NO);

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
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setFastClaims(List.of(FastTrack.fastClaimNoiseInducedHearingLoss));

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            DynamicListElement locationOne = createDynamicListElement("00001", "court 1 - 1 address - Y01 7RB");
            DynamicListElement locationTwo = createDynamicListElement(preSelectedCourt, "court 2 - 2 address - Y02 7RB");
            DynamicListElement locationThree = createDynamicListElement("00003", "court 3 - 3 address - Y03 7RB");
            DynamicList altExpected = createDynamicList(List.of(locationOne, locationTwo, locationThree), null);

            DynamicListElement otherLocation = createDynamicListElement("OTHER_LOCATION", "Other location");
            DynamicList expected = createDynamicList(List.of(locationTwo, otherLocation), locationTwo);

            assertThat(responseCaseData.getSdoR2Trial().getHearingCourtLocationList()).isEqualTo(expected);
            assertThat(responseCaseData.getSdoR2Trial().getAltHearingCourtLocationList()).isEqualTo(altExpected);
        }

        @Test
        void shouldPopulateDefaultFieldsForNihl() {
            List<FastTrack> fastTrackList = new ArrayList<>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            String preSelectedCourt = "214320";
            List<LocationRefData> locations = List.of(
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);

            Category category = hearingChannelCategory(HearingSubChannel.INTER.name(), HearingMethod.IN_PERSON.getLabel());
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setFastClaims(fastTrackList);

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

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData data = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getText()).isEqualTo(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT);
            assertThat(data.getSdoR2SmallClaimsWitnessStatementOther().getSdoStatementOfWitness())
                .isEqualTo(SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT);
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
                .atStateClaimIssued().build();
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            caseData.setDrawDirectionsOrderRequired(NO);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

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

        }

        @Test
        void shouldUpdateCaseManagementLocation_whenUnder1000SpecCcmcc() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);
            caseData.setCaseManagementLocation(createCaseLocation(ccmccEpimsId, "ccmcRegion"));
            caseData.setTotalClaimAmount(BigDecimal.valueOf(999));
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            Party applicant1 = new Party();
            applicant1.setType(Party.Type.INDIVIDUAL);
            caseData.setApplicant1(applicant1);

            CaseLocationCivil applicantRequestedLocation = createCaseLocation(
                "app court requested epimm",
                "app court request region"
            );
            RequestedCourt applicantRequestedCourt = new RequestedCourt();
            applicantRequestedCourt.setCaseLocation(applicantRequestedLocation);
            applicantRequestedCourt.setResponseCourtCode("123");
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQRequestedCourt(applicantRequestedCourt);
            caseData.setApplicant1DQ(applicant1DQ);

            Party respondent1 = new Party();
            respondent1.setType(Party.Type.INDIVIDUAL);
            caseData.setRespondent1(respondent1);

            CaseLocationCivil respondentRequestedLocation = createCaseLocation(
                "def court requested epimm",
                "def court request region"
            );
            RequestedCourt respondentRequestedCourt = new RequestedCourt();
            respondentRequestedCourt.setCaseLocation(respondentRequestedLocation);
            respondentRequestedCourt.setResponseCourtCode("321");
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQRequestedCourt(respondentRequestedCourt);
            caseData.setRespondent1DQ(respondent1DQ);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("caseManagementLocation")
                .extracting("region", "baseLocation")
                .containsExactly("def court request region", "def court requested epimm");
        }

        private Applicant1DQ buildApplicantRequestedCourt() {
            CaseLocationCivil applicantRequestedLocation = new CaseLocationCivil();
            applicantRequestedLocation.setBaseLocation("app court requested epimm");
            applicantRequestedLocation.setRegion("app court request region");
            RequestedCourt applicantRequestedCourt = new RequestedCourt();
            applicantRequestedCourt.setCaseLocation(applicantRequestedLocation);
            applicantRequestedCourt.setResponseCourtCode("123");
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQRequestedCourt(applicantRequestedCourt);
            return applicant1DQ;
        }

        private Respondent1DQ buildRespondentRequestedCourt() {
            CaseLocationCivil respondentRequestedLocation = new CaseLocationCivil();
            respondentRequestedLocation.setBaseLocation("def court requested epimm");
            respondentRequestedLocation.setRegion("def court request region");
            RequestedCourt respondentRequestedCourt = new RequestedCourt();
            respondentRequestedCourt.setCaseLocation(respondentRequestedLocation);
            respondentRequestedCourt.setResponseCourtCode("321");
            Respondent1DQ respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQRequestedCourt(respondentRequestedCourt);
            return respondent1DQ;
        }

        @Test
        void shouldNotUpdateCaseManagementLocation_whenNotUnder1000SpecCcmcc() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);
            CaseLocationCivil existingLocation = new CaseLocationCivil();
            existingLocation.setBaseLocation("1010101");
            existingLocation.setRegion("orange");
            caseData.setCaseManagementLocation(existingLocation);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(1999));
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            Party applicant1 = new Party();
            applicant1.setType(Party.Type.INDIVIDUAL);
            caseData.setApplicant1(applicant1);

            caseData.setApplicant1DQ(
                buildApplicantRequestedCourt()
            );

            Party respondent1 = new Party();
            respondent1.setType(Party.Type.INDIVIDUAL);
            caseData.setRespondent1(respondent1);

            caseData.setRespondent1DQ(
                buildRespondentRequestedCourt()
            );

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

        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            List<String> items = List.of("label 1", "label 2", "label 3");
            DynamicList localOptions = DynamicList.fromList(items, Object::toString, items.get(0), false);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setCaseManagementLocation(createCaseLocation("00000", null));
            caseData.setDisposalHearingMethodInPerson(localOptions);
            caseData.setFastTrackMethodInPerson(localOptions);
            caseData.setSmallClaimsMethodInPerson(localOptions);
            caseData.setSetFastTrackFlag(YES);
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            given(time.now()).willReturn(submittedDate);

            given(featureToggleService.isLocationWhiteListedForCaseProgression(anyString())).willReturn(true);
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            CaseData caseData = params.getCaseData();
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            caseData.setDrawDirectionsOrderRequired(NO);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                params.copy().caseData(caseData)
            );

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getBusinessProcess()).isNotNull();
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(CREATE_SDO.name());
            assertThat(updatedData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
        }
    }

    @Nested
    class AboutToSubmitCallbackVariableCase {
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setCaseManagementLocation(createCaseLocation("00000", null));
            caseData.setFastTrackMethodInPerson(localOptions);
            caseData.setDisposalHearingMethodInPerson(localOptions);
            caseData.setSmallClaimsMethodInPerson(localOptions);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("sdoOrderDocument");
        }
    }

    @Nested
    class AboutToSubmitCallbackWelshParty {
        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            given(time.now()).willReturn(submittedDate);
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        }

        @Test
        void shouldSaveDocumentToTempList_whenClaimantIsWelsh() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setSdoOrderDocument(createCaseDocument());
            caseData.setClaimantBilingualLanguagePreference("WELSH");
            caseData.setCaseManagementLocation(createCaseLocation("00000", null));
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).doesNotContainKey("sdoOrderDocument");
            assertThat(response.getData()).doesNotContainKey("systemGeneratedCaseDocuments");
            assertThat(response.getData()).containsKey("preTranslationDocuments");
        }

        @Test
        void shouldSaveDocumentToTempList_whenDefendantIsWelsh() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setSdoOrderDocument(createCaseDocument());
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            caseData.setCaseDataLiP(caseDataLiP);
            caseData.setCaseManagementLocation(createCaseLocation("00000", null));
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
        DynamicListElement optionOne = createDynamicListElement("00001", "court 1 - 1 address - Y01 7RB");
        DynamicListElement optionTwo = createDynamicListElement("00002", "court 2 - 2 address - Y02 7RB");
        DynamicListElement optionThree = createDynamicListElement("00003", "court 3 - 3 address - Y03 7RB");
        DynamicList localOptions = createDynamicList(List.of(optionOne, optionTwo, optionThree), null);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        caseData.setDisposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson);
        caseData.setDisposalHearingMethodInPerson(cloneDynamicListWithValue(localOptions, optionTwo));
        caseData.setFastTrackMethodInPerson(localOptions);
        caseData.setSmallClaimsMethodInPerson(localOptions);
        caseData.setDisposalHearingMethodInPerson(cloneDynamicListWithValue(localOptions, optionTwo));
        caseData.setDisposalHearingMethodToggle(Collections.singletonList(OrderDetailsPagesSectionsToggle.SHOW));
        caseData.setOrderType(OrderType.DISPOSAL);
        caseData.setRespondent1Represented(NO);
        caseData.setCaseManagementLocation(createCaseLocation(optionTwo.getCode(), null));

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(featureToggleService.isLocationWhiteListedForCaseProgression(optionTwo.getCode()))
            .thenReturn(isLocationWhiteListed);
        when(locationRefDataService.getLocationMatchingLabel(optionTwo.getCode(), params.getParams().get(
            CallbackParams.Params.BEARER_TOKEN).toString()))
            .thenReturn(Optional.of(locationRefDataWithRegion()));

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
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
        DynamicListElement transferValue = createDynamicListElement(null, "Site 1 - Adr 1 - AAA 111");
        DynamicList transferList = createDynamicList(null, transferValue);
        caseData.setTransferCourtLocationList(transferList);
        caseData.setCaseManagementLocation(createCaseLocation("111", "2"));

        CaseData caseData2 = CaseDataBuilder.builder().build();
        caseData2.setApplicant1Represented(applicantRepresented);
        caseData2.setRespondent1Represented(respondent1Represented);
        CallbackParams params = callbackParamsOf(caseData2, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            assertEquals(eaCourtLocation, responseCaseData.getEaCourtLocation());
        } else {
            assertThat(responseCaseData.getEaCourtLocation()).isNull();
        }
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));

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
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQRequestedCourt(new RequestedCourt());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));
            caseData.setApplicant1DQ(applicant1DQ);
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            caseData.setCaseAccessCategory(SPEC_CLAIM);
            caseData.setTotalClaimAmount(BigDecimal.valueOf(10000));

            CaseLocationCivil requestedLocation = createCaseLocation("00000", "dummy region");
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setResponseCourtCode("court3");
            requestedCourt.setCaseLocation(requestedLocation);
            Applicant1DQ applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQRequestedCourt(requestedCourt);
            caseData.setApplicant1DQ(applicant1DQ);

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
        CaseData scenario1 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        DynamicListElement selectedCourt = createDynamicListElement("00002", "court 2 - 2 address - Y02 7RB");
        DynamicList fastTrackSelection = cloneDynamicListWithValue(options, selectedCourt);
        scenario1.setCaseAccessCategory(UNSPEC_CLAIM);
        scenario1.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
        scenario1.setFastTrackMethodInPerson(fastTrackSelection);
        scenario1.setDrawDirectionsOrderRequired(NO);
        scenario1.setClaimsTrack(ClaimsTrack.fastTrack);

        CaseData scenario2 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario2.setCaseAccessCategory(UNSPEC_CLAIM);
        scenario2.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
        scenario2.setFastTrackMethodInPerson(cloneDynamicListWithValue(options, selectedCourt));
        scenario2.setDrawDirectionsOrderRequired(YES);
        scenario2.setDrawDirectionsOrderSmallClaims(NO);
        scenario2.setOrderType(OrderType.DECIDE_DAMAGES);

        CaseData scenario3 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        DynamicList disposalSelection = cloneDynamicListWithValue(options, selectedCourt);
        scenario3.setCaseAccessCategory(UNSPEC_CLAIM);
        scenario3.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        scenario3.setDisposalHearingMethodInPerson(disposalSelection);
        scenario3.setDrawDirectionsOrderRequired(YES);
        scenario3.setDrawDirectionsOrderSmallClaims(NO);
        scenario3.setOrderType(OrderType.DISPOSAL);

        CaseData scenario4 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        DynamicList smallClaimsSelection = cloneDynamicListWithValue(options, selectedCourt);
        scenario4.setCaseAccessCategory(UNSPEC_CLAIM);
        scenario4.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        scenario4.setSmallClaimsMethodInPerson(smallClaimsSelection);
        scenario4.setDrawDirectionsOrderRequired(NO);
        scenario4.setClaimsTrack(ClaimsTrack.smallClaimsTrack);

        CaseData scenario5 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario5.setCaseAccessCategory(UNSPEC_CLAIM);
        scenario5.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        scenario5.setSmallClaimsMethodInPerson(cloneDynamicListWithValue(options, selectedCourt));
        scenario5.setDrawDirectionsOrderRequired(YES);
        scenario5.setDrawDirectionsOrderSmallClaims(YES);
        scenario5.setOrderType(OrderType.DECIDE_DAMAGES);

        CaseData scenario6 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario6.setCaseAccessCategory(UNSPEC_CLAIM);
        scenario6.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
        scenario6.setDisposalHearingMethodInPerson(cloneDynamicListWithValue(options, selectedCourt));
        scenario6.setDrawDirectionsOrderRequired(YES);
        scenario6.setDrawDirectionsOrderSmallClaims(NO);
        scenario6.setOrderType(OrderType.DISPOSAL);

        return Stream.of(
            Arguments.of(scenario1, AllocatedTrack.FAST_CLAIM),
            Arguments.of(scenario2, AllocatedTrack.FAST_CLAIM),
            Arguments.of(scenario3, AllocatedTrack.FAST_CLAIM),
            Arguments.of(scenario4, AllocatedTrack.SMALL_CLAIM),
            Arguments.of(scenario5, AllocatedTrack.SMALL_CLAIM),
            Arguments.of(scenario6, AllocatedTrack.SMALL_CLAIM)
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
        DynamicListElement selectedCourt = createDynamicListElement("00002", "court 2 - 2 address - Y02 7RB");

        CaseData scenario1 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario1.setCaseAccessCategory(SPEC_CLAIM);
        scenario1.setResponseClaimTrack("SMALL_CLAIM");
        scenario1.setFastTrackMethodInPerson(cloneDynamicListWithValue(options, selectedCourt));
        scenario1.setDrawDirectionsOrderRequired(NO);
        scenario1.setClaimsTrack(ClaimsTrack.fastTrack);

        CaseData scenario2 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario2.setCaseAccessCategory(SPEC_CLAIM);
        scenario2.setResponseClaimTrack("SMALL_CLAIM");
        scenario2.setFastTrackMethodInPerson(cloneDynamicListWithValue(options, selectedCourt));
        scenario2.setDrawDirectionsOrderRequired(YES);
        scenario2.setDrawDirectionsOrderSmallClaims(NO);
        scenario2.setOrderType(OrderType.DECIDE_DAMAGES);

        CaseData scenario3 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario3.setCaseAccessCategory(SPEC_CLAIM);
        scenario3.setResponseClaimTrack("SMALL_CLAIM");
        scenario3.setDrawDirectionsOrderRequired(YES);
        scenario3.setDrawDirectionsOrderSmallClaims(NO);
        scenario3.setOrderType(OrderType.DECIDE_DAMAGES);

        CaseData scenario4 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario4.setCaseAccessCategory(SPEC_CLAIM);
        scenario4.setResponseClaimTrack("FAST_CLAIM");
        scenario4.setDisposalHearingMethodInPerson(cloneDynamicListWithValue(options, selectedCourt));
        scenario4.setDrawDirectionsOrderRequired(YES);
        scenario4.setDrawDirectionsOrderSmallClaims(NO);
        scenario4.setOrderType(OrderType.DISPOSAL);

        CaseData scenario5 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario5.setCaseAccessCategory(SPEC_CLAIM);
        scenario5.setResponseClaimTrack("FAST_CLAIM");
        scenario5.setDrawDirectionsOrderRequired(YES);
        scenario5.setDrawDirectionsOrderSmallClaims(NO);
        scenario5.setOrderType(OrderType.DISPOSAL);

        CaseData scenario6 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario6.setCaseAccessCategory(SPEC_CLAIM);
        scenario6.setResponseClaimTrack("FAST_CLAIM");
        scenario6.setSmallClaimsMethodInPerson(cloneDynamicListWithValue(options, selectedCourt));
        scenario6.setDrawDirectionsOrderRequired(NO);
        scenario6.setClaimsTrack(ClaimsTrack.smallClaimsTrack);

        CaseData scenario7 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario7.setCaseAccessCategory(SPEC_CLAIM);
        scenario7.setResponseClaimTrack("FAST_CLAIM");
        scenario7.setSmallClaimsMethodInPerson(cloneDynamicListWithValue(options, selectedCourt));
        scenario7.setDrawDirectionsOrderRequired(YES);
        scenario7.setDrawDirectionsOrderSmallClaims(YES);
        scenario7.setOrderType(OrderType.DECIDE_DAMAGES);

        CaseData scenario8 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario8.setCaseAccessCategory(SPEC_CLAIM);
        scenario8.setResponseClaimTrack("FAST_CLAIM");
        scenario8.setDrawDirectionsOrderRequired(YES);
        scenario8.setDrawDirectionsOrderSmallClaims(YES);
        scenario8.setOrderType(OrderType.DECIDE_DAMAGES);

        CaseData scenario9 = withCaseManagementLocation(
            CaseDataBuilder.builder().atStateClaimDraft().build()
        );
        scenario9.setCaseAccessCategory(SPEC_CLAIM);
        scenario9.setResponseClaimTrack("SMALL_CLAIM");
        scenario9.setDisposalHearingMethodInPerson(cloneDynamicListWithValue(options, selectedCourt));
        scenario9.setDrawDirectionsOrderRequired(YES);
        scenario9.setDrawDirectionsOrderSmallClaims(NO);
        scenario9.setOrderType(OrderType.DISPOSAL);

        return Stream.of(
            Arguments.of(scenario1, "FAST_CLAIM"),
            Arguments.of(scenario2, "FAST_CLAIM"),
            Arguments.of(scenario3, "FAST_CLAIM"),
            Arguments.of(scenario4, "FAST_CLAIM"),
            Arguments.of(scenario5, "FAST_CLAIM"),
            Arguments.of(scenario6, "SMALL_CLAIM"),
            Arguments.of(scenario7, "SMALL_CLAIM"),
            Arguments.of(scenario8, "SMALL_CLAIM"),
            Arguments.of(scenario9, "SMALL_CLAIM")
        );
    }

    @Nested
    class MidEventPrePopulateOrderDetailsPagesCallback extends LocationRefSampleDataBuilder {
        private LocalDate newDate;
        private LocalDate nextWorkingDayDate;

        @BeforeEach
        void setup() {
            newDate = LocalDate.of(2020, 1, 15);
            nextWorkingDayDate = LocalDate.of(2023, 12, 15);
            LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            when(time.now()).thenReturn(localDateTime);
            when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(nextWorkingDayDate);
            when(deadlinesCalculator.calculateFirstWorkingDay(any(LocalDate.class))).thenReturn(nextWorkingDayDate);
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
                .build();
            caseData.setCaseManagementLocation(createCaseLocation("00000", null));
            given(locationRefDataService.getHearingCourtLocations(any()))
                .willReturn(getSampleCourLocationsRefObjectToSort());
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(true);

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
            Optional<LocationRefData> shouldBeSelected = getSampleCourLocationsRefObjectToSort().stream()
                .filter(locationRefData -> locationRefData.getEpimmsId().equals(
                    caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().getCaseLocation().getBaseLocation()))
                .findFirst();
            assertThat(shouldBeSelected).isPresent();
            assertThat(dynamicList.getValue()).isNotNull()
                .extracting("label").isEqualTo(LocationReferenceDataService.getDisplayEntry(shouldBeSelected.get()));

            assertThat(response.getData()).extracting("showCarmFields").isEqualTo("Yes");

            assertThat(response.getData()).extracting("fastTrackAltDisputeResolutionToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackVariationOfDirectionsToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackSettlementToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackDisclosureOfDocumentsToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackWitnessOfFactToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLossToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackCostsToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackTrialToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackMethodToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocumentsToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFactToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidenceToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingQuestionsToExpertsToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLossToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearingToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingMethodToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingBundleToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingClaimSettlingToggle").isNotNull();
            assertThat(response.getData()).extracting("disposalHearingCostsToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsHearingToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsMethodToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsDocumentsToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsWitnessStatementToggle").isNotNull();
            assertThat(response.getData()).extracting("smallClaimsMediationSectionToggle").isNotNull();
            assertThat(response.getData()).extracting("caseManagementLocation").isNotNull();

            assertThat(response.getData()).extracting("disposalHearingJudgesRecital").extracting("input")
                .isEqualTo("Upon considering the claim form, particulars of claim, statements of case"
                               + " and Directions questionnaires");

            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocuments").extracting("input1")
                .isEqualTo("The parties shall serve on each other copies of the documents upon which reliance is "
                               + "to be placed at the disposal hearing by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingDisclosureOfDocuments").extracting("input2")
                .isEqualTo(
                    "The parties must upload to the Digital Portal copies of those documents which they wish the "
                        + "court to consider when deciding the amount of damages, by 4pm on");

            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input3")
                .isEqualTo("The claimant must upload to the Digital Portal copies of the witness statements"
                               + " of all witnesses of fact on whose evidence reliance is to be placed by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("date2")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input4")
                .isEqualTo("The provisions of CPR 32.6 apply to such evidence.");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input5")
                .isEqualTo("Any application by the defendant in relation to CPR 32.7 must be made by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("date3")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("disposalHearingWitnessOfFact").extracting("input6")
                .isEqualTo("and must be accompanied by proposed directions for allocation and listing for trial on "
                               + "quantum. This is because cross-examination will cause the hearing to exceed "
                               + "the 30-minute maximum time estimate for a disposal hearing.");

            assertThat(response.getData()).extracting("disposalHearingMedicalEvidence").extracting("input")
                .isEqualTo("The claimant has permission to rely upon the written expert evidence already uploaded "
                               + "to the Digital Portal with the particulars of claim and in addition has permission to"
                               + " rely upon any associated correspondence or updating report which is uploaded"
                               + " to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingMedicalEvidence").extracting("date")
                .isEqualTo(nextWorkingDayDate.toString());

            assertThat(response.getData()).extracting("disposalHearingQuestionsToExperts").extracting("date")
                .isEqualTo(nextWorkingDayDate.toString());

            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("input2")
                .isEqualTo("If there is a claim for ongoing or future loss in the original schedule of losses, "
                               + "the claimant must upload to the Digital Portal an up-to-date schedule of loss "
                               + "by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("date2")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("input3")
                .isEqualTo("If the defendant wants to challenge this claim, "
                               + "they must send an up-to-date counter-schedule of loss "
                               + "to the claimant by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("date3")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("input4")
                .isEqualTo("If the defendant want to challenge the sums claimed in the schedule of loss"
                               + " they must upload to the Digital Portal an updated counter schedule of loss "
                               + "by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingSchedulesOfLoss").extracting("date4")
                .isEqualTo(nextWorkingDayDate.toString());

            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearing").extracting("input")
                .isEqualTo("This claim will be listed for final disposal "
                               + "before a judge on the first available date after");
            assertThat(response.getData()).extracting("disposalHearingFinalDisposalHearing").extracting("date")
                .isEqualTo(LocalDate.now().plusWeeks(16).toString());

            assertThat(response.getData()).extracting("disposalHearingBundle").extracting("input")
                .isEqualTo("At least 7 days before the disposal hearing, "
                               + "the claimant must file and serve");

            assertThat(response.getData()).extracting("disposalHearingNotes").extracting("input")
                .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have"
                               + " this Order set aside or varied. Any such application must be uploaded "
                               + "to the Digital Portal together with the appropriate fee, by 4pm on");
            assertThat(response.getData()).extracting("disposalHearingNotes").extracting("date")
                .isEqualTo(nextWorkingDayDate.toString());

            assertThat(response.getData()).doesNotHaveToString("disposalHearingJudgementDeductionValue");

            assertThat(response.getData()).extracting("fastTrackJudgesRecital").extracting("input")
                .isEqualTo("Upon considering the statements of case and the information provided by the parties,");

            assertThat(response.getData()).doesNotHaveToString("fastTrackJudgementDeductionValue");

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

            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input1")
                .isEqualTo("The claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on");
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("date1")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input2")
                .isEqualTo("If the defendant wants to challenge this claim, upload to the Digital Portal "
                               + "counter-schedule of loss by 4pm on");
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("date2")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackSchedulesOfLoss").extracting("input3")
                .isEqualTo("If there is a claim for future pecuniary loss and the parties have not already set out "
                               + "their case on periodical payments, they must do so in the respective schedule and "
                               + "counter-schedule.");
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("input1")
                .isEqualTo("The time provisionally allowed for this trial is");
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("date1")
                .isEqualTo(LocalDate.now().plusWeeks(22).toString());
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("date2")
                .isEqualTo(LocalDate.now().plusWeeks(30).toString());
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("input2")
                .isEqualTo("If either party considers that the time estimate is insufficient, they must inform the"
                               + " court within 7 days of the date stated on this order.");
            assertThat(response.getData()).extracting("fastTrackTrial").extracting("input3")
                .isEqualTo("At least 7 days before the trial, the claimant must upload to the Digital Portal");

            assertThat(response.getData()).extracting("fastTrackNotes").extracting("input")
                .isEqualTo("This Order has been made without a hearing. Each party has the right to apply to have this"
                               + " Order set aside or varied. Any application must be received by the Court,"
                               + " together with the appropriate fee by 4pm on");

            assertThat(response.getData()).extracting("fastTrackNotes").extracting("date")
                .isEqualTo(nextWorkingDayDate.toString());

            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input1")
                .isEqualTo("The claimant must prepare a Scott Schedule of the defects, items of damage, "
                               + "or any other relevant matters");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input2")
                .isEqualTo("""
                        The columns should be headed:
                          •  Item
                          •  Alleged defect
                          •  Claimant’s costing
                          •  Defendant’s response
                          •  Defendant’s costing
                          •  Reserved for Judge’s use""");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input3")
                .isEqualTo("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns"
                               + " completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("date1")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("input4")
                .isEqualTo("The defendant must upload to the Digital Portal an amended version of the Scott Schedule "
                               + "with the relevant columns in response completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackBuildingDispute").extracting("date2")
                .isEqualTo(nextWorkingDayDate.toString());

            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input1")
                .isEqualTo("Documents should be retained as follows:");
            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input2")
                .isEqualTo("a) The parties must retain all electronically stored documents relating to the issues in"
                               + " this claim.");
            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input3")
                .isEqualTo("b) the defendant must retain the original clinical notes relating to the issues in this"
                               + " claim. The defendant must give facilities for inspection by the claimant, the"
                               + " claimant's legal advisers and experts of these original notes on 7 days written"
                               + " notice.");
            assertThat(response.getData()).extracting("fastTrackClinicalNegligence").extracting("input4")
                .isEqualTo("c) Legible copies of the medical and educational records of the claimant are to be placed "
                               + "in a separate paginated bundle by the claimant's solicitors and kept up to date. "
                               + "All references to medical notes are to be made by reference "
                               + "to the pages in that bundle.");

            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting("input1")
                .isEqualTo("""
                        If impecuniosity is alleged by the claimant and not admitted by the defendant, the \
                        claimant's disclosure as ordered earlier in this Order must include:
                        a) Evidence of all income from all sources for a period of 3 months prior to the \
                        commencement of hire until the earlier of:
                         \
                             i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        b) Copies of all bank, credit card, and saving account statements for a period of 3\
                         months prior to the commencement of hire until the earlier of:
                             i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        c) Evidence of any loan, overdraft or other credit facilities available to the \
                        claimant.""");
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting(
                    "sdoR2FastTrackCreditHireDetails").extracting("input2")
                .isEqualTo("""
                        The claimant must upload to the Digital Portal a witness statement addressing
                        a) the need to hire a replacement vehicle; and
                        b) impecuniosity""");
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting(
                    "sdoR2FastTrackCreditHireDetails").extracting("date1")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting(
                    "sdoR2FastTrackCreditHireDetails").extracting("input3")
                .isEqualTo(
                    "A failure to comply with the paragraph above will result in the claimant being debarred "
                        + "from asserting need or relying on impecuniosity as the case may be at the final "
                        + "hearing, save with permission of the Trial Judge.");
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting(
                    "sdoR2FastTrackCreditHireDetails").extracting("input4")
                .isEqualTo(
                    "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                        + "later than 4pm on");
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting(
                    "sdoR2FastTrackCreditHireDetails").extracting("date2")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting("input5")
                .isEqualTo("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                               + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                               + "one witness to provide evidence of basic hire rates available within the claimant's "
                               + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                               + "is available.");
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting("input6")
                .isEqualTo("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting("date3")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting("input7")
                .isEqualTo("and the claimant's evidence in reply if so advised to be uploaded by 4pm on");
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting("date4")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("sdoR2FastTrackCreditHire").extracting("input8")
                .isEqualTo("This witness statement is limited to 10 pages per party, including any appendices.");

            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input1")
                .isEqualTo("The claimant must prepare a Scott Schedule of the items in disrepair.");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input2")
                .isEqualTo("""
                        The columns should be headed:
                          •  Item
                          •  Alleged disrepair
                          •  Defendant’s response
                          •  Reserved for Judge’s use""");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input3")
                .isEqualTo("The claimant must upload to the Digital Portal the Scott Schedule with the relevant "
                               + "columns completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("date1")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("input4")
                .isEqualTo("The defendant must upload to the Digital Portal the amended Scott Schedule with the "
                               + "relevant columns in response completed by 4pm on");
            assertThat(response.getData()).extracting("fastTrackHousingDisrepair").extracting("date2")
                .isEqualTo(nextWorkingDayDate.toString());

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

            assertThat(response.getData()).extracting("fastTrackRoadTrafficAccident").extracting("input")
                .isEqualTo("Photographs and/or a plan of the accident location shall be prepared and agreed by the "
                               + "parties and uploaded to the Digital Portal by 4pm on");

            assertThat(response.getData()).extracting("smallClaimsJudgesRecital").extracting("input")
                .isEqualTo("Upon considering the statements of case and the information provided by the parties,");

            assertThat(response.getData()).doesNotHaveToString("smallClaimsJudgementDeductionValue");

            assertThat(response.getData()).extracting("smallClaimsHearing").extracting("input1")
                .isEqualTo("The hearing of the claim will be on a date to be notified to you by a separate "
                               + "notification. The hearing will have a time estimate of");
            assertThat(response.getData()).extracting("smallClaimsHearing").extracting("input2")
                .isEqualTo("The claimant must by no later than 4 weeks before the hearing date, pay the court the "
                               + "required hearing fee or submit a fully completed application for Help with Fees. \n"
                               + "If the claimant fails to pay the fee or obtain a fee exemption by that time the "
                               + "claim will be struck without further order.");

            assertThat(response.getData()).extracting("smallClaimsDocuments").extracting("input1")
                .isEqualTo("Each party must upload to the Digital Portal copies of all documents which they wish the"
                               + " court to consider when reaching its decision not less than 21 days before "
                               + "the hearing.");
            assertThat(response.getData()).extracting("smallClaimsDocuments").extracting("input2")
                .isEqualTo("The court may refuse to consider any document which has not been uploaded to the "
                               + "Digital Portal by the above date.");

            assertThat(response.getData()).extracting("smallClaimsNotes").extracting("input")
                .isEqualTo("This order has been made without hearing. "
                               + "Each party has the right to apply to have this Order set aside or varied. "
                               + "Any such application must be received by the Court "
                               + "(together with the appropriate fee) by 4pm on "
                               + DateFormatHelper.formatLocalDate(newDate, DateFormatHelper.DATE) + ".");

            assertThat(response.getData()).extracting("smallClaimsMediationSectionStatement").extracting("input")
                .isEqualTo("If you failed to attend a mediation appointment,"
                               + " then the judge at the hearing may impose a sanction. "
                               + "This could require you to pay costs, or could result in your claim or defence being dismissed. "
                               + "You should deliver to every other party, and to the court, your explanation for non-attendance, "
                               + "with any supporting documents, at least 14 days before the hearing. "
                               + "Any other party who wishes to comment on the failure to attend the mediation appointment should "
                               + "deliver their comments,"
                               + " with any supporting documents, to all parties and to the court at least 14 days before the hearing.");

            assertThat(response.getData()).doesNotHaveToString("smallClaimsFlightDelay");

            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input1")
                .isEqualTo("""
                        If impecuniosity is alleged by the claimant and not admitted by the defendant, the \
                        claimant's disclosure as ordered earlier in this Order must include:
                        a) Evidence of all income from all sources for a period of 3 months prior to the \
                        commencement of hire until the earlier of:
                         \
                             i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        b) Copies of all bank, credit card, and saving account statements for a period of 3\
                         months prior to the commencement of hire until the earlier of:
                             i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        c) Evidence of any loan, overdraft or other credit facilities available to the \
                        claimant.""");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input2")
                .isEqualTo("""
                        The claimant must upload to the Digital Portal a witness statement addressing
                        a) the need to hire a replacement vehicle; and
                        b) impecuniosity""");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date1")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input3")
                .isEqualTo("A failure to comply with the paragraph above will result in the claimant being debarred "
                               + "from asserting need or relying on impecuniosity as the case may be at the final "
                               + "hearing, save with permission of the Trial Judge.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input4")
                .isEqualTo("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
                               + "later than 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date2")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input5")
                .isEqualTo("If the parties fail to agree rates subject to liability and/or other issues pursuant to"
                               + " the paragraph above, each party may rely upon written evidence by way of witness"
                               + " statement of one witness to provide evidence of basic hire rates available within"
                               + " the claimant's geographical location, from a mainstream supplier, or a local"
                               + " reputable supplier if none is available.");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input6")
                .isEqualTo("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date3")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input7")
                .isEqualTo("and the claimant's evidence in reply if so advised to be uploaded by 4pm on");
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("date4")
                .isEqualTo(nextWorkingDayDate.toString());
            assertThat(response.getData()).extracting("smallClaimsCreditHire").extracting("input11")
                .isEqualTo("This witness statement is limited to 10 pages per party, including any appendices.");

            assertThat(response.getData()).extracting("smallClaimsRoadTrafficAccident").extracting("input")
                .isEqualTo("Photographs and/or a plan of the accident location shall be prepared and agreed by the "
                               + "parties and uploaded to the Digital Portal no later than 21 days before the "
                               + "hearing.");
            assertThat(response.getData()).extracting("disposalHearingHearingTime").extracting("input")
                .isEqualTo("This claim will be listed for final disposal before a judge on the first available date "
                               + "after");
            assertThat(response.getData()).extracting("disposalHearingHearingTime").extracting("dateTo")
                .isEqualTo(LocalDate.now().plusWeeks(16).toString());
            assertThat(response.getData()).extracting("disposalOrderWithoutHearing").extracting("input")
                .isEqualTo(String.format(
                    "This order has been made without hearing. "
                        + "Each party has the right to apply to have this Order set aside or varied. "
                        + "Any such application must be received by the Court (together with the "
                        + "appropriate fee) by 4pm on %s.",
                    date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                ));
            assertThat(response.getData()).extracting("fastTrackHearingTime").extracting("helpText1")
                .isEqualTo("If either party considers that the time estimate is insufficient, "
                               + "they must inform the court within 7 days of the date of this order.");
            assertThat(response.getData()).extracting("fastTrackHearingTime").extracting("dateToToggle").isNotNull();
            assertThat(response.getData()).extracting("fastTrackHearingTime").extracting("dateFrom")
                .isEqualTo(LocalDate.now().plusWeeks(22).toString());
            assertThat(response.getData()).extracting("fastTrackHearingTime").extracting("dateTo")
                .isEqualTo(LocalDate.now().plusWeeks(30).toString());
            assertThat(response.getData()).extracting("fastTrackOrderWithoutJudgement").extracting("input")
                .isEqualTo(String.format(
                    "This order has been made without hearing. "
                        + "Each party has the right to apply "
                        + "to have this Order set aside or varied. Any such application must be "
                        + "received by the Court (together with the appropriate fee) by 4pm "
                        + "on %s.",
                    date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                ));
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
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
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
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("smallClaimsFlightDelay").extracting("relatedClaimsInput")
                .isEqualTo("""
                        In the event that the Claimant(s) or Defendant(s) are aware if other\s
                        claims relating to the same flight they must notify the court\s
                        where the claim is being managed within 14 days of receipt of\s
                        this Order providing all relevant details of those claims including\s
                        case number(s), hearing date(s) and copy final substantive order(s)\s
                        if any, to assist the Court with ongoing case management which may\s
                        include the cases being heard together.""");
            assertThat(response.getData()).extracting("smallClaimsFlightDelay").extracting("legalDocumentsInput")
                .isEqualTo("""
                        Any arguments as to the law to be applied to this claim, together with\s
                        copies of legal authorities or precedents relied on, shall be uploaded\s
                        to the Digital Portal not later than 3 full working days before the\s
                        final hearing date.""");

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
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

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
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);

            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
            given(featureToggleService.isCarmEnabledForCase(any())).willReturn(true);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            DynamicListElement optionOne = createDynamicListElement("00001", "court 1 - 1 address - Y01 7RB");
            DynamicListElement optionTwo = createDynamicListElement(preSelectedCourt, "court 2 - 2 address - Y02 7RB");
            DynamicListElement optionThree = createDynamicListElement("00003", "court 3 - 3 address - Y03 7RB");
            DynamicList expected = createDynamicList(List.of(optionOne, optionTwo, optionThree), null);
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

            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
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
            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));

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
                locationRefData("00001", "00001", "court 1", "1 address", "Y01 7RB"),
                locationRefData(preSelectedCourt, preSelectedCourt, "court 2", "2 address", "Y02 7RB"),
                locationRefData("00003", "00003", "court 3", "3 address", "Y03 7RB")
            );
            when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);

            Category category = hearingChannelCategory("INTER", "In Person");
            CategorySearchResult categorySearchResult = categorySearchResult(category);
            when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.of(
                categorySearchResult));
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
            Party applicant1 = new Party();
            applicant1.setType(Party.Type.ORGANISATION);
            applicant1.setIndividualTitle("Mr.");
            applicant1.setIndividualFirstName("Alex");
            applicant1.setIndividualLastName("Richards");
            applicant1.setPartyName("Mr. Alex Richards");
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .respondent1DQWithLocation()
                .applicant1DQWithLocation()
                .build();
            caseData.setApplicant1(applicant1);
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
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1DQWithLocation()
                .applicant1DQWithLocation()
                .setClaimTypeToSpecClaim()
                .atStateClaimDraft()
                .totalClaimAmount(BigDecimal.valueOf(10000))
                .build();
            caseData.setOrderType(OrderType.DECIDE_DAMAGES);
            Party applicant1 = new Party();
            applicant1.setType(Party.Type.ORGANISATION);
            applicant1.setIndividualTitle("Mr.");
            applicant1.setIndividualFirstName("Alex");
            applicant1.setIndividualLastName("Richards");
            applicant1.setPartyName("Mr. Alex Richards");
            caseData.setApplicant1(applicant1);

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
            JudgementSum tempJudgementSum = new JudgementSum();
            tempJudgementSum.setJudgementSum(12.0);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();
            caseData.setDrawDirectionsOrder(tempJudgementSum);

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
        void showReturnError_whenUnspecIntermediateTrack() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setAllocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM);
            caseData.setOrderType(OrderType.DISPOSAL);
            caseData.setCcdState(JUDICIAL_REFERRAL);

            when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsOnly(ERROR_MINTI_DISPOSAL_NOT_ALLOWED);
        }

        @Test
        void showReturnError_whenUnspecMultiTrack() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setAllocatedTrack(AllocatedTrack.MULTI_CLAIM);
            caseData.setOrderType(OrderType.DISPOSAL);
            caseData.setCcdState(JUDICIAL_REFERRAL);

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
                .setClaimTypeToSpecClaim().build();
            caseData.setResponseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name());
            caseData.setOrderType(OrderType.DISPOSAL);
            caseData.setCcdState(JUDICIAL_REFERRAL);

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
                .setClaimTypeToSpecClaim().build();
            caseData.setResponseClaimTrack(AllocatedTrack.MULTI_CLAIM.name());
            caseData.setOrderType(OrderType.DISPOSAL);
            caseData.setCcdState(JUDICIAL_REFERRAL);

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

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void smallClaimsFlagSetToYesPathOne() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void smallClaimsFlagSetToYesPathTwo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
            caseData.setDrawDirectionsOrderSmallClaims(YesOrNo.YES);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
        }

        @Test
        void fastTrackFlagSetToYesPathOne() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setFastClaims(List.of(FastTrack.fastClaimBuildingDispute));

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        }

        @Test
        void fastTrackFlagSetToYesPathTwo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
            caseData.setDrawDirectionsOrderSmallClaims(YesOrNo.NO);
            caseData.setOrderType(OrderType.DECIDE_DAMAGES);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setFastClaims(List.of(FastTrack.fastClaimBuildingDispute));

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
        }

        @Test
        void fastTRackSdoR2NihlPathTwo() {

            List<FastTrack> fastTrackList = new ArrayList<>();
            fastTrackList.add(FastTrack.fastClaimBuildingDispute);
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
            caseData.setDrawDirectionsOrderSmallClaims(YesOrNo.NO);
            caseData.setOrderType(OrderType.DECIDE_DAMAGES);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setTrialAdditionalDirectionsForFastTrack(fastTrackList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
        }

        @Test
        void fastTrackFlagSetToYesNihlPathOne() {
            List<FastTrack> fastTrackList = new ArrayList<>();
            fastTrackList.add(FastTrack.fastClaimBuildingDispute);
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setFastClaims(fastTrackList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
        }

        @Test
        void smallClaimsSdoR2FlagSetToYesPathOne() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            caseData.setSmallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing));

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("setSmallClaimsFlag").isEqualTo("Yes");
            assertThat(response.getData()).extracting("setFastTrackFlag").isEqualTo("No");
            assertThat(response.getData()).extracting("isSdoR2NewScreen").isEqualTo("Yes");
        }

        @Test
        void smallClaimsSdoR2FlagSetToYesPathTwo() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft().build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
            caseData.setDrawDirectionsOrderSmallClaims(YesOrNo.YES);
            caseData.setDrawDirectionsOrderSmallClaimsAdditionalDirections(List.of(SmallTrack.smallClaimDisputeResolutionHearing));

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
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedDisposalHearingSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOTelephoneHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedDisposalHearingSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedDisposalHearingSDOVideoHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedFastTrackSDOInPersonHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedFastTrackSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedFastTrackSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedFastTrackSDOTelephoneHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedFastTrackSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedFastTrackSDOVideoHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedSmallClaimsSDOInPersonHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedSmallClaimsSDOInPersonHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedSmallClaimsSDOTelephoneHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedSmallClaimsSDOTelephoneHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenStateClaimIssuedSmallClaimsSDOVideoHearing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
                .atStateClaimIssuedSmallClaimsSDOVideoHearing().build();

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("sdoOrderDocument").isNotNull();
        }

        @Test
        void shouldAssignCategoryId_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSdoOrderDocument().getDocumentLink().getCategoryID()).isEqualTo(
                "caseManagementOrders");
        }

        @Test
        void shouldGenerateAndSaveSdoOrder_whenNihl() {
            List<FastTrack> fastTrackList = new ArrayList<>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .atStateClaimDraft()
                .build();
            caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setFastClaims(fastTrackList);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
            when(sdoGeneratorService.generate(any(), any())).thenReturn(order);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getSdoOrderDocument().getDocumentLink().getCategoryID()).isEqualTo(
                "caseManagementOrders");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldValidateFieldsForNihl(boolean valid) {
            List<FastTrack> fastTrackList = new ArrayList<>();
            fastTrackList.add(FastTrack.fastClaimNoiseInducedHearingLoss);

            LocalDate testDate = valid ? LocalDate.now().plusDays(1) : LocalDate.now();

            SdoR2DisclosureOfDocuments disclosureOfDocuments = new SdoR2DisclosureOfDocuments();
            disclosureOfDocuments.setStandardDisclosureDate(testDate);
            disclosureOfDocuments.setInspectionDate(testDate);

            SdoR2AddendumReport addendumReport = new SdoR2AddendumReport();
            addendumReport.setSdoAddendumReportDate(testDate);

            SdoR2FurtherAudiogram furtherAudiogram = new SdoR2FurtherAudiogram();
            furtherAudiogram.setSdoClaimantShallUndergoDate(testDate);
            furtherAudiogram.setSdoServiceReportDate(testDate);

            SdoR2ApplicationToRelyOnFurtherDetails relyDetails = new SdoR2ApplicationToRelyOnFurtherDetails();
            relyDetails.setApplicationToRelyDetailsDate(testDate);

            SdoR2ApplicationToRelyOnFurther applicationToRely = new SdoR2ApplicationToRelyOnFurther();
            applicationToRely.setApplicationToRelyOnFurtherDetails(relyDetails);

            SdoR2QuestionsClaimantExpert questionsClaimant = new SdoR2QuestionsClaimantExpert();
            questionsClaimant.setSdoDefendantMayAskDate(testDate);
            questionsClaimant.setSdoQuestionsShallBeAnsweredDate(testDate);
            questionsClaimant.setSdoApplicationToRelyOnFurther(applicationToRely);

            SdoR2PermissionToRelyOnExpert permissionToRely = new SdoR2PermissionToRelyOnExpert();
            permissionToRely.setSdoPermissionToRelyOnExpertDate(testDate);
            permissionToRely.setSdoJointMeetingOfExpertsDate(testDate);

            SdoR2EvidenceAcousticEngineer evidenceAcousticEngineer = new SdoR2EvidenceAcousticEngineer();
            evidenceAcousticEngineer.setSdoInstructionOfTheExpertDate(testDate);
            evidenceAcousticEngineer.setSdoExpertReportDate(testDate);
            evidenceAcousticEngineer.setSdoWrittenQuestionsDate(testDate);
            evidenceAcousticEngineer.setSdoRepliesDate(testDate);

            SdoR2QuestionsToEntExpert questionsToEntExpert = new SdoR2QuestionsToEntExpert();
            questionsToEntExpert.setSdoQuestionsShallBeAnsweredDate(testDate);
            questionsToEntExpert.setSdoWrittenQuestionsDate(testDate);

            SdoR2ScheduleOfLoss scheduleOfLoss = new SdoR2ScheduleOfLoss();
            scheduleOfLoss.setSdoR2ScheduleOfLossClaimantDate(testDate);
            scheduleOfLoss.setSdoR2ScheduleOfLossDefendantDate(testDate);

            SdoR2TrialFirstOpenDateAfter trialFirstOpenDateAfter = new SdoR2TrialFirstOpenDateAfter();
            trialFirstOpenDateAfter.setListFrom(testDate);

            SdoR2TrialWindow trialWindow = new SdoR2TrialWindow();
            trialWindow.setListFrom(testDate);
            trialWindow.setDateTo(testDate);

            SdoR2Trial sdoR2Trial = new SdoR2Trial();
            sdoR2Trial.setSdoR2TrialFirstOpenDateAfter(trialFirstOpenDateAfter);
            sdoR2Trial.setSdoR2TrialWindow(trialWindow);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();
            caseData.setDrawDirectionsOrderRequired(NO);
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);
            caseData.setFastClaims(fastTrackList);
            caseData.setSdoR2DisclosureOfDocuments(disclosureOfDocuments);
            caseData.setSdoR2AddendumReport(addendumReport);
            caseData.setSdoR2FurtherAudiogram(furtherAudiogram);
            caseData.setSdoR2QuestionsClaimantExpert(questionsClaimant);
            caseData.setSdoR2PermissionToRelyOnExpert(permissionToRely);
            caseData.setSdoR2EvidenceAcousticEngineer(evidenceAcousticEngineer);
            caseData.setSdoR2QuestionsToEntExpert(questionsToEntExpert);
            caseData.setSdoR2ScheduleOfLoss(scheduleOfLoss);
            caseData.setSdoR2Trial(sdoR2Trial);
            caseData.setSdoR2ImportantNotesDate(testDate);
            caseData.setSdoR2WitnessesOfFact(createWitnessesOfFact(testDate, valid));

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
                .build();
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            caseData.setDrawDirectionsOrderRequired(NO);
            caseData.setSmallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing));

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);
            CaseDocument order = createCaseDocument();
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

            String header = sdoNarrativeService.buildConfirmationHeader(caseData);
            String body = sdoNarrativeService.buildConfirmationBody(caseData);

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

            String header = sdoNarrativeService.buildConfirmationHeader(caseData);
            String body = sdoNarrativeService.buildConfirmationBody(caseData);

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

            String header = sdoNarrativeService.buildConfirmationHeader(caseData);
            String body = sdoNarrativeService.buildConfirmationBody(caseData);

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
                .build();
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("The number entered cannot be less than zero");
        }

        @Test
        void shouldThrowErrorWhenEnteringNegativeNumberOfWitnessFastTrack() {

            CaseData caseData = CaseDataBuilder.builder()
                .atFastTrackWitnessOfFactWithNegativeInputs()
                .build();
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("The number entered cannot be less than zero");
        }

        @Test
        void shouldNotThrowErrorWhenEnteringPositiveNumberOfWitnessSmallClaim() {

            CaseData caseData = CaseDataBuilder.builder()
                .atSmallClaimsWitnessStatementWithPositiveInputs()
                .build();
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotThrowErrorWhenEnteringPositiveNumberOfWitnessFastTrack() {

            CaseData caseData = CaseDataBuilder.builder()
                .atFastTrackWitnessOfFactWithPositiveInputs()
                .build();
            caseData.setClaimsTrack(ClaimsTrack.fastTrack);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isEmpty();

        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldValidateDRHFields(boolean valid) {
            LocalDate testDate = valid ? LocalDate.now().plusDays(1) : LocalDate.now().minusDays(2);
            SdoR2SmallClaimsPPI smallClaimsPPI = new SdoR2SmallClaimsPPI();
            smallClaimsPPI.setPpiDate(testDate);

            SdoR2SmallClaimsHearingFirstOpenDateAfter firstOpenDateAfter = new SdoR2SmallClaimsHearingFirstOpenDateAfter();
            firstOpenDateAfter.setListFrom(testDate);

            SdoR2SmallClaimsHearing smallClaimsHearing = new SdoR2SmallClaimsHearing();
            smallClaimsHearing.setTrialOnOptions(HearingOnRadioOptions.OPEN_DATE);
            smallClaimsHearing.setSdoR2SmallClaimsHearingFirstOpenDateAfter(firstOpenDateAfter);

            SdoR2SmallClaimsImpNotes impNotes = new SdoR2SmallClaimsImpNotes();
            impNotes.setDate(testDate);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build();
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            caseData.setDrawDirectionsOrderRequired(NO);
            caseData.setSmallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing));
            caseData.setSdoR2SmallClaimsPPI(smallClaimsPPI);
            caseData.setSdoR2SmallClaimsHearing(smallClaimsHearing);
            caseData.setSdoR2SmallClaimsImpNotes(impNotes);
            caseData.setSdoR2SmallClaimsWitnessStatements(createWitnessStatements(valid));

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
            SdoR2SmallClaimsImpNotes impNotes = new SdoR2SmallClaimsImpNotes();
            impNotes.setDate(LocalDate.now().plusDays(2));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build();
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            caseData.setDrawDirectionsOrderRequired(NO);
            caseData.setSmallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing));
            caseData.setSdoR2SmallClaimsImpNotes(impNotes);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldThrowValidationErrorForDRHHearingWindow(boolean valid) {
            LocalDate testDate = valid ? LocalDate.now().plusDays(1) : LocalDate.now().minusDays(2);
            SdoR2SmallClaimsHearingWindow hearingWindow = new SdoR2SmallClaimsHearingWindow();
            hearingWindow.setListFrom(testDate);
            hearingWindow.setDateTo(testDate);

            SdoR2SmallClaimsHearing hearing = new SdoR2SmallClaimsHearing();
            hearing.setTrialOnOptions(HearingOnRadioOptions.HEARING_WINDOW);
            hearing.setSdoR2SmallClaimsHearingWindow(hearingWindow);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build();
            caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);
            caseData.setDrawDirectionsOrderRequired(NO);
            caseData.setSmallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing));
            caseData.setSdoR2SmallClaimsHearing(hearing);

            CallbackParams params = callbackParamsOf(CallbackVersion.V_1, caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            if (valid) {
                assertThat(response.getErrors()).isEmpty();
            } else {
                assertThat(response.getErrors()).hasSize(2);
            }
        }
    }

    private Category hearingChannelCategory(String key, String valueEn) {
        Category category = new Category();
        category.setCategoryKey("HearingChannel");
        category.setKey(key);
        category.setValueEn(valueEn);
        category.setActiveFlag("Y");
        return category;
    }

    private CategorySearchResult categorySearchResult(Category category) {
        CategorySearchResult result = new CategorySearchResult();
        result.setCategories(List.of(category));
        return result;
    }

    private LocationRefData locationRefData(
        String epimmsId,
        String courtLocationCode,
        String siteName,
        String courtAddress,
        String postcode
    ) {
        LocationRefData location = new LocationRefData();
        location.setEpimmsId(epimmsId);
        location.setCourtLocationCode(courtLocationCode);
        location.setSiteName(siteName);
        location.setCourtAddress(courtAddress);
        location.setPostcode(postcode);
        return location;
    }

    private LocationRefData locationRefDataWithRegion() {
        LocationRefData location = new LocationRefData();
        location.setRegionId("region id");
        location.setEpimmsId("epimms id");
        location.setSiteName("site name");
        return location;
    }
}

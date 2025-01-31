package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHireDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsBundleOfDocs;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle.SHOW;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSDOCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_SDO);
    private static final String HEARING_CHANNEL = "HearingChannel";
    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";
    public static final String CONFIRMATION_HEADER = "# Your order has been issued"
        + "%n## Claim number: %s";
    public static final String CONFIRMATION_SUMMARY_1v1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";
    public static final String CONFIRMATION_SUMMARY_2v1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Claimant 2</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";
    public static final String CONFIRMATION_SUMMARY_1v2 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 2</strong>%n"
        + "<br/>%s";
    private static final String UPON_CONSIDERING =
        "Upon considering the claim form, particulars of claim, statements of case and Directions questionnaires";
    public static final String HEARING_TIME_TEXT_AFTER =
        "The claimant must by no later than 4 weeks before the hearing date, pay the court the "
            + "required hearing fee or submit a fully completed application for Help with Fees. \nIf the "
            + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
            + "struck without further order.";

    public static final String FEEDBACK_LINK = "<p>%s"
        + " <a href='https://www.smartsurvey.co.uk/s/QKJTVU//' target=_blank>here</a></p>";

    public static final String ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE = "Date must be in the future";
    public static final String ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO = "The number entered cannot be less than zero";
    public static final String ERROR_MINTI_DISPOSAL_NOT_ALLOWED = "Disposal Hearing is not available for Multi Track and Intermediate Track Claims. "
        + "This can be requested by using the Make an Order event.";

    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;
    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final SdoGeneratorService sdoGeneratorService;
    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper;
    private final AssignCategoryId assignCategoryId;
    private final CategoryService categoryService;
    private final List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);
    private final List<IncludeInOrderToggle> includeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);
    static final String witnessStatementString = "This witness statement is limited to 10 pages per party, including any appendices.";
    static final String laterThanFourPmString = "later than 4pm on";
    static final String claimantEvidenceString = "and the claimant's evidence in reply if so advised to be uploaded by 4pm on";
    @Value("${genApp.lrd.ccmcc.amountPounds}")
    BigDecimal ccmccAmount;
    @Value("${court-location.unspecified-claim.epimms-id}")
    String ccmccEpimsId;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(V_1, ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(MID, "order-details-navigation"), this::setOrderDetailsFlags)
            .put(callbackKey(MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(V_1, MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitSDO)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    // This is currently a mid event but once pre states are defined it should be moved to an about to start event.
    // Once it has been moved to an about to start event the following file will need to be updated:
    //  FlowStateAllowedEventService.java.
    // This way pressing previous on the ccd page won't end up calling this method again and thus
    // repopulating the fields if they have been changed.
    // There is no reason to add conditionals to avoid this here since having it as an about to start event will mean
    // it is only ever called once.
    // Then any changes to fields in ccd will persist in ccd regardless of backwards or forwards page navigation.
    private CallbackResponse prePopulateOrderDetailsPages(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        updatedData
            .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
            .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);

        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            updatedData.showCarmFields(YES);
        } else {
            updatedData.showCarmFields(NO);
        }

        /**
         * Update case management location to preferred logic and return preferred location when legal advisor SDO,
         * otherwise return preferred location only.
         */
        Optional<RequestedCourt> preferredCourt = updateCaseManagementLocationIfLegalAdvisorSdo(updatedData, caseData);

        DynamicList hearingMethodList = getDynamicHearingMethodList(callbackParams, caseData);

        if (V_1.equals(callbackParams.getVersion())) {
            DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream().filter(elem -> elem.getLabel()
                .equals(HearingMethod.IN_PERSON.getLabel())).findFirst().orElse(null);
            hearingMethodList.setValue(hearingMethodInPerson);
            updatedData.hearingMethodValuesFastTrack(hearingMethodList);
            updatedData.hearingMethodValuesDisposalHearing(hearingMethodList);
            updatedData.hearingMethodValuesSmallClaims(hearingMethodList);
        }

        List<LocationRefData> locationRefDataList = getAllLocationFromRefData(callbackParams);
        DynamicList locationsList = getLocationList(preferredCourt.orElse(null), false, locationRefDataList);
        updatedData.disposalHearingMethodInPerson(locationsList);
        updatedData.fastTrackMethodInPerson(locationsList);
        updatedData.smallClaimsMethodInPerson(locationsList);

        List<OrderDetailsPagesSectionsToggle> checkList = List.of(SHOW);
        setCheckList(updatedData, checkList);

        DisposalHearingJudgesRecital tempDisposalHearingJudgesRecital = DisposalHearingJudgesRecital.builder()
            .input(UPON_CONSIDERING)
            .build();

        updatedData.disposalHearingJudgesRecital(tempDisposalHearingJudgesRecital).build();

        updateDeductionValue(caseData, updatedData);

        DisposalHearingDisclosureOfDocuments tempDisposalHearingDisclosureOfDocuments =
            DisposalHearingDisclosureOfDocuments.builder()
                .input1("The parties shall serve on each other copies of the documents upon which reliance is to be"
                            + " placed at the disposal hearing by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .input2("The parties must upload to the Digital Portal copies of those documents which they wish the "
                            + "court to consider when deciding the amount of damages, by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .build();

        updatedData.disposalHearingDisclosureOfDocuments(tempDisposalHearingDisclosureOfDocuments).build();

        DisposalHearingWitnessOfFact tempDisposalHearingWitnessOfFact = DisposalHearingWitnessOfFact.builder()
            .input3("The claimant must upload to the Digital Portal copies of the witness statements of all witnesses"
                        + " of fact on whose evidence reliance is to be placed by 4pm on")
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
            .input4("The provisions of CPR 32.6 apply to such evidence.")
            .input5("Any application by the defendant in relation to CPR 32.7 must be made by 4pm on")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
            .input6("and must be accompanied by proposed directions for allocation and listing for trial on quantum. "
                        + "This is because cross-examination will cause the hearing to exceed the 30-minute "
                        + "maximum time estimate for a disposal hearing.")
            .build();

        updatedData.disposalHearingWitnessOfFact(tempDisposalHearingWitnessOfFact).build();

        DisposalHearingMedicalEvidence tempDisposalHearingMedicalEvidence = DisposalHearingMedicalEvidence.builder()
            .input("The claimant has permission to rely upon the written expert evidence already uploaded to the"
                       + " Digital Portal with the particulars of claim and in addition has permission to rely upon"
                       + " any associated correspondence or updating report which is uploaded to the Digital Portal"
                       + " by 4pm on")
            .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
            .build();

        updatedData.disposalHearingMedicalEvidence(tempDisposalHearingMedicalEvidence).build();

        DisposalHearingQuestionsToExperts tempDisposalHearingQuestionsToExperts = DisposalHearingQuestionsToExperts
            .builder()
            .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
            .build();

        updatedData.disposalHearingQuestionsToExperts(tempDisposalHearingQuestionsToExperts).build();

        DisposalHearingSchedulesOfLoss tempDisposalHearingSchedulesOfLoss = DisposalHearingSchedulesOfLoss.builder()
            .input2("If there is a claim for ongoing or future loss in the original schedule of losses, the claimant"
                        + " must upload to the Digital Portal an up-to-date schedule of loss by 4pm on")
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
            .input3("If the defendant wants to challenge this claim, "
                        + "they must send an up-to-date counter-schedule of loss to the claimant by 4pm on")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
            .input4("If the defendant want to challenge the sums claimed in the schedule of loss they must upload"
                        + " to the Digital Portal an updated counter schedule of loss by 4pm on")
            .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
            .build();

        updatedData.disposalHearingSchedulesOfLoss(tempDisposalHearingSchedulesOfLoss).build();

        DisposalHearingFinalDisposalHearing tempDisposalHearingFinalDisposalHearing =
            DisposalHearingFinalDisposalHearing.builder()
                .input("This claim will be listed for final disposal before a judge on the first available date after")
                .date(LocalDate.now().plusWeeks(16))
                .build();

        updatedData.disposalHearingFinalDisposalHearing(tempDisposalHearingFinalDisposalHearing).build();

        // updated Hearing time field copy of the above field, leaving above field in as requested to not break
        // existing cases
        DisposalHearingHearingTime tempDisposalHearingHearingTime =
            DisposalHearingHearingTime.builder()
                .input(
                    "This claim will be listed for final disposal before a judge on the first available date after")
                .dateTo(LocalDate.now().plusWeeks(16))
                .build();

        updatedData.disposalHearingHearingTime(tempDisposalHearingHearingTime).build();

        DisposalOrderWithoutHearing disposalOrderWithoutHearing = DisposalOrderWithoutHearing.builder()
            .input(String.format(
                "This order has been made without hearing. "
                    + "Each party has the right to apply to have this Order set "
                    + "aside or varied. Any such application must be received "
                    + "by the Court (together with the appropriate fee) "
                    + "by 4pm on %s.",
                deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5)
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
            )).build();
        updatedData.disposalOrderWithoutHearing(disposalOrderWithoutHearing).build();

        DisposalHearingBundle tempDisposalHearingBundle = DisposalHearingBundle.builder()
            .input("At least 7 days before the disposal hearing, the claimant must file and serve")
            .build();

        updatedData.disposalHearingBundle(tempDisposalHearingBundle).build();

        DisposalHearingNotes tempDisposalHearingNotes = DisposalHearingNotes.builder()
            .input("This Order has been made without a hearing. Each party has the right to apply to have this Order"
                       + " set aside or varied. Any such application must be uploaded to the Digital Portal"
                       + " together with the appropriate fee, by 4pm on")
            .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)))
            .build();

        updatedData.disposalHearingNotes(tempDisposalHearingNotes).build();

        FastTrackJudgesRecital tempFastTrackJudgesRecital = FastTrackJudgesRecital.builder()
            .input("Upon considering the statements of case and the information provided by the parties,")
            .build();

        updatedData.fastTrackJudgesRecital(tempFastTrackJudgesRecital).build();

        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
            .input1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on")
            .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
            .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on")
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
            .input3("Requests will be complied with within 7 days of the receipt of the request.")
            .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();

        if (featureToggleService.isSdoR2Enabled()) {
            updatedData.sdoR2FastTrackWitnessOfFact(getSdoR2WitnessOfFact()).build();
        } else {
            updatedData.fastTrackWitnessOfFact(getFastTrackWitnessOfFact()).build();
        }

        FastTrackSchedulesOfLoss tempFastTrackSchedulesOfLoss = FastTrackSchedulesOfLoss.builder()
            .input1("The claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on")
            .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
            .input2("If the defendant wants to challenge this claim, upload to the Digital Portal "
                        + "counter-schedule of loss by 4pm on")
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
            .input3("If there is a claim for future pecuniary loss and the parties have not already set out "
                        + "their case on periodical payments, they must do so in the respective schedule and "
                        + "counter-schedule.")
            .build();

        updatedData.fastTrackSchedulesOfLoss(tempFastTrackSchedulesOfLoss).build();

        FastTrackTrial tempFastTrackTrial = FastTrackTrial.builder()
            .input1("The time provisionally allowed for this trial is")
            .date1(LocalDate.now().plusWeeks(22))
            .date2(LocalDate.now().plusWeeks(30))
            .input2("If either party considers that the time estimate is insufficient, they must inform the court "
                        + "within 7 days of the date stated on this order.")
            .input3("At least 7 days before the trial, the claimant must upload to the Digital Portal")
            .type(Collections.singletonList(FastTrackTrialBundleType.DOCUMENTS))
            .build();

        updatedData.fastTrackTrial(tempFastTrackTrial).build();

        FastTrackHearingTime tempFastTrackHearingTime = FastTrackHearingTime.builder()
            .dateFrom(LocalDate.now().plusWeeks(22))
            .dateTo(LocalDate.now().plusWeeks(30))
            .dateToToggle(dateToShowTrue)
            .helpText1("If either party considers that the time estimate is insufficient, "
                           + "they must inform the court within 7 days of the date of this order.")
            .helpText2("Not more than seven nor less than three clear days before the trial, "
                           + "the claimant must file at court and serve an indexed and paginated bundle of "
                           + "documents which complies with the requirements of Rule 39.5 Civil Procedure Rules "
                           + "and which complies with requirements of PD32. The parties must endeavour to agree "
                           + "the contents of the bundle before it is filed. The bundle will include a case "
                           + "summary and a chronology.")
            .build();
        updatedData.fastTrackHearingTime(tempFastTrackHearingTime);

        FastTrackNotes tempFastTrackNotes = FastTrackNotes.builder()
            .input("This Order has been made without a hearing. Each party has the right to apply to have this Order "
                       + "set aside or varied. Any application must be received by the Court, "
                       + "together with the appropriate fee by 4pm on")
            .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)))
            .build();

        updatedData.fastTrackNotes(tempFastTrackNotes).build();

        FastTrackOrderWithoutJudgement tempFastTrackOrderWithoutJudgement = FastTrackOrderWithoutJudgement.builder()
            .input(String.format(
                "This order has been made without hearing. "
                    + "Each party has the right to apply "
                    + "to have this Order set aside or varied. Any such application must be "
                    + "received by the Court (together with the appropriate fee) by 4pm "
                    + "on %s.",
                deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(LocalDateTime.now())
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
            ))
            .build();

        updatedData.fastTrackOrderWithoutJudgement(tempFastTrackOrderWithoutJudgement);

        FastTrackBuildingDispute tempFastTrackBuildingDispute = FastTrackBuildingDispute.builder()
            .input1("The claimant must prepare a Scott Schedule of the defects, items of damage, "
                        + "or any other relevant matters")
            .input2("The columns should be headed:\n"
                        + "  •  Item\n"
                        + "  •  Alleged defect\n"
                        + "  •  Claimant’s costing\n"
                        + "  •  Defendant’s response\n"
                        + "  •  Defendant’s costing\n"
                        + "  •  Reserved for Judge’s use")
            .input3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns"
                        + " completed by 4pm on")
            .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
            .input4("The defendant must upload to the Digital Portal an amended version of the Scott Schedule "
                        + "with the relevant columns in response completed by 4pm on")
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
            .build();

        updatedData.fastTrackBuildingDispute(tempFastTrackBuildingDispute).build();

        FastTrackClinicalNegligence tempFastTrackClinicalNegligence = FastTrackClinicalNegligence.builder()
            .input1("Documents should be retained as follows:")
            .input2("a) The parties must retain all electronically stored documents relating to the issues in this "
                        + "claim.")
            .input3("b) the defendant must retain the original clinical notes relating to the issues in this claim. "
                        + "The defendant must give facilities for inspection by the claimant, the claimant's legal "
                        + "advisers and experts of these original notes on 7 days written notice.")
            .input4("c) Legible copies of the medical and educational records of the claimant "
                        + "are to be placed in a separate paginated bundle by the claimant's "
                        + "solicitors and kept up to date. All references to medical notes are to be made by reference "
                        + "to the pages in that bundle.")
            .build();

        String partiesLiaseString = "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no ";
        updatedData.fastTrackClinicalNegligence(tempFastTrackClinicalNegligence).build();
        if (featureToggleService.isSdoR2Enabled()) {
            List<AddOrRemoveToggle> addOrRemoveToggleList = List.of(AddOrRemoveToggle.ADD);
            SdoR2FastTrackCreditHireDetails tempSdoR2FastTrackCreditHireDetails = SdoR2FastTrackCreditHireDetails.builder()
                .input2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                            + "a) the need to hire a replacement vehicle; and\n"
                            + "b) impecuniosity")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                            + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                            + "save with permission of the Trial Judge.")
                .input4(partiesLiaseString + laterThanFourPmString)
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
                .build();

            SdoR2FastTrackCreditHire tempSdoR2FastTrackCreditHire = SdoR2FastTrackCreditHire.builder()
                .input1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                            + "disclosure as ordered earlier in this Order must include:\n"
                            + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                            + "commencement of hire until the earlier of:\n "
                            + "     i) 3 months after cessation of hire\n"
                            + "     ii) the repair or replacement of the claimant's vehicle\n"
                            + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
                            + "prior to the commencement of hire until the earlier of:\n"
                            + "     i) 3 months after cessation of hire\n"
                            + "     ii) the repair or replacement of the claimant's vehicle\n"
                            + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.")
                .input5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                            + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                            + "one witness to provide evidence of basic hire rates available within the claimant's "
                            + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                            + "is available.")
                .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .input7(claimantEvidenceString)
                .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .input8(witnessStatementString)
                .detailsShowToggle(addOrRemoveToggleList)
                .sdoR2FastTrackCreditHireDetails(tempSdoR2FastTrackCreditHireDetails)
                .build();

            updatedData.sdoR2FastTrackCreditHire(tempSdoR2FastTrackCreditHire).build();
        }
        FastTrackCreditHire tempFastTrackCreditHire = FastTrackCreditHire.builder()
            .input1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                        + "disclosure as ordered earlier in this Order must include:\n"
                        + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                        + "commencement of hire until the earlier of:\n "
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
                        + "prior to the commencement of hire until the earlier of:\n"
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.")
            .input2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity")
            .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
            .input3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.")
            .input4(partiesLiaseString + laterThanFourPmString)
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
            .input5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                        + "is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .input7(claimantEvidenceString)
            .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
            .input8(witnessStatementString)
            .build();

        updatedData.fastTrackCreditHire(tempFastTrackCreditHire).build();

        FastTrackHousingDisrepair tempFastTrackHousingDisrepair = FastTrackHousingDisrepair.builder()
            .input1("The claimant must prepare a Scott Schedule of the items in disrepair.")
            .input2("The columns should be headed:\n"
                        + "  •  Item\n"
                        + "  •  Alleged disrepair\n"
                        + "  •  Defendant’s response\n"
                        + "  •  Reserved for Judge’s use")
            .input3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant "
                        + "columns completed by 4pm on")
            .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
            .input4("The defendant must upload to the Digital Portal the amended Scott Schedule with the "
                        + "relevant columns in response completed by 4pm on")
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)))
            .build();

        updatedData.fastTrackHousingDisrepair(tempFastTrackHousingDisrepair).build();

        FastTrackPersonalInjury tempFastTrackPersonalInjury = FastTrackPersonalInjury.builder()
            .input1("The claimant has permission to rely upon the written expert evidence already uploaded to "
                        + "the Digital Portal with the particulars of claim and in addition has permission to rely upon"
                        + " any associated correspondence or updating report which is uploaded to the Digital Portal by"
                        + " 4pm on")
            .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
            .input2("Any questions which are to be addressed to an expert must be sent to the expert directly "
                        + "and uploaded to the Digital Portal by 4pm on")
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
            .input3("The answers to the questions shall be answered by the Expert by")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .input4("and uploaded to the Digital Portal by")
            .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .build();

        updatedData.fastTrackPersonalInjury(tempFastTrackPersonalInjury).build();

        FastTrackRoadTrafficAccident tempFastTrackRoadTrafficAccident = FastTrackRoadTrafficAccident.builder()
            .input("Photographs and/or a plan of the accident location shall be prepared and agreed by the "
                       + "parties and uploaded to the Digital Portal by 4pm on")
            .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .build();

        updatedData.fastTrackRoadTrafficAccident(tempFastTrackRoadTrafficAccident).build();

        SmallClaimsJudgesRecital tempSmallClaimsJudgesRecital = SmallClaimsJudgesRecital.builder()
            .input("Upon considering the statements of case and the information provided by the parties,")
            .build();

        updatedData.smallClaimsJudgesRecital(tempSmallClaimsJudgesRecital).build();

        SmallClaimsDocuments tempSmallClaimsDocuments = SmallClaimsDocuments.builder()
            .input1("Each party must upload to the Digital Portal copies of all documents which they wish the court to"
                        + " consider when reaching its decision not less than 21 days before the hearing.")
            .input2("The court may refuse to consider any document which has not been uploaded to the "
                        + "Digital Portal by the above date.")
            .build();

        updatedData.smallClaimsDocuments(tempSmallClaimsDocuments).build();

        if (featureToggleService.isSdoR2Enabled()) {
            SdoR2SmallClaimsWitnessStatements tempSdoR2SmallClaimsWitnessStatements = SdoR2SmallClaimsWitnessStatements
                .builder()
                .sdoStatementOfWitness(
                    "Each party must upload to the Digital Portal copies of all witness statements of the witnesses"
                        + " upon whose evidence they intend to rely at the hearing not less than 21 days before"
                        + " the hearing.")
                .isRestrictWitness(NO)
                .sdoR2SmallClaimsRestrictWitness(SdoR2SmallClaimsRestrictWitness.builder()
                                                     .noOfWitnessClaimant(2)
                                                     .noOfWitnessDefendant(2)
                                                     .partyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT)
                                                     .build())
                .isRestrictPages(NO)
                .sdoR2SmallClaimsRestrictPages(SdoR2SmallClaimsRestrictPages.builder()
                                                   .witnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1)
                                                   .noOfPages(12)
                                                   .fontDetails(RESTRICT_NUMBER_PAGES_TEXT2)
                                                   .build())
                .text(WITNESS_DESCRIPTION_TEXT)
                .build();
            updatedData.sdoR2SmallClaimsWitnessStatementOther(tempSdoR2SmallClaimsWitnessStatements).build();

        } else {
            SmallClaimsWitnessStatement tempSmallClaimsWitnessStatement = SmallClaimsWitnessStatement.builder()
                .smallClaimsNumberOfWitnessesToggle(checkList)
                .input1("Each party must upload to the Digital Portal copies of all witness statements of the witnesses"
                            + " upon whose evidence they intend to rely at the hearing not less than 21 days before"
                            + " the hearing.")
                .input2("2")
                .input3("2")
                .input4("For this limitation, a party is counted as a witness.")
                .text("A witness statement must: \na) Start with the name of the case and the claim number;"
                          + "\nb) State the full name and address of the witness; "
                          + "\nc) Set out the witness's evidence clearly in numbered paragraphs on numbered pages;"
                          + "\nd) End with this paragraph: 'I believe that the facts stated in this witness "
                          + "statement are true. I understand that proceedings for contempt of court may be "
                          + "brought against anyone who makes, or causes to be made, a false statement in a "
                          + "document verified by a statement of truth without an honest belief in its truth'."
                          + "\ne) be signed by the witness and dated."
                          + "\nf) If a witness is unable to read the statement there must be a certificate that "
                          + "it has been read or interpreted to the witness by a suitably qualified person and "
                          + "at the final hearing there must be an independent interpreter who will not be "
                          + "provided by the Court."
                          + "\n\nThe judge may refuse to allow a witness to give evidence or consider any "
                          + "statement of any witness whose statement has not been uploaded to the Digital Portal in "
                          + "accordance with the paragraphs above."
                          + "\n\nA witness whose statement has been uploaded in accordance with the above must attend "
                          + "the hearing. If they do not attend, it will be for the court to decide how much "
                          + "reliance, if any, to place on their evidence.")
                .build();

            updatedData.smallClaimsWitnessStatement(tempSmallClaimsWitnessStatement).build();
        }

        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            updatedData.smallClaimsMediationSectionStatement(SmallClaimsMediation.builder()
                                                                 .input(
                                                                     "If you failed to attend a mediation appointment,"
                                                                         + " then the judge at the hearing may impose a sanction. "
                                                                         + "This could require you to pay costs, or could result in your claim or defence being dismissed. "
                                                                         + "You should deliver to every other party, and to the court, your explanation for non-attendance, "
                                                                         + "with any supporting documents, at least 14 days before the hearing. "
                                                                         + "Any other party who wishes to comment on the failure to attend the mediation appointment should "
                                                                         + "deliver their comments,"
                                                                         + " with any supporting documents, to all parties and to the court at least "
                                                                         + "14 days before the hearing.")
                                                                 .build());
        }

        if (featureToggleService.isSdoR2Enabled()) {
            SmallClaimsFlightDelay tempSmallClaimsFlightDelay = SmallClaimsFlightDelay.builder()
                .smallClaimsFlightDelayToggle(checkList)
                .relatedClaimsInput("In the event that the Claimant(s) or Defendant(s) are aware if other \n"
                                        + "claims relating to the same flight they must notify the court \n"
                                        + "where the claim is being managed within 14 days of receipt of \n"
                                        + "this Order providing all relevant details of those claims including \n"
                                        + "case number(s), hearing date(s) and copy final substantive order(s) \n"
                                        + "if any, to assist the Court with ongoing case management which may \n"
                                        + "include the cases being heard together.")
                .legalDocumentsInput("Any arguments as to the law to be applied to this claim, together with \n"
                                         + "copies of legal authorities or precedents relied on, shall be uploaded \n"
                                         + "to the Digital Portal not later than 3 full working days before the \n"
                                         + "final hearing date.")
                .build();

            updatedData.smallClaimsFlightDelay(tempSmallClaimsFlightDelay).build();
        }

        SmallClaimsHearing tempSmallClaimsHearing = SmallClaimsHearing.builder()
            .input1("The hearing of the claim will be on a date to be notified to you by a separate notification. "
                        + "The hearing will have a time estimate of")
            .input2(HEARING_TIME_TEXT_AFTER)
            .build();

        updatedData.smallClaimsHearing(tempSmallClaimsHearing).build();

        SmallClaimsNotes.SmallClaimsNotesBuilder tempSmallClaimsNotes = SmallClaimsNotes.builder();
        tempSmallClaimsNotes.input("This order has been made without hearing. "
                                       + "Each party has the right to apply to have this Order set aside or varied. "
                                       + "Any such application must be received by the Court "
                                       + "(together with the appropriate fee) by 4pm on "
                                       + DateFormatHelper.formatLocalDate(
            deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5), DATE)
        );

        updatedData.smallClaimsNotes(tempSmallClaimsNotes.build()).build();

        SmallClaimsCreditHire tempSmallClaimsCreditHire = SmallClaimsCreditHire.builder()
            .input1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                        + "disclosure as ordered earlier in this Order must include:\n"
                        + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                        + "commencement of hire until the earlier of:\n "
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
                        + "prior to the commencement of hire until the earlier of:\n"
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.")
            .input2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity")
            .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
            .input3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.")
            .input4(partiesLiaseString + laterThanFourPmString)
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
            .input5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                        + "is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .input7(claimantEvidenceString)
            .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
            .input11(witnessStatementString)
            .build();

        updatedData.smallClaimsCreditHire(tempSmallClaimsCreditHire).build();

        SmallClaimsRoadTrafficAccident tempSmallClaimsRoadTrafficAccident = SmallClaimsRoadTrafficAccident.builder()
            .input("Photographs and/or a plan of the accident location shall be prepared and agreed by the parties"
                       + " and uploaded to the Digital Portal no later than 21 days before the hearing.")
            .build();

        updatedData.smallClaimsRoadTrafficAccident(tempSmallClaimsRoadTrafficAccident).build();

        //This the flow after request for reconsideration
        if (featureToggleService.isSdoR2Enabled() && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(caseData.getDecisionOnRequestReconsiderationOptions())) {
            updatedData.drawDirectionsOrderRequired(null);
            updatedData.drawDirectionsOrderSmallClaims(null);
            updatedData.fastClaims(null);
            updatedData.smallClaims(null);
            updatedData.claimsTrack(null);
            updatedData.orderType(null);
            updatedData.trialAdditionalDirectionsForFastTrack(null);
            updatedData.drawDirectionsOrderSmallClaimsAdditionalDirections(null);
            updatedData.fastTrackAllocation(FastTrackAllocation.builder().assignComplexityBand(null).build());
            updatedData.disposalHearingAddNewDirections(null);
            updatedData.smallClaimsAddNewDirections(null);
            updatedData.fastTrackAddNewDirections(null);
            updatedData.sdoHearingNotes(null);
            updatedData.fastTrackHearingNotes(null);
            updatedData.disposalHearingHearingNotes(null);
            updatedData.sdoR2SmallClaimsHearing(null);
            updatedData.sdoR2SmallClaimsUploadDoc(null);
            updatedData.sdoR2SmallClaimsPPI(null);
            updatedData.sdoR2SmallClaimsImpNotes(null);
            updatedData.sdoR2SmallClaimsWitnessStatements(null);
            updatedData.sdoR2SmallClaimsHearingToggle(null);
            updatedData.sdoR2SmallClaimsJudgesRecital(null);
            updatedData.sdoR2SmallClaimsWitnessStatementsToggle(null);
            updatedData.sdoR2SmallClaimsPPIToggle(null);
            updatedData.sdoR2SmallClaimsUploadDocToggle(null);
        }

        if (featureToggleService.isSdoR2Enabled()) {
            updateExpertEvidenceFields(updatedData);
            updateDisclosureOfDocumentFields(updatedData);
            populateDRHFields(callbackParams, updatedData, preferredCourt, hearingMethodList, locationRefDataList);
            prePopulateNihlFields(updatedData, hearingMethodList, preferredCourt, locationRefDataList);
            List<IncludeInOrderToggle> localIncludeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);
            setCheckListNihl(updatedData, localIncludeInOrderToggle);
            updatedData.sdoR2FastTrackUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
                SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
            updatedData.sdoR2SmallClaimsUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
                SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
            updatedData.sdoR2DisposalHearingUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
                SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private DynamicList getDynamicHearingMethodList(CallbackParams callbackParams, CaseData caseData) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String serviceId = caseData.getCaseAccessCategory().equals(CaseCategory.SPEC_CLAIM)
            ? SPEC_SERVICE_ID : UNSPEC_SERVICE_ID;
        Optional<CategorySearchResult> categorySearchResult = categoryService.findCategoryByCategoryIdAndServiceId(
            authToken, HEARING_CHANNEL, serviceId
        );
        DynamicList hearingMethodList = HearingMethodUtils.getHearingMethodList(categorySearchResult.orElse(null));
        List<DynamicListElement> hearingMethodListWithoutNotInAttendance = hearingMethodList
            .getListItems()
            .stream()
            .filter(elem -> !elem.getLabel().equals(HearingMethod.NOT_IN_ATTENDANCE.getLabel()))
            .toList();
        hearingMethodList.setListItems(hearingMethodListWithoutNotInAttendance);
        return hearingMethodList;
    }

    private FastTrackWitnessOfFact getFastTrackWitnessOfFact() {
        return FastTrackWitnessOfFact.builder()
            .input1("Each party must upload to the Digital Portal copies of the statements of all witnesses of "
                        + "fact on whom they intend to rely.")
            .input2("3")
            .input3("3")
            .input4("For this limitation, a party is counted as a witness.")
            .input5("Each witness statement should be no more than")
            .input6("10")
            .input7("A4 pages. Statements should be double spaced using a font size of 12.")
            .input8("Witness statements shall be uploaded to the Digital Portal by 4pm on")
            .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .input9("Evidence will not be permitted at trial from a witness whose statement has not been uploaded "
                        + "in accordance with this Order. Evidence not uploaded, or uploaded late, will not be "
                        + "permitted except with permission from the Court.")
            .build();
    }

    private static SdoR2WitnessOfFact getSdoR2WitnessOfFact() {
        return SdoR2WitnessOfFact.builder()
            .sdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS)
            .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                      .isRestrictWitness(NO)
                                      .restrictNoOfWitnessDetails(
                                          SdoR2RestrictNoOfWitnessDetails.builder()
                                              .noOfWitnessClaimant(3)
                                              .noOfWitnessDefendant(3)
                                              .partyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT)
                                              .build())
                                      .build())
            .sdoRestrictPages(SdoR2RestrictPages.builder()
                                  .isRestrictPages(NO)
                                  .restrictNoOfPagesDetails(
                                      SdoR2RestrictNoOfPagesDetails.builder()
                                          .witnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1)
                                          .noOfPages(12)
                                          .fontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2)
                                          .build())
                                  .build())
            .sdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE)
            .sdoWitnessDeadlineDate(LocalDate.now().plusDays(70))
            .sdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE)
            .build();
    }

    private void updateExpertEvidenceFields(CaseData.CaseDataBuilder<?, ?> updatedData) {
        FastTrackPersonalInjury tempFastTrackPersonalInjury = FastTrackPersonalInjury.builder()
            .input1("The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                        + " Digital Portal with the particulars of claim")
            .input2("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert " +
                        "directly and uploaded to the Digital Portal by 4pm on")
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(14)))
            .input3("The answers to the questions shall be answered by the Expert by")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(42)))
            .input4("and uploaded to the Digital Portal by the party who has asked the question by")
            .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(49)))
            .build();

        updatedData.fastTrackPersonalInjury(tempFastTrackPersonalInjury).build();
    }

    private void updateDisclosureOfDocumentFields(CaseData.CaseDataBuilder<?, ?> updatedData) {
        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
            .input1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on")
            .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
            .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on")
            .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(5)))
            .input3("Requests will be complied with within 7 days of the receipt of the request.")
            .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();
    }

    private void populateDRHFields(CallbackParams callbackParams,
                                   CaseData.CaseDataBuilder<?, ?> updatedData, Optional<RequestedCourt> preferredCourt,
                                   DynamicList hearingMethodList, List<LocationRefData> locationRefDataList) {
        DynamicList courtList = getCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        courtList.setValue(courtList.getListItems().get(0));

        DynamicListElement hearingMethodTelephone = hearingMethodList.getListItems().stream().filter(elem -> elem.getLabel()
            .equals(HearingMethod.TELEPHONE.getLabel())).findFirst().orElse(null);
        hearingMethodList.setValue(hearingMethodTelephone);

        updatedData.sdoR2SmallClaimsJudgesRecital(SdoR2SmallClaimsJudgesRecital.builder().input(
            SdoR2UiConstantSmallClaim.JUDGE_RECITAL).build());
        updatedData.sdoR2SmallClaimsPPI(SdoR2SmallClaimsPPI.builder().ppiDate(LocalDate.now().plusDays(21)).text(
            SdoR2UiConstantSmallClaim.PPI_DESCRIPTION).build());
        updatedData.sdoR2SmallClaimsUploadDoc(SdoR2SmallClaimsUploadDoc.builder().sdoUploadOfDocumentsTxt(
            SdoR2UiConstantSmallClaim.UPLOAD_DOC_DESCRIPTION).build());
        updatedData.sdoR2SmallClaimsWitnessStatements(SdoR2SmallClaimsWitnessStatements.builder()
                                                          .sdoStatementOfWitness(SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT)
                                                          .isRestrictWitness(NO)
                                                          .isRestrictPages(NO)
                                                          .sdoR2SmallClaimsRestrictWitness(
                                                              SdoR2SmallClaimsRestrictWitness
                                                                  .builder()
                                                                  .partyIsCountedAsWitnessTxt(SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT)
                                                                  .build())
                                                          .sdoR2SmallClaimsRestrictPages(SdoR2SmallClaimsRestrictPages.builder()
                                                                                             .fontDetails(
                                                                                                 SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2)
                                                                                             .noOfPages(12)
                                                                                             .witnessShouldNotMoreThanTxt(
                                                                                                 SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1)
                                                                                             .build())
                                                          .text(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT).build());
        updatedData.sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                                                .trialOnOptions(HearingOnRadioOptions.OPEN_DATE)
                                                .methodOfHearing(hearingMethodList)
                                                .lengthList(SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES)
                                                .physicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY)
                                                .sdoR2SmallClaimsHearingFirstOpenDateAfter(
                                                    SdoR2SmallClaimsHearingFirstOpenDateAfter.builder()
                                                        .listFrom(LocalDate.now().plusDays(56)).build())
                                                .sdoR2SmallClaimsHearingWindow(SdoR2SmallClaimsHearingWindow.builder().dateTo(
                                                        LocalDate.now().plusDays(70))
                                                                                   .listFrom(LocalDate.now().plusDays(56)).build())
                                                .hearingCourtLocationList(courtList)
                                                .altHearingCourtLocationList(getLocationList(
                                                    preferredCourt.orElse(null),
                                                    true,
                                                    locationRefDataList
                                                ))
                                                .sdoR2SmallClaimsBundleOfDocs(SdoR2SmallClaimsBundleOfDocs.builder()
                                                                                  .physicalBundlePartyTxt(
                                                                                      SdoR2UiConstantSmallClaim.BUNDLE_TEXT).build()).build());
        updatedData.sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder()
                                                 .text(SdoR2UiConstantSmallClaim.IMP_NOTES_TEXT)
                                                 .date(LocalDate.now().plusDays(7)).build());
        updatedData.sdoR2SmallClaimsUploadDocToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsHearingToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsWitnessStatementsToggle(includeInOrderToggle);
        updatedData.sdoR2DrhUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());

        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            updatedData.sdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
            updatedData.sdoR2SmallClaimsMediationSectionStatement(SdoR2SmallClaimsMediation.builder()
                                                                      .input(SdoR2UiConstantSmallClaim.CARM_MEDIATION_TEXT)
                                                                      .build());
        }
    }

    private void prePopulateNihlFields(CaseData.CaseDataBuilder<?, ?> updatedData, DynamicList hearingMethodList,
                                       Optional<RequestedCourt> preferredCourt,
                                       List<LocationRefData> locationRefDataList) {

        DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream().filter(elem -> elem.getLabel()
            .equals(HearingMethod.IN_PERSON.getLabel())).findFirst().orElse(null);
        hearingMethodList.setValue(hearingMethodInPerson);
        updatedData.sdoFastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                                                  .input(SdoR2UiConstantFastTrack.JUDGE_RECITAL).build());
        updatedData.sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                                                   .standardDisclosureTxt(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE)
                                                   .standardDisclosureDate(LocalDate.now().plusDays(28))
                                                   .inspectionTxt(SdoR2UiConstantFastTrack.INSPECTION)
                                                   .inspectionDate(LocalDate.now().plusDays(42))
                                                   .requestsWillBeCompiledLabel(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH)
                                                   .build());
        updatedData.sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                                             .sdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS)
                                             .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                                                       .isRestrictWitness(NO)
                                                                       .restrictNoOfWitnessDetails(
                                                                           SdoR2RestrictNoOfWitnessDetails
                                                                               .builder()
                                                                               .noOfWitnessClaimant(3).noOfWitnessDefendant(
                                                                                   3)
                                                                               .partyIsCountedAsWitnessTxt(
                                                                                   SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT)
                                                                               .build())
                                                                       .build())
                                             .sdoRestrictPages(SdoR2RestrictPages.builder()
                                                                   .isRestrictPages(NO)
                                                                   .restrictNoOfPagesDetails(
                                                                       SdoR2RestrictNoOfPagesDetails.builder()
                                                                           .witnessShouldNotMoreThanTxt(
                                                                               SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1)
                                                                           .noOfPages(12)
                                                                           .fontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2)
                                                                           .build()).build())
                                             .sdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE)
                                             .sdoWitnessDeadlineDate(LocalDate.now().plusDays(70))
                                             .sdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE)
                                             .build());
        updatedData.sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder().sdoR2ScheduleOfLossClaimantText(
                SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT)
                                            .isClaimForPecuniaryLoss(NO)
                                            .sdoR2ScheduleOfLossClaimantDate(LocalDate.now().plusDays(364))
                                            .sdoR2ScheduleOfLossDefendantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT)
                                            .sdoR2ScheduleOfLossDefendantDate(LocalDate.now().plusDays(378))
                                            .sdoR2ScheduleOfLossPecuniaryLossTxt(SdoR2UiConstantFastTrack.PECUNIARY_LOSS)
                                            .build());
        updatedData.sdoR2Trial(SdoR2Trial.builder()
                                   .trialOnOptions(TrialOnRadioOptions.OPEN_DATE)
                                   .lengthList(FastTrackHearingTimeEstimate.FIVE_HOURS)
                                   .methodOfHearing(hearingMethodList)
                                   .physicalBundleOptions(PhysicalTrialBundleOptions.PARTY)
                                   .sdoR2TrialFirstOpenDateAfter(
                                       SdoR2TrialFirstOpenDateAfter.builder()
                                           .listFrom(LocalDate.now().plusDays(434)).build())
                                   .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                                                         .listFrom(LocalDate.now().plusDays(434))
                                                         .dateTo(LocalDate.now().plusDays(455))
                                                         .build())
                                   .hearingCourtLocationList(DynamicList.builder()
                                                                 .listItems(getCourtLocationForSdoR2(
                                                                     preferredCourt
                                                                         .orElse(null),
                                                                     locationRefDataList
                                                                 ).getListItems())
                                                                 .value(getCourtLocationForSdoR2(
                                                                     preferredCourt
                                                                         .orElse(null),
                                                                     locationRefDataList
                                                                 ).getListItems().get(0)).build())

                                   .altHearingCourtLocationList(getAlternativeCourtLocationsForNihl(locationRefDataList))
                                   .physicalBundlePartyTxt(SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE)
                                   .build());

        updatedData.sdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        updatedData.sdoR2ImportantNotesDate(LocalDate.now().plusDays(7));

        updatedData.sdoR2ExpertEvidence(SdoR2ExpertEvidence.builder()
                                            .sdoClaimantPermissionToRelyTxt(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY).build());
        updatedData.sdoR2AddendumReport(SdoR2AddendumReport.builder()
                                            .sdoAddendumReportTxt(SdoR2UiConstantFastTrack.ADDENDUM_REPORT)
                                            .sdoAddendumReportDate(LocalDate.now().plusDays(56)).build());
        updatedData.sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder()
                                              .sdoClaimantShallUndergoTxt(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO)
                                              .sdoServiceReportTxt(SdoR2UiConstantFastTrack.SERVICE_REPORT)
                                              .sdoClaimantShallUndergoDate(LocalDate.now().plusDays(42))
                                              .sdoServiceReportDate(LocalDate.now().plusDays(98)).build());
        updatedData.sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                                                     .sdoDefendantMayAskTxt(SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK)
                                                     .sdoDefendantMayAskDate(LocalDate.now().plusDays(126))
                                                     .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED)
                                                     .sdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(147))
                                                     .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL)
                                                     .sdoApplicationToRelyOnFurther(
                                                         SdoR2ApplicationToRelyOnFurther.builder()
                                                             .doRequireApplicationToRely(NO)
                                                             .applicationToRelyOnFurtherDetails(
                                                                 SdoR2ApplicationToRelyOnFurtherDetails.builder()
                                                                     .applicationToRelyDetailsTxt(
                                                                         SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS)
                                                                     .applicationToRelyDetailsDate(LocalDate.now().plusDays(
                                                                         161)).build()).build())
                                                     .build());
        updatedData.sdoR2PermissionToRelyOnExpert(SdoR2PermissionToRelyOnExpert.builder()
                                                      .sdoPermissionToRelyOnExpertTxt(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT)
                                                      .sdoPermissionToRelyOnExpertDate(LocalDate.now().plusDays(119))
                                                      .sdoJointMeetingOfExpertsTxt(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS)
                                                      .sdoJointMeetingOfExpertsDate(LocalDate.now().plusDays(147))
                                                      .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS)
                                                      .build());
        updatedData.sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
                                                      .sdoEvidenceAcousticEngineerTxt(SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER)
                                                      .sdoInstructionOfTheExpertTxt(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT)
                                                      .sdoInstructionOfTheExpertDate(LocalDate.now().plusDays(42))
                                                      .sdoInstructionOfTheExpertTxtArea(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA)
                                                      .sdoExpertReportTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT)
                                                      .sdoExpertReportDate(LocalDate.now().plusDays(280))
                                                      .sdoExpertReportDigitalPortalTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL)
                                                      .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS)
                                                      .sdoWrittenQuestionsDate(LocalDate.now().plusDays(294))
                                                      .sdoWrittenQuestionsDigitalPortalTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL)
                                                      .sdoRepliesTxt(SdoR2UiConstantFastTrack.REPLIES)
                                                      .sdoRepliesDate(LocalDate.now().plusDays(315))
                                                      .sdoRepliesDigitalPortalTxt(SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL)
                                                      .sdoServiceOfOrderTxt(SdoR2UiConstantFastTrack.SERVICE_OF_ORDER)
                                                      .build());
        updatedData.sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                                                  .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS)
                                                  .sdoWrittenQuestionsDate(LocalDate.now().plusDays(336))
                                                  .sdoWrittenQuestionsDigPortalTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL)
                                                  .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED)
                                                  .sdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(350))
                                                  .sdoShallBeUploadedTxt(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED)
                                                  .build());
        updatedData.sdoR2UploadOfDocuments(SdoR2UploadOfDocuments.builder()
                                               .sdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS)
                                               .build());
        updatedData.sdoR2NihlUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());

    }

    private void updateDeductionValue(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(d -> d + "%")
            .ifPresent(deductionPercentage -> {
                DisposalHearingJudgementDeductionValue tempDisposalHearingJudgementDeductionValue =
                    DisposalHearingJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.disposalHearingJudgementDeductionValue(tempDisposalHearingJudgementDeductionValue);

                FastTrackJudgementDeductionValue tempFastTrackJudgementDeductionValue =
                    FastTrackJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.fastTrackJudgementDeductionValue(tempFastTrackJudgementDeductionValue).build();

                SmallClaimsJudgementDeductionValue tempSmallClaimsJudgementDeductionValue =
                    SmallClaimsJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.smallClaimsJudgementDeductionValue(tempSmallClaimsJudgementDeductionValue).build();
            });
    }

    private List<LocationRefData> getAllLocationFromRefData(CallbackParams callbackParams) {
        return locationRefDataService.getHearingCourtLocations(
            callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    /**
     * Creates the dynamic list for the hearing location, pre-selecting the preferred court if possible.
     *
     * @param preferredCourt (optional) preferred court if any
     * @param locations      locations from refdata
     * @return dynamic list, with a value selected if appropriate and possible
     */
    private DynamicList getLocationList(RequestedCourt preferredCourt, boolean getAllCourts,
                                        List<LocationRefData> locations) {
        DynamicList locationsList;
        Optional<LocationRefData> matchingLocation = Optional.ofNullable(preferredCourt)
            .flatMap(requestedCourt -> locationHelper.getMatching(locations, preferredCourt));

        if (featureToggleService.isSdoR2Enabled() && getAllCourts) {
            //for SDOR2 we need to display all court in alternative court locations
            matchingLocation = Optional.empty();
        }
        if (matchingLocation.isPresent()) {
            locationsList = DynamicList.fromList(locations,
                                                 this::getLocationEpimms,
                                                 LocationReferenceDataService::getDisplayEntry,
                                                 matchingLocation.get(),
                                                 true
            );
        } else {
            locationsList = DynamicList.fromList(locations,
                                                 this::getLocationEpimms,
                                                 LocationReferenceDataService::getDisplayEntry,
                                                 null,
                                                 true
            );
        }
        return locationsList;
    }

    private DynamicList getAlternativeCourtLocationsForNihl(List<LocationRefData> locations) {

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();

        locations.stream().forEach(loc -> dynamicListOptions.add(
            dynamicElementFromCode(loc.getEpimmsId(), LocationReferenceDataService.getDisplayEntry(loc))));
        return DynamicList.fromDynamicListElementList(dynamicListOptions);
    }

    private DynamicList getCourtLocationForSdoR2(RequestedCourt preferredCourt,
                                                 List<LocationRefData> locations) {
        Optional<LocationRefData> matchingLocation = Optional.ofNullable(preferredCourt)
            .flatMap(requestedCourt -> locationHelper.getMatching(locations, preferredCourt));

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        if (matchingLocation.isPresent()) {
            dynamicListOptions.add(dynamicElementFromCode(
                matchingLocation.get().getEpimmsId(),
                LocationReferenceDataService.getDisplayEntry(matchingLocation.get())
            ));
        }
        dynamicListOptions.add(dynamicElementFromCode("OTHER_LOCATION", "Other location"));
        return DynamicList.fromDynamicListElementList(dynamicListOptions);
    }

    private String getLocationEpimms(LocationRefData location) {
        return location.getEpimmsId();
    }

    private CallbackResponse setOrderDetailsFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        if (isMultiOrIntermediateTrackClaim(caseData)
            && OrderType.DISPOSAL.equals(caseData.getOrderType())
            && CaseState.JUDICIAL_REFERRAL.equals(caseData.getCcdState())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_MINTI_DISPOSAL_NOT_ALLOWED))
                .build();
        }

        updateDeductionValue(caseData, updatedData);

        updatedData.setSmallClaimsFlag(NO).build();
        updatedData.setFastTrackFlag(NO).build();

        if (featureToggleService.isSdoR2Enabled()) {
            updatedData.isSdoR2NewScreen(NO).build();
        }

        if (SdoHelper.isSmallClaimsTrack(caseData)) {
            updatedData.setSmallClaimsFlag(YES).build();
            if (featureToggleService.isSdoR2Enabled() && SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)) {
                updatedData.isSdoR2NewScreen(YES).build();
            }
        } else if (SdoHelper.isFastTrack(caseData)) {
            updatedData.setFastTrackFlag(YES).build();
            if (featureToggleService.isSdoR2Enabled() && SdoHelper.isNihlFastTrack(caseData)) {
                updatedData.isSdoR2NewScreen(YES).build();
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private ArrayList<String> validateFieldsNihl(CaseData caseData) {
        ArrayList<String> errors = new ArrayList<>();
        if (caseData.getSdoR2DisclosureOfDocuments() != null && caseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureDate() != null) {
            validateFutureDate(caseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2DisclosureOfDocuments() != null && caseData.getSdoR2DisclosureOfDocuments().getInspectionDate() != null) {
            validateFutureDate(caseData.getSdoR2DisclosureOfDocuments().getInspectionDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2WitnessesOfFact() != null && caseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineDate() != null) {
            validateFutureDate(caseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2AddendumReport() != null && caseData.getSdoR2AddendumReport().getSdoAddendumReportDate() != null) {
            validateFutureDate(caseData.getSdoR2AddendumReport().getSdoAddendumReportDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2FurtherAudiogram() != null && caseData.getSdoR2FurtherAudiogram().getSdoClaimantShallUndergoDate() != null) {
            validateFutureDate(caseData.getSdoR2FurtherAudiogram().getSdoClaimantShallUndergoDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2FurtherAudiogram() != null && caseData.getSdoR2FurtherAudiogram().getSdoServiceReportDate() != null) {
            validateFutureDate(caseData.getSdoR2FurtherAudiogram().getSdoServiceReportDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2QuestionsClaimantExpert() != null && caseData.getSdoR2QuestionsClaimantExpert().getSdoDefendantMayAskDate() != null) {
            validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert().getSdoDefendantMayAskDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2QuestionsClaimantExpert() != null && caseData.getSdoR2QuestionsClaimantExpert().getSdoQuestionsShallBeAnsweredDate() != null) {
            validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert().getSdoQuestionsShallBeAnsweredDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2QuestionsClaimantExpert() != null && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails().getApplicationToRelyDetailsDate() != null) {
            validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert()
                                   .getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails().getApplicationToRelyDetailsDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2PermissionToRelyOnExpert() != null && caseData.getSdoR2PermissionToRelyOnExpert().getSdoPermissionToRelyOnExpertDate() != null) {
            validateFutureDate(caseData.getSdoR2PermissionToRelyOnExpert().getSdoPermissionToRelyOnExpertDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2PermissionToRelyOnExpert() != null && caseData.getSdoR2PermissionToRelyOnExpert().getSdoJointMeetingOfExpertsDate() != null) {
            validateFutureDate(caseData.getSdoR2PermissionToRelyOnExpert().getSdoJointMeetingOfExpertsDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null && caseData.getSdoR2EvidenceAcousticEngineer().getSdoInstructionOfTheExpertDate() != null) {
            validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoInstructionOfTheExpertDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null && caseData.getSdoR2EvidenceAcousticEngineer().getSdoExpertReportDate() != null) {
            validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoExpertReportDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null && caseData.getSdoR2EvidenceAcousticEngineer().getSdoWrittenQuestionsDate() != null) {
            validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoWrittenQuestionsDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null && caseData.getSdoR2EvidenceAcousticEngineer().getSdoRepliesDate() != null) {
            validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoRepliesDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2QuestionsToEntExpert() != null && caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate() != null) {
            validateFutureDate(caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2QuestionsToEntExpert() != null && caseData.getSdoR2QuestionsToEntExpert().getSdoQuestionsShallBeAnsweredDate() != null) {
            validateFutureDate(caseData.getSdoR2QuestionsToEntExpert().getSdoQuestionsShallBeAnsweredDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2ScheduleOfLoss() != null && caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossClaimantDate() != null) {
            validateFutureDate(caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossClaimantDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2ScheduleOfLoss() != null && caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantDate() != null) {
            validateFutureDate(caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2Trial() != null && caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter() != null
            && caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter().getListFrom() != null) {
            validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter().getListFrom())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2Trial() != null && caseData.getSdoR2Trial().getSdoR2TrialWindow() != null && caseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom() != null) {
            validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2Trial() != null && caseData.getSdoR2Trial().getSdoR2TrialWindow() != null && caseData.getSdoR2Trial().getSdoR2TrialWindow().getDateTo() != null) {
            validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getDateTo())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2ImportantNotesDate() != null) {
            validateFutureDate(caseData.getSdoR2ImportantNotesDate())
                .ifPresent(errors::add);
        }

        if (caseData.getSdoR2WitnessesOfFact() != null && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessClaimant() != null) {
            validateGreaterOrEqualZero(caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessClaimant())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2WitnessesOfFact() != null && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessDefendant() != null) {
            validateGreaterOrEqualZero(caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessDefendant())
                .ifPresent(errors::add);
        }

        return errors;
    }

    private Optional<String> validateFutureDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isAfter(today)) {
            return Optional.empty();
        }
        return Optional.of(ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE);
    }

    private Optional<String> validateFutureDate(LocalDate date, LocalDate today) {
        if (date.isAfter(today)) {
            return Optional.empty();
        }
        return Optional.of(ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE);
    }

    private Optional<String> validateGreaterOrEqualZero(Integer quantity) {
        if (quantity < 0) {
            return Optional.of(ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
        }
        return Optional.empty();
    }

    private CallbackResponse generateSdoOrder(CallbackParams callbackParams) {
        CaseData caseData = V_1.equals(callbackParams.getVersion())
            ? mapHearingMethodFields(callbackParams.getCaseData())
            : callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        List<String> errors = new ArrayList<>();
        if (nonNull(caseData.getSmallClaimsWitnessStatement())) {
            String inputValue1 = caseData.getSmallClaimsWitnessStatement().getInput2();
            String inputValue2 = caseData.getSmallClaimsWitnessStatement().getInput3();
            final String witnessValidationErrorMessage = validateNegativeWitness(inputValue1, inputValue2);
            if (!witnessValidationErrorMessage.isEmpty()) {
                errors.add(witnessValidationErrorMessage);
            }
        } else if (nonNull(caseData.getFastTrackWitnessOfFact())) {
            String inputValue1 = caseData.getFastTrackWitnessOfFact().getInput2();
            String inputValue2 = caseData.getFastTrackWitnessOfFact().getInput3();
            final String witnessValidationErrorMessage = validateNegativeWitness(inputValue1, inputValue2);
            if (!witnessValidationErrorMessage.isEmpty()) {
                errors.add(witnessValidationErrorMessage);
            }
        } else if (featureToggleService.isSdoR2Enabled() && SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)) {
            errors.addAll(validateDRHFields(caseData));
        }

        if (featureToggleService.isSdoR2Enabled() && SdoHelper.isNihlFastTrack(caseData)) {
            List<String> errorsNihl;
            errorsNihl = validateFieldsNihl(caseData);
            if (!errorsNihl.isEmpty()) {
                errors.addAll(errorsNihl);
            }
        }

        if (errors.isEmpty()) {
            CaseDocument document = sdoGeneratorService.generate(
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            if (document != null) {
                updatedData.sdoOrderDocument(document);
            }
            assignCategoryId.assignCategoryIdToCaseDocument(document, "caseManagementOrders");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private List<String> validateDRHFields(CaseData caseData) {
        ArrayList<String> errors = new ArrayList<>();
        LocalDate today = LocalDate.now();
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsPPI()) && Objects.nonNull(caseData.getSdoR2SmallClaimsPPI().getPpiDate())) {
            validateFutureDate(caseData.getSdoR2SmallClaimsPPI().getPpiDate(), today).ifPresent(errors::add);
        }
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsWitnessStatements()) && caseData.getSdoR2SmallClaimsWitnessStatements().getIsRestrictWitness() == YES
            && nonNull(caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessClaimant())) {
            validateGreaterThanZero(caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessClaimant()).ifPresent(
                errors::add);
        }
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsWitnessStatements()) && caseData.getSdoR2SmallClaimsWitnessStatements().getIsRestrictWitness() == YES
            && nonNull(caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessDefendant())) {
            validateGreaterThanZero(caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessDefendant()).ifPresent(
                errors::add);
        }
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsHearing()) && caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions() == HearingOnRadioOptions.OPEN_DATE) {
            validateFutureDate(
                caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingFirstOpenDateAfter().getListFrom(),
                today
            ).ifPresent(errors::add);
        }
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsHearing()) && caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions() == HearingOnRadioOptions.HEARING_WINDOW) {
            validateFutureDate(
                caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getDateTo(),
                today
            ).ifPresent(errors::add);
        }
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsHearing()) && caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions() == HearingOnRadioOptions.HEARING_WINDOW) {
            validateFutureDate(
                caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getListFrom(),
                today
            ).ifPresent(errors::add);
        }
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsImpNotes())) {
            validateFutureDate(caseData.getSdoR2SmallClaimsImpNotes().getDate(), today).ifPresent(errors::add);
        }
        return errors;
    }

    private CaseData mapHearingMethodFields(CaseData caseData) {
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        if (caseData.getHearingMethodValuesDisposalHearing() != null
            && caseData.getHearingMethodValuesDisposalHearing().getValue() != null) {
            String disposalHearingMethodLabel = caseData.getHearingMethodValuesDisposalHearing().getValue().getLabel();
            if (disposalHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodVideoConferenceHearing);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesFastTrack() != null
            && caseData.getHearingMethodValuesFastTrack().getValue() != null) {
            String fastTrackHearingMethodLabel = caseData.getHearingMethodValuesFastTrack().getValue().getLabel();
            if (fastTrackHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodVideoConferenceHearing);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesSmallClaims() != null
            && caseData.getHearingMethodValuesSmallClaims().getValue() != null) {
            String smallClaimsHearingMethodLabel = caseData.getHearingMethodValuesSmallClaims().getValue().getLabel();
            if (smallClaimsHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodTelephoneHearing);
            }
        }

        return updatedData.build();
    }

    private CallbackResponse submitSDO(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> dataBuilder = getSharedData(callbackParams);

        CaseData caseData = callbackParams.getCaseData();

        CaseDocument document = caseData.getSdoOrderDocument();
        if (document != null) {
            List<Element<CaseDocument>> generatedDocuments = callbackParams.getCaseData()
                .getSystemGeneratedCaseDocuments();
            generatedDocuments.add(element(document));
            dataBuilder.systemGeneratedCaseDocuments(generatedDocuments);
        }
        // null/remove preview SDO document, otherwise it will show as duplicate within case file view
        dataBuilder.sdoOrderDocument(null);

        dataBuilder.hearingNotes(getHearingNotes(caseData));

        // LiP check ensures any LiP cases will always create takeCaseOffline WA task until CP goes live
        boolean isLipCase = caseData.isApplicantLiP() || caseData.isRespondent1LiP() || caseData.isRespondent2LiP();
        boolean isLocationWhiteListed = featureToggleService.isLocationWhiteListedForCaseProgression(caseData.getCaseManagementLocation().getBaseLocation());

        if (!isLipCase) {
            log.info("Case {} is whitelisted for case progression.", caseData.getCcdCaseReference());
            dataBuilder.eaCourtLocation(YES);
            dataBuilder.hmcEaCourtLocation(!isLipCase && isLocationWhiteListed ? YES : NO);
        } else if (isLipCaseWithProgressionEnabledAndCourtWhiteListed(caseData)) {
            dataBuilder.eaCourtLocation(YesOrNo.YES);
        } else {
            log.info("Case {} is NOT whitelisted for case progression.", caseData.getCcdCaseReference());
            dataBuilder.eaCourtLocation(NO);
        }

        dataBuilder.disposalHearingMethodInPerson(deleteLocationList(
            caseData.getDisposalHearingMethodInPerson()));
        dataBuilder.fastTrackMethodInPerson(deleteLocationList(
            caseData.getFastTrackMethodInPerson()));
        dataBuilder.smallClaimsMethodInPerson(deleteLocationList(
            caseData.getSmallClaimsMethodInPerson()));
        if (featureToggleService.isSdoR2Enabled() && SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)
            && caseData.getSdoR2SmallClaimsHearing() != null) {
            dataBuilder.sdoR2SmallClaimsHearing(updateHearingAfterDeletingLocationList(caseData.getSdoR2SmallClaimsHearing()));
        }
        setClaimsTrackBasedOnJudgeSelection(dataBuilder, caseData);

        // Avoid location lists (listItems) from being saved in caseData, just save the selected values
        if (featureToggleService.isSdoR2Enabled() && caseData.getSdoR2Trial() != null) {
            SdoR2Trial sdoR2Trial = caseData.getSdoR2Trial();
            if (caseData.getSdoR2Trial().getHearingCourtLocationList() != null) {
                sdoR2Trial.setHearingCourtLocationList(DynamicList.builder().value(
                    caseData.getSdoR2Trial().getHearingCourtLocationList().getValue()).build());
            }
            if (caseData.getSdoR2Trial().getAltHearingCourtLocationList() != null) {
                sdoR2Trial.setAltHearingCourtLocationList(DynamicList.builder().value(
                    caseData.getSdoR2Trial().getAltHearingCourtLocationList().getValue()).build());
            }
            dataBuilder.sdoR2Trial(sdoR2Trial);
        }

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                dataBuilder
            ));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private boolean isLipCaseWithProgressionEnabledAndCourtWhiteListed(CaseData caseData) {
        return (caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne())
            && featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation());
    }

    private SdoR2SmallClaimsHearing updateHearingAfterDeletingLocationList(SdoR2SmallClaimsHearing sdoR2SmallClaimsHearing) {
        SdoR2SmallClaimsHearing updatedSdoR2SmallClaimsHearing = sdoR2SmallClaimsHearing;
        if (sdoR2SmallClaimsHearing.getHearingCourtLocationList() != null) {
            updatedSdoR2SmallClaimsHearing.setHearingCourtLocationList(deleteLocationList(sdoR2SmallClaimsHearing.getHearingCourtLocationList()));
        }
        if (sdoR2SmallClaimsHearing.getAltHearingCourtLocationList() != null) {
            updatedSdoR2SmallClaimsHearing.setAltHearingCourtLocationList(deleteLocationList(sdoR2SmallClaimsHearing.getAltHearingCourtLocationList()));
        }
        return updatedSdoR2SmallClaimsHearing;
    }

    // During SDO the claim track can change based on judges selection. In this case we want to update claims track
    // to this decision, or maintain it, if it was not changed.
    private void setClaimsTrackBasedOnJudgeSelection(CaseData.CaseDataBuilder<?, ?> dataBuilder, CaseData caseData) {
        CaseCategory caseAccessCategory = caseData.getCaseAccessCategory();
        switch (caseAccessCategory) {
            case UNSPEC_CLAIM:// unspec use allocatedTrack to hold claims track value
                if (SdoHelper.isSmallClaimsTrack(caseData)) {
                    dataBuilder.allocatedTrack(SMALL_CLAIM);
                } else if (SdoHelper.isFastTrack(caseData)) {
                    dataBuilder.allocatedTrack(FAST_CLAIM);
                }
                break;
            case SPEC_CLAIM:// spec claims use responseClaimTrack to hold claims track value
                if (SdoHelper.isSmallClaimsTrack(caseData)) {
                    dataBuilder.responseClaimTrack(SMALL_CLAIM.name());
                } else if (SdoHelper.isFastTrack(caseData)) {
                    dataBuilder.responseClaimTrack(FAST_CLAIM.name());
                }
                break;
            default:
                break;
        }
    }

    private boolean sdoSubmittedPreCPForLiPCase(CaseData caseData) {
        return !featureToggleService.isCaseProgressionEnabled()
            && (caseData.isRespondent1LiP() || caseData.isRespondent2LiP() || caseData.isApplicantNotRepresented());
    }

    private DynamicList deleteLocationList(DynamicList list) {
        if (isNull(list)) {
            return null;
        }
        return DynamicList.builder().value(list.getValue()).build();
    }

    private boolean nonNull(Object object) {
        return object != null;
    }

    private Optional<String> validateGreaterThanZero(int count) {
        if (count < 0) {
            return Optional.of(ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
        }
        return Optional.empty();
    }

    private String validateNegativeWitness(String inputValue1, String inputValue2) {
        final String errorMessage = "";
        if (inputValue1 != null && inputValue2 != null) {
            int number1 = Integer.parseInt(inputValue1);
            int number2 = Integer.parseInt(inputValue2);
            if (number1 < 0 || number2 < 0) {
                return ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO;
            }
        }
        return errorMessage;
    }

    private CaseData.CaseDataBuilder<?, ?> getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> dataBuilder = caseData.toBuilder();

        dataBuilder.businessProcess(BusinessProcess.ready(CREATE_SDO));

        return dataBuilder;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getHeader(CaseData caseData) {
        return format(
            CONFIRMATION_HEADER,
            caseData.getLegacyCaseReference()
        );
    }

    private String getBody(CaseData caseData) {
        String applicant1Name = caseData.getApplicant1().getPartyName();
        String respondent1Name = caseData.getRespondent1().getPartyName();
        Party applicant2 = caseData.getApplicant2();
        Party respondent2 = caseData.getRespondent2();

        String initialBody = format(
            CONFIRMATION_SUMMARY_1v1,
            applicant1Name,
            respondent1Name
        );

        if (applicant2 != null) {
            initialBody = format(
                CONFIRMATION_SUMMARY_2v1,
                applicant1Name,
                applicant2.getPartyName(),
                respondent1Name
            );
        } else if (respondent2 != null) {
            initialBody = format(
                CONFIRMATION_SUMMARY_1v2,
                applicant1Name,
                respondent1Name,
                respondent2.getPartyName()
            );
        }
        return initialBody + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");
    }

    private void setCheckList(
        CaseData.CaseDataBuilder<?, ?> updatedData,
        List<OrderDetailsPagesSectionsToggle> checkList
    ) {
        updatedData.fastTrackAltDisputeResolutionToggle(checkList);
        updatedData.fastTrackVariationOfDirectionsToggle(checkList);
        updatedData.fastTrackSettlementToggle(checkList);
        updatedData.fastTrackDisclosureOfDocumentsToggle(checkList);
        updatedData.fastTrackWitnessOfFactToggle(checkList);
        updatedData.fastTrackSchedulesOfLossToggle(checkList);
        updatedData.fastTrackCostsToggle(checkList);
        updatedData.fastTrackTrialToggle(checkList);
        updatedData.fastTrackMethodToggle(checkList);
        updatedData.disposalHearingDisclosureOfDocumentsToggle(checkList);
        updatedData.disposalHearingWitnessOfFactToggle(checkList);
        updatedData.disposalHearingMedicalEvidenceToggle(checkList);
        updatedData.disposalHearingQuestionsToExpertsToggle(checkList);
        updatedData.disposalHearingSchedulesOfLossToggle(checkList);
        updatedData.disposalHearingFinalDisposalHearingToggle(checkList);
        updatedData.disposalHearingMethodToggle(checkList);
        updatedData.disposalHearingBundleToggle(checkList);
        updatedData.disposalHearingClaimSettlingToggle(checkList);
        updatedData.disposalHearingCostsToggle(checkList);
        updatedData.smallClaimsHearingToggle(checkList);
        updatedData.smallClaimsMethodToggle(checkList);
        updatedData.smallClaimsDocumentsToggle(checkList);
        updatedData.smallClaimsWitnessStatementToggle(checkList);
        if (featureToggleService.isSdoR2Enabled()) {
            updatedData.smallClaimsFlightDelayToggle(checkList);
        }
        if (featureToggleService.isCarmEnabledForCase(updatedData.build())) {
            updatedData.smallClaimsMediationSectionToggle(checkList);
        }
    }

    private void setCheckListNihl(
        CaseData.CaseDataBuilder<?, ?> updatedData,
        List<IncludeInOrderToggle> includeInOrderToggle
    ) {
        updatedData.sdoAltDisputeResolution(SdoR2FastTrackAltDisputeResolution.builder().includeInOrderToggle(
            includeInOrderToggle).build());
        updatedData.sdoVariationOfDirections(SdoR2VariationOfDirections.builder().includeInOrderToggle(
            includeInOrderToggle).build());
        updatedData.sdoR2Settlement(SdoR2Settlement.builder().includeInOrderToggle(includeInOrderToggle).build());
        updatedData.sdoR2DisclosureOfDocumentsToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorWitnessesOfFactToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorExpertEvidenceToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorAddendumReportToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorFurtherAudiogramToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorQuestionsClaimantExpertToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorPermissionToRelyOnExpertToggle(includeInOrderToggle);
        updatedData.sdoR2SeparatorEvidenceAcousticEngineerToggle(includeInOrderToggle);
        updatedData.sdoR2SeparatorQuestionsToEntExpertToggle(includeInOrderToggle);
        updatedData.sdoR2ScheduleOfLossToggle(includeInOrderToggle);
        updatedData.sdoR2SeparatorUploadOfDocumentsToggle(includeInOrderToggle);
        updatedData.sdoR2TrialToggle(includeInOrderToggle);
        if (featureToggleService.isCarmEnabledForCase(updatedData.build())) {
            updatedData.sdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
        }
    }

    private Optional<RequestedCourt> updateCaseManagementLocationIfLegalAdvisorSdo(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        Optional<RequestedCourt> preferredCourt;
        if (isSpecClaim1000OrLessAndCcmcc(ccmccAmount).test(caseData)) {
            preferredCourt = locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData, true);
            preferredCourt.map(RequestedCourt::getCaseLocation)
                .ifPresent(updatedData::caseManagementLocation);
            return preferredCourt;
        } else {
            return locationHelper.getCaseManagementLocation(caseData);
        }
    }

    public Predicate<CaseData> isSpecClaim1000OrLessAndCcmcc(BigDecimal ccmccAmount) {
        return caseData ->
            caseData.getCaseAccessCategory().equals(CaseCategory.SPEC_CLAIM)
                && ccmccAmount.compareTo(caseData.getTotalClaimAmount()) >= 0
                && caseData.getCaseManagementLocation().getBaseLocation().equals(ccmccEpimsId);
    }

    private boolean isMultiOrIntermediateTrackClaim(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && (AllocatedTrack.INTERMEDIATE_CLAIM.equals(caseData.getAllocatedTrack())
            || AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())
            || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }

}

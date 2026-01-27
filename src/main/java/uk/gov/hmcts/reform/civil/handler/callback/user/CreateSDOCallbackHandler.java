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
import uk.gov.hmcts.reform.civil.model.sdo.PPI;
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
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
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

    public static final String DEFAULT_PENAL_NOTICE = """
        PENAL NOTICE

        WARNING

        [DEFENDANT] IF YOU DO NOT COMPLY WITH THIS ORDER YOU MAY BE HELD IN CONTEMPT OF COURT AND PUNISHED BY A FINE, \
        IMPRISONMENT, CONFISCATION OF ASSETS OR OTHER PUNISHMENT UNDER THE LAW.

        A penal notice against the Defendant is attached to paragraph X below.""";

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
        caseData.setSmallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson);
        caseData.setFastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);

        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            caseData.setShowCarmFields(YES);
        } else {
            caseData.setShowCarmFields(NO);
        }

        if (featureToggleService.isWelshEnabledForMainCase()
            && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual())) {
            caseData.setBilingualHint(YesOrNo.YES);
        }

        /**
         * Update case management location to preferred logic and return preferred location when legal advisor SDO,
         * otherwise return preferred location only.
         */
        Optional<RequestedCourt> preferredCourt = updateCaseManagementLocationIfLegalAdvisorSdo(callbackParams, caseData);

        DynamicList hearingMethodList = getDynamicHearingMethodList(callbackParams, caseData);

        if (V_1.equals(callbackParams.getVersion())) {
            DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream().filter(elem -> elem.getLabel()
                .equals(HearingMethod.IN_PERSON.getLabel())).findFirst().orElse(null);
            hearingMethodList.setValue(hearingMethodInPerson);
            caseData.setHearingMethodValuesFastTrack(hearingMethodList);
            caseData.setHearingMethodValuesDisposalHearing(hearingMethodList);
            caseData.setHearingMethodValuesSmallClaims(hearingMethodList);
        }

        List<LocationRefData> locationRefDataList = getAllLocationFromRefData(callbackParams);
        DynamicList locationsList = getLocationList(preferredCourt.orElse(null), false, locationRefDataList);
        caseData.setDisposalHearingMethodInPerson(locationsList);
        caseData.setFastTrackMethodInPerson(locationsList);
        caseData.setSmallClaimsMethodInPerson(locationsList);

        List<OrderDetailsPagesSectionsToggle> checkList = List.of(SHOW);
        setCheckList(caseData, checkList);

        DisposalHearingJudgesRecital tempDisposalHearingJudgesRecital = new DisposalHearingJudgesRecital();
        tempDisposalHearingJudgesRecital.setInput(UPON_CONSIDERING);

        caseData.setDisposalHearingJudgesRecital(tempDisposalHearingJudgesRecital);

        updateDeductionValue(caseData);

        DisposalHearingDisclosureOfDocuments tempDisposalHearingDisclosureOfDocuments = new DisposalHearingDisclosureOfDocuments();
        tempDisposalHearingDisclosureOfDocuments.setInput1("The parties shall serve on each other copies of the documents upon which reliance is to be"
                            + " placed at the disposal hearing by 4pm on");
        tempDisposalHearingDisclosureOfDocuments.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        tempDisposalHearingDisclosureOfDocuments.setInput2("The parties must upload to the Digital Portal copies of those documents which they wish the "
                            + "court to consider when deciding the amount of damages, by 4pm on");
        tempDisposalHearingDisclosureOfDocuments.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));

        caseData.setDisposalHearingDisclosureOfDocuments(tempDisposalHearingDisclosureOfDocuments);

        DisposalHearingWitnessOfFact tempDisposalHearingWitnessOfFact = new DisposalHearingWitnessOfFact();
        tempDisposalHearingWitnessOfFact.setInput3("The claimant must upload to the Digital Portal copies of the witness statements of all witnesses"
                        + " of fact on whose evidence reliance is to be placed by 4pm on");
        tempDisposalHearingWitnessOfFact.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        tempDisposalHearingWitnessOfFact.setInput4("The provisions of CPR 32.6 apply to such evidence.");
        tempDisposalHearingWitnessOfFact.setInput5("Any application by the defendant in relation to CPR 32.7 must be made by 4pm on");
        tempDisposalHearingWitnessOfFact.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)));
        tempDisposalHearingWitnessOfFact.setInput6("and must be accompanied by proposed directions for allocation and listing for trial on quantum. "
                        + "This is because cross-examination will cause the hearing to exceed the 30-minute "
                        + "maximum time estimate for a disposal hearing.");

        caseData.setDisposalHearingWitnessOfFact(tempDisposalHearingWitnessOfFact);

        DisposalHearingMedicalEvidence tempDisposalHearingMedicalEvidence = new DisposalHearingMedicalEvidence();
        tempDisposalHearingMedicalEvidence.setInput("The claimant has permission to rely upon the written expert evidence already uploaded to the"
                       + " Digital Portal with the particulars of claim and in addition has permission to rely upon"
                       + " any associated correspondence or updating report which is uploaded to the Digital Portal"
                       + " by 4pm on");
        tempDisposalHearingMedicalEvidence.setDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));

        caseData.setDisposalHearingMedicalEvidence(tempDisposalHearingMedicalEvidence);

        DisposalHearingQuestionsToExperts tempDisposalHearingQuestionsToExperts = new DisposalHearingQuestionsToExperts();
        tempDisposalHearingQuestionsToExperts.setDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)));

        caseData.setDisposalHearingQuestionsToExperts(tempDisposalHearingQuestionsToExperts);

        DisposalHearingSchedulesOfLoss tempDisposalHearingSchedulesOfLoss = new DisposalHearingSchedulesOfLoss();
        tempDisposalHearingSchedulesOfLoss.setInput2("If there is a claim for ongoing or future loss in the original schedule of losses, the claimant"
                        + " must upload to the Digital Portal an up-to-date schedule of loss by 4pm on");
        tempDisposalHearingSchedulesOfLoss.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        tempDisposalHearingSchedulesOfLoss.setInput3("If the defendant wants to challenge this claim, "
                        + "they must send an up-to-date counter-schedule of loss to the claimant by 4pm on");
        tempDisposalHearingSchedulesOfLoss.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));
        tempDisposalHearingSchedulesOfLoss.setInput4("If the defendant want to challenge the sums claimed in the schedule of loss they must upload"
                        + " to the Digital Portal an updated counter schedule of loss by 4pm on");
        tempDisposalHearingSchedulesOfLoss.setDate4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));

        caseData.setDisposalHearingSchedulesOfLoss(tempDisposalHearingSchedulesOfLoss);

        DisposalHearingFinalDisposalHearing tempDisposalHearingFinalDisposalHearing = new DisposalHearingFinalDisposalHearing();
        tempDisposalHearingFinalDisposalHearing.setInput("This claim will be listed for final disposal before a judge on the first available date after");
        tempDisposalHearingFinalDisposalHearing.setDate(LocalDate.now().plusWeeks(16));

        caseData.setDisposalHearingFinalDisposalHearing(tempDisposalHearingFinalDisposalHearing);

        // updated Hearing time field copy of the above field, leaving above field in as requested to not break
        // existing cases
        DisposalHearingHearingTime tempDisposalHearingHearingTime = new DisposalHearingHearingTime();
        tempDisposalHearingHearingTime.setInput(
                    "This claim will be listed for final disposal before a judge on the first available date after");
        tempDisposalHearingHearingTime.setDateTo(LocalDate.now().plusWeeks(16));

        caseData.setDisposalHearingHearingTime(tempDisposalHearingHearingTime);

        DisposalOrderWithoutHearing disposalOrderWithoutHearing = new DisposalOrderWithoutHearing();
        disposalOrderWithoutHearing.setInput(String.format(
                "This order has been made without hearing. "
                    + "Each party has the right to apply to have this Order set "
                    + "aside or varied. Any such application must be received "
                    + "by the Court (together with the appropriate fee) "
                    + "by 4pm on %s.",
                deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5)
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
            ));
        caseData.setDisposalOrderWithoutHearing(disposalOrderWithoutHearing);

        DisposalHearingBundle tempDisposalHearingBundle = new DisposalHearingBundle();
        tempDisposalHearingBundle.setInput("At least 7 days before the disposal hearing, the claimant must file and serve");

        caseData.setDisposalHearingBundle(tempDisposalHearingBundle);

        DisposalHearingNotes tempDisposalHearingNotes = new DisposalHearingNotes();
        tempDisposalHearingNotes.setInput("This Order has been made without a hearing. Each party has the right to apply to have this Order"
                       + " set aside or varied. Any such application must be uploaded to the Digital Portal"
                       + " together with the appropriate fee, by 4pm on");
        tempDisposalHearingNotes.setDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)));

        caseData.setDisposalHearingNotes(tempDisposalHearingNotes);

        FastTrackJudgesRecital tempFastTrackJudgesRecital = new FastTrackJudgesRecital();
        tempFastTrackJudgesRecital.setInput("Upon considering the statements of case and the information provided by the parties,");

        caseData.setFastTrackJudgesRecital(tempFastTrackJudgesRecital);

        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = new FastTrackDisclosureOfDocuments();
        tempFastTrackDisclosureOfDocuments.setInput1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on");
        tempFastTrackDisclosureOfDocuments.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        tempFastTrackDisclosureOfDocuments.setInput2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on");
        tempFastTrackDisclosureOfDocuments.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)));
        tempFastTrackDisclosureOfDocuments.setInput3("Requests will be complied with within 7 days of the receipt of the request.");
        tempFastTrackDisclosureOfDocuments.setInput4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on");
        tempFastTrackDisclosureOfDocuments.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));

        caseData.setFastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments);
        caseData.setSdoR2FastTrackWitnessOfFact(getSdoR2WitnessOfFact());

        FastTrackSchedulesOfLoss tempFastTrackSchedulesOfLoss = new FastTrackSchedulesOfLoss();
        tempFastTrackSchedulesOfLoss.setInput1("The claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on");
        tempFastTrackSchedulesOfLoss.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        tempFastTrackSchedulesOfLoss.setInput2("If the defendant wants to challenge this claim, upload to the Digital Portal "
                        + "counter-schedule of loss by 4pm on");
        tempFastTrackSchedulesOfLoss.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));
        tempFastTrackSchedulesOfLoss.setInput3("If there is a claim for future pecuniary loss and the parties have not already set out "
                        + "their case on periodical payments, they must do so in the respective schedule and "
                        + "counter-schedule.");

        caseData.setFastTrackSchedulesOfLoss(tempFastTrackSchedulesOfLoss);

        FastTrackTrial tempFastTrackTrial = new FastTrackTrial();
        tempFastTrackTrial.setInput1("The time provisionally allowed for this trial is");
        tempFastTrackTrial.setDate1(LocalDate.now().plusWeeks(22));
        tempFastTrackTrial.setDate2(LocalDate.now().plusWeeks(30));
        tempFastTrackTrial.setInput2("If either party considers that the time estimate is insufficient, they must inform the court "
                        + "within 7 days of the date stated on this order.");
        tempFastTrackTrial.setInput3("At least 7 days before the trial, the claimant must upload to the Digital Portal");
        tempFastTrackTrial.setType(Collections.singletonList(FastTrackTrialBundleType.DOCUMENTS));

        caseData.setFastTrackTrial(tempFastTrackTrial);

        FastTrackHearingTime tempFastTrackHearingTime = new FastTrackHearingTime();
        tempFastTrackHearingTime.setDateFrom(LocalDate.now().plusWeeks(22));
        tempFastTrackHearingTime.setDateTo(LocalDate.now().plusWeeks(30));
        tempFastTrackHearingTime.setDateToToggle(dateToShowTrue);
        tempFastTrackHearingTime.setHelpText1("If either party considers that the time estimate is insufficient, "
                           + "they must inform the court within 7 days of the date of this order.");
        caseData.setFastTrackHearingTime(tempFastTrackHearingTime);

        FastTrackNotes tempFastTrackNotes = new FastTrackNotes();
        tempFastTrackNotes.setInput("This Order has been made without a hearing. Each party has the right to apply to have this Order "
                       + "set aside or varied. Any application must be received by the Court, "
                       + "together with the appropriate fee by 4pm on");
        tempFastTrackNotes.setDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(1)));

        caseData.setFastTrackNotes(tempFastTrackNotes);

        FastTrackOrderWithoutJudgement tempFastTrackOrderWithoutJudgement = new FastTrackOrderWithoutJudgement();
        tempFastTrackOrderWithoutJudgement.setInput(String.format(
                "This order has been made without hearing. "
                    + "Each party has the right to apply "
                    + "to have this Order set aside or varied. Any such application must be "
                    + "received by the Court (together with the appropriate fee) by 4pm "
                    + "on %s.",
                deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(LocalDateTime.now())
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
            ));

        caseData.setFastTrackOrderWithoutJudgement(tempFastTrackOrderWithoutJudgement);

        FastTrackBuildingDispute tempFastTrackBuildingDispute = new FastTrackBuildingDispute();
        tempFastTrackBuildingDispute.setInput1("The claimant must prepare a Scott Schedule of the defects, items of damage, "
                        + "or any other relevant matters");
        tempFastTrackBuildingDispute.setInput2("The columns should be headed:\n"
                        + "  •  Item\n"
                        + "  •  Alleged defect\n"
                        + "  •  Claimant’s costing\n"
                        + "  •  Defendant’s response\n"
                        + "  •  Defendant’s costing\n"
                        + "  •  Reserved for Judge’s use");
        tempFastTrackBuildingDispute.setInput3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns"
                        + " completed by 4pm on");
        tempFastTrackBuildingDispute.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        tempFastTrackBuildingDispute.setInput4("The defendant must upload to the Digital Portal an amended version of the Scott Schedule "
                        + "with the relevant columns in response completed by 4pm on");
        tempFastTrackBuildingDispute.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));

        caseData.setFastTrackBuildingDispute(tempFastTrackBuildingDispute);

        FastTrackClinicalNegligence tempFastTrackClinicalNegligence = new FastTrackClinicalNegligence();
        tempFastTrackClinicalNegligence.setInput1("Documents should be retained as follows:");
        tempFastTrackClinicalNegligence.setInput2("a) The parties must retain all electronically stored documents relating to the issues in this "
                        + "claim.");
        tempFastTrackClinicalNegligence.setInput3("b) the defendant must retain the original clinical notes relating to the issues in this claim. "
                        + "The defendant must give facilities for inspection by the claimant, the claimant's legal "
                        + "advisers and experts of these original notes on 7 days written notice.");
        tempFastTrackClinicalNegligence.setInput4("c) Legible copies of the medical and educational records of the claimant "
                        + "are to be placed in a separate paginated bundle by the claimant's "
                        + "solicitors and kept up to date. All references to medical notes are to be made by reference "
                        + "to the pages in that bundle.");

        caseData.setFastTrackClinicalNegligence(tempFastTrackClinicalNegligence);

        SdoR2FastTrackCreditHireDetails tempSdoR2FastTrackCreditHireDetails = new SdoR2FastTrackCreditHireDetails();
        tempSdoR2FastTrackCreditHireDetails.setInput2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity");
        tempSdoR2FastTrackCreditHireDetails.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        tempSdoR2FastTrackCreditHireDetails.setInput3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.");
        String partiesLiaseString = "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no ";
        tempSdoR2FastTrackCreditHireDetails.setInput4(partiesLiaseString + laterThanFourPmString);
        tempSdoR2FastTrackCreditHireDetails.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)));

        SdoR2FastTrackCreditHire tempSdoR2FastTrackCreditHire = new SdoR2FastTrackCreditHire();
        tempSdoR2FastTrackCreditHire.setInput1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                        + "disclosure as ordered earlier in this Order must include:\n"
                        + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                        + "commencement of hire until the earlier of:\n "
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
                        + "prior to the commencement of hire until the earlier of:\n"
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.");
        tempSdoR2FastTrackCreditHire.setInput5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                        + "is available.");
        tempSdoR2FastTrackCreditHire.setInput6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
        tempSdoR2FastTrackCreditHire.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));
        tempSdoR2FastTrackCreditHire.setInput7(claimantEvidenceString);
        tempSdoR2FastTrackCreditHire.setDate4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        tempSdoR2FastTrackCreditHire.setInput8(witnessStatementString);
        List<AddOrRemoveToggle> addOrRemoveToggleList = List.of(AddOrRemoveToggle.ADD);
        tempSdoR2FastTrackCreditHire.setDetailsShowToggle(addOrRemoveToggleList);
        tempSdoR2FastTrackCreditHire.setSdoR2FastTrackCreditHireDetails(tempSdoR2FastTrackCreditHireDetails);

        caseData.setSdoR2FastTrackCreditHire(tempSdoR2FastTrackCreditHire);

        FastTrackCreditHire tempFastTrackCreditHire = new FastTrackCreditHire();
        tempFastTrackCreditHire.setInput1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                        + "disclosure as ordered earlier in this Order must include:\n"
                        + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                        + "commencement of hire until the earlier of:\n "
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
                        + "prior to the commencement of hire until the earlier of:\n"
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.");
        tempFastTrackCreditHire.setInput2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity");
        tempFastTrackCreditHire.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        tempFastTrackCreditHire.setInput3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.");
        tempFastTrackCreditHire.setInput4(partiesLiaseString + laterThanFourPmString);
        tempFastTrackCreditHire.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)));
        tempFastTrackCreditHire.setInput5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                        + "is available.");
        tempFastTrackCreditHire.setInput6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
        tempFastTrackCreditHire.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));
        tempFastTrackCreditHire.setInput7(claimantEvidenceString);
        tempFastTrackCreditHire.setDate4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        tempFastTrackCreditHire.setInput8(witnessStatementString);

        caseData.setFastTrackCreditHire(tempFastTrackCreditHire);

        FastTrackHousingDisrepair tempFastTrackHousingDisrepair = new FastTrackHousingDisrepair();
        tempFastTrackHousingDisrepair.setInput1("The claimant must prepare a Scott Schedule of the items in disrepair.");
        tempFastTrackHousingDisrepair.setInput2("The columns should be headed:\n"
                        + "  •  Item\n"
                        + "  •  Alleged disrepair\n"
                        + "  •  Defendant’s response\n"
                        + "  •  Reserved for Judge’s use");
        tempFastTrackHousingDisrepair.setInput3("The claimant must upload to the Digital Portal the Scott Schedule with the relevant "
                        + "columns completed by 4pm on");
        tempFastTrackHousingDisrepair.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        tempFastTrackHousingDisrepair.setInput4("The defendant must upload to the Digital Portal the amended Scott Schedule with the "
                        + "relevant columns in response completed by 4pm on");
        tempFastTrackHousingDisrepair.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(12)));

        caseData.setFastTrackHousingDisrepair(tempFastTrackHousingDisrepair);

        FastTrackPersonalInjury tempFastTrackPersonalInjury = new FastTrackPersonalInjury();
        tempFastTrackPersonalInjury.setInput1("The claimant has permission to rely upon the written expert evidence already uploaded to "
                        + "the Digital Portal with the particulars of claim and in addition has permission to rely upon"
                        + " any associated correspondence or updating report which is uploaded to the Digital Portal by"
                        + " 4pm on");
        tempFastTrackPersonalInjury.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        tempFastTrackPersonalInjury.setInput2("Any questions which are to be addressed to an expert must be sent to the expert directly "
                        + "and uploaded to the Digital Portal by 4pm on");
        tempFastTrackPersonalInjury.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        tempFastTrackPersonalInjury.setInput3("The answers to the questions shall be answered by the Expert by");
        tempFastTrackPersonalInjury.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));
        tempFastTrackPersonalInjury.setInput4("and uploaded to the Digital Portal by");
        tempFastTrackPersonalInjury.setDate4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));

        caseData.setFastTrackPersonalInjury(tempFastTrackPersonalInjury);

        FastTrackRoadTrafficAccident tempFastTrackRoadTrafficAccident = new FastTrackRoadTrafficAccident();
        tempFastTrackRoadTrafficAccident.setInput("Photographs and/or a plan of the accident location shall be prepared and agreed by the "
                       + "parties and uploaded to the Digital Portal by 4pm on");
        tempFastTrackRoadTrafficAccident.setDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));

        caseData.setFastTrackRoadTrafficAccident(tempFastTrackRoadTrafficAccident);

        SmallClaimsJudgesRecital tempSmallClaimsJudgesRecital = new SmallClaimsJudgesRecital();
        tempSmallClaimsJudgesRecital.setInput("Upon considering the statements of case and the information provided by the parties,");

        caseData.setSmallClaimsJudgesRecital(tempSmallClaimsJudgesRecital);

        SmallClaimsDocuments tempSmallClaimsDocuments = new SmallClaimsDocuments();
        tempSmallClaimsDocuments.setInput1("Each party must upload to the Digital Portal copies of all documents which they wish the court to"
                        + " consider when reaching its decision not less than 21 days before the hearing.");
        tempSmallClaimsDocuments.setInput2("The court may refuse to consider any document which has not been uploaded to the "
                        + "Digital Portal by the above date.");

        caseData.setSmallClaimsDocuments(tempSmallClaimsDocuments);

        SdoR2SmallClaimsWitnessStatements tempSdoR2SmallClaimsWitnessStatements = new SdoR2SmallClaimsWitnessStatements();
        tempSdoR2SmallClaimsWitnessStatements.setSdoStatementOfWitness(
                "Each party must upload to the Digital Portal copies of all witness statements of the witnesses"
                    + " upon whose evidence they intend to rely at the hearing not less than 21 days before"
                    + " the hearing.");
        tempSdoR2SmallClaimsWitnessStatements.setIsRestrictWitness(NO);
        SdoR2SmallClaimsRestrictWitness sdoR2SmallClaimsRestrictWitness =  new SdoR2SmallClaimsRestrictWitness();
        sdoR2SmallClaimsRestrictWitness.setNoOfWitnessClaimant(2);
        sdoR2SmallClaimsRestrictWitness.setNoOfWitnessDefendant(2);
        sdoR2SmallClaimsRestrictWitness.setPartyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT);
        tempSdoR2SmallClaimsWitnessStatements.setSdoR2SmallClaimsRestrictWitness(sdoR2SmallClaimsRestrictWitness);
        tempSdoR2SmallClaimsWitnessStatements.setIsRestrictPages(NO);
        SdoR2SmallClaimsRestrictPages sdoR2SmallClaimsRestrictPages  = new SdoR2SmallClaimsRestrictPages();
        sdoR2SmallClaimsRestrictPages.setWitnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1);
        sdoR2SmallClaimsRestrictPages.setNoOfPages(12);
        sdoR2SmallClaimsRestrictPages.setFontDetails(RESTRICT_NUMBER_PAGES_TEXT2);
        tempSdoR2SmallClaimsWitnessStatements.setSdoR2SmallClaimsRestrictPages(sdoR2SmallClaimsRestrictPages);
        tempSdoR2SmallClaimsWitnessStatements.setText(WITNESS_DESCRIPTION_TEXT);
        caseData.setSdoR2SmallClaimsWitnessStatementOther(tempSdoR2SmallClaimsWitnessStatements);

        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            SmallClaimsMediation smallClaimsMediation  = new SmallClaimsMediation();
            smallClaimsMediation.setInput(
                                                                     "If you failed to attend a mediation appointment,"
                                                                         + " then the judge at the hearing may impose a sanction. "
                                                                         + "This could require you to pay costs, or could result in your claim or defence being dismissed. "
                                                                         + "You should deliver to every other party, and to the court, your explanation for non-attendance, "
                                                                         + "with any supporting documents, at least 14 days before the hearing. "
                                                                         + "Any other party who wishes to comment on the failure to attend the mediation appointment should "
                                                                         + "deliver their comments,"
                                                                         + " with any supporting documents, to all parties and to the court at least "
                    + "14 days before the hearing.");
            caseData.setSmallClaimsMediationSectionStatement(smallClaimsMediation);
        }

        SmallClaimsFlightDelay tempSmallClaimsFlightDelay = new SmallClaimsFlightDelay();
        tempSmallClaimsFlightDelay.setSmallClaimsFlightDelayToggle(checkList);
        tempSmallClaimsFlightDelay.setRelatedClaimsInput("In the event that the Claimant(s) or Defendant(s) are aware if other \n"
                                    + "claims relating to the same flight they must notify the court \n"
                                    + "where the claim is being managed within 14 days of receipt of \n"
                                    + "this Order providing all relevant details of those claims including \n"
                                    + "case number(s), hearing date(s) and copy final substantive order(s) \n"
                                    + "if any, to assist the Court with ongoing case management which may \n"
                                    + "include the cases being heard together.");
        tempSmallClaimsFlightDelay.setLegalDocumentsInput("Any arguments as to the law to be applied to this claim, together with \n"
                                     + "copies of legal authorities or precedents relied on, shall be uploaded \n"
                                     + "to the Digital Portal not later than 3 full working days before the \n"
                                     + "final hearing date.");

        caseData.setSmallClaimsFlightDelay(tempSmallClaimsFlightDelay);

        SmallClaimsHearing tempSmallClaimsHearing = new SmallClaimsHearing();
        tempSmallClaimsHearing.setInput1("The hearing of the claim will be on a date to be notified to you by a separate notification. "
                        + "The hearing will have a time estimate of");
        tempSmallClaimsHearing.setInput2(HEARING_TIME_TEXT_AFTER);

        caseData.setSmallClaimsHearing(tempSmallClaimsHearing);

        SmallClaimsNotes tempSmallClaimsNotes = new SmallClaimsNotes();
        tempSmallClaimsNotes.setInput("This order has been made without hearing. "
                                       + "Each party has the right to apply to have this Order set aside or varied. "
                                       + "Any such application must be received by the Court "
                                       + "(together with the appropriate fee) by 4pm on "
                                       + DateFormatHelper.formatLocalDate(
            deadlinesCalculator.plusWorkingDays(LocalDate.now(), 5), DATE)
        );

        caseData.setSmallClaimsNotes(tempSmallClaimsNotes);

        SmallClaimsCreditHire tempSmallClaimsCreditHire = new SmallClaimsCreditHire();
        tempSmallClaimsCreditHire.setInput1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
                        + "disclosure as ordered earlier in this Order must include:\n"
                        + "a) Evidence of all income from all sources for a period of 3 months prior to the "
                        + "commencement of hire until the earlier of:\n "
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
                        + "prior to the commencement of hire until the earlier of:\n"
                        + "     i) 3 months after cessation of hire\n"
                        + "     ii) the repair or replacement of the claimant's vehicle\n"
                        + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.");
        tempSmallClaimsCreditHire.setInput2("The claimant must upload to the Digital Portal a witness statement addressing\n"
                        + "a) the need to hire a replacement vehicle; and\n"
                        + "b) impecuniosity");
        tempSmallClaimsCreditHire.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        tempSmallClaimsCreditHire.setInput3("A failure to comply with the paragraph above will result in the claimant being debarred from "
                        + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
                        + "save with permission of the Trial Judge.");
        tempSmallClaimsCreditHire.setInput4(partiesLiaseString + laterThanFourPmString);
        tempSmallClaimsCreditHire.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)));
        tempSmallClaimsCreditHire.setInput5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
                        + "paragraph above, each party may rely upon written evidence by way of witness statement of "
                        + "one witness to provide evidence of basic hire rates available within the claimant's "
                        + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
                        + "is available.");
        tempSmallClaimsCreditHire.setInput6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
        tempSmallClaimsCreditHire.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));
        tempSmallClaimsCreditHire.setInput7(claimantEvidenceString);
        tempSmallClaimsCreditHire.setDate4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)));
        tempSmallClaimsCreditHire.setInput11(witnessStatementString);

        caseData.setSmallClaimsCreditHire(tempSmallClaimsCreditHire);

        SmallClaimsRoadTrafficAccident tempSmallClaimsRoadTrafficAccident = new SmallClaimsRoadTrafficAccident();
        tempSmallClaimsRoadTrafficAccident.setInput("Photographs and/or a plan of the accident location shall be prepared and agreed by the parties"
                       + " and uploaded to the Digital Portal no later than 21 days before the hearing.");

        caseData.setSmallClaimsRoadTrafficAccident(tempSmallClaimsRoadTrafficAccident);

        //This the flow after request for reconsideration
        if (CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(caseData.getDecisionOnRequestReconsiderationOptions())) {
            FastTrackAllocation fastTrackAllocation = new FastTrackAllocation();
            fastTrackAllocation.setAssignComplexityBand(null);
            caseData.setDrawDirectionsOrderRequired(null);
            caseData.setDrawDirectionsOrderSmallClaims(null);
            caseData.setFastClaims(null);
            caseData.setSmallClaims(null);
            caseData.setClaimsTrack(null);
            caseData.setOrderType(null);
            caseData.setTrialAdditionalDirectionsForFastTrack(null);
            caseData.setDrawDirectionsOrderSmallClaimsAdditionalDirections(null);
            caseData.setFastTrackAllocation(fastTrackAllocation);
            caseData.setDisposalHearingAddNewDirections(null);
            caseData.setSmallClaimsAddNewDirections(null);
            caseData.setFastTrackAddNewDirections(null);
            caseData.setSdoHearingNotes(null);
            caseData.setFastTrackHearingNotes(null);
            caseData.setDisposalHearingHearingNotes(null);
            caseData.setSdoR2SmallClaimsHearing(null);
            caseData.setSdoR2SmallClaimsUploadDoc(null);
            caseData.setSdoR2SmallClaimsPPI(null);
            caseData.setSdoR2SmallClaimsImpNotes(null);
            caseData.setSdoR2SmallClaimsWitnessStatements(null);
            caseData.setSdoR2SmallClaimsHearingToggle(null);
            caseData.setSdoR2SmallClaimsJudgesRecital(null);
            caseData.setSdoR2SmallClaimsWitnessStatementsToggle(null);
            caseData.setSdoR2SmallClaimsPPIToggle(null);
            caseData.setSdoR2SmallClaimsUploadDocToggle(null);
        }

        updateExpertEvidenceFields(caseData);
        updateDisclosureOfDocumentFields(caseData);
        populateDRHFields(callbackParams, caseData, preferredCourt, hearingMethodList, locationRefDataList);
        prePopulateNihlFields(caseData, hearingMethodList, preferredCourt, locationRefDataList);
        List<IncludeInOrderToggle> localIncludeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);
        setCheckListNihl(caseData, localIncludeInOrderToggle);
        SdoR2WelshLanguageUsage sdoR2WelshLanguageUsage  = new SdoR2WelshLanguageUsage();
        sdoR2WelshLanguageUsage.setDescription(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION);
        caseData.setSdoR2FastTrackUseOfWelshLanguage(sdoR2WelshLanguageUsage);
        caseData.setSdoR2SmallClaimsUseOfWelshLanguage(sdoR2WelshLanguageUsage);
        caseData.setSdoR2DisposalHearingUseOfWelshLanguage(sdoR2WelshLanguageUsage);

        caseData.setSmallClaimsPenalNotice(DEFAULT_PENAL_NOTICE);
        caseData.setFastTrackPenalNotice(DEFAULT_PENAL_NOTICE);
        caseData.setSmallClaimsPenalNoticeToggle(new ArrayList<>());
        caseData.setFastTrackPenalNoticeToggle(new ArrayList<>());

        populatePpiFields(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
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

    private static SdoR2WitnessOfFact getSdoR2WitnessOfFact() {
        SdoR2RestrictNoOfWitnessDetails sdoR2RestrictNoOfWitnessDetails  = new SdoR2RestrictNoOfWitnessDetails();
        sdoR2RestrictNoOfWitnessDetails.setNoOfWitnessClaimant(3);
        sdoR2RestrictNoOfWitnessDetails.setNoOfWitnessDefendant(3);
        sdoR2RestrictNoOfWitnessDetails.setPartyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT);
        SdoR2RestrictWitness sdoR2RestrictWitness  = new SdoR2RestrictWitness();
        sdoR2RestrictWitness.setIsRestrictWitness(NO);
        sdoR2RestrictWitness.setRestrictNoOfWitnessDetails(sdoR2RestrictNoOfWitnessDetails);
        SdoR2WitnessOfFact sdoR2WitnessOfFact = new SdoR2WitnessOfFact();
        sdoR2WitnessOfFact.setSdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS);
        sdoR2WitnessOfFact.setSdoR2RestrictWitness(sdoR2RestrictWitness);

        SdoR2RestrictNoOfPagesDetails sdoR2RestrictNoOfPagesDetails  = new SdoR2RestrictNoOfPagesDetails();
        sdoR2RestrictNoOfPagesDetails.setWitnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1);
        sdoR2RestrictNoOfPagesDetails.setNoOfPages(12);
        sdoR2RestrictNoOfPagesDetails.setFontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2);
        SdoR2RestrictPages sdoR2RestrictPages  = new SdoR2RestrictPages();
        sdoR2RestrictPages.setIsRestrictPages(NO);
        sdoR2RestrictPages.setRestrictNoOfPagesDetails(sdoR2RestrictNoOfPagesDetails);
        sdoR2WitnessOfFact.setSdoRestrictPages(sdoR2RestrictPages);
        sdoR2WitnessOfFact.setSdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE);
        sdoR2WitnessOfFact.setSdoWitnessDeadlineDate(LocalDate.now().plusDays(70));
        sdoR2WitnessOfFact.setSdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE);
        return sdoR2WitnessOfFact;
    }

    private void updateExpertEvidenceFields(CaseData updatedData) {
        FastTrackPersonalInjury tempFastTrackPersonalInjury = new FastTrackPersonalInjury();
        tempFastTrackPersonalInjury.setInput1("The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                        + " Digital Portal with the particulars of claim");
        tempFastTrackPersonalInjury.setInput2("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert " +
                        "directly and uploaded to the Digital Portal by 4pm on");
        tempFastTrackPersonalInjury.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(14)));
        tempFastTrackPersonalInjury.setInput3("The answers to the questions shall be answered by the Expert by");
        tempFastTrackPersonalInjury.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(42)));
        tempFastTrackPersonalInjury.setInput4("and uploaded to the Digital Portal by the party who has asked the question by");
        tempFastTrackPersonalInjury.setDate4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(49)));

        updatedData.setFastTrackPersonalInjury(tempFastTrackPersonalInjury);
    }

    private void updateDisclosureOfDocumentFields(CaseData updatedData) {
        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = new FastTrackDisclosureOfDocuments();
        tempFastTrackDisclosureOfDocuments.setInput1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on");
        tempFastTrackDisclosureOfDocuments.setDate1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)));
        tempFastTrackDisclosureOfDocuments.setInput2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on");
        tempFastTrackDisclosureOfDocuments.setDate2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(5)));
        tempFastTrackDisclosureOfDocuments.setInput3("Requests will be complied with within 7 days of the receipt of the request.");
        tempFastTrackDisclosureOfDocuments.setInput4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on");
        tempFastTrackDisclosureOfDocuments.setDate3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)));

        updatedData.setFastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments);
    }

    private void populateDRHFields(CallbackParams callbackParams,
                                   CaseData updatedData, Optional<RequestedCourt> preferredCourt,
                                   DynamicList hearingMethodList, List<LocationRefData> locationRefDataList) {
        DynamicList courtList = getCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        courtList.setValue(courtList.getListItems().get(0));

        hearingMethodList.setValue(hearingMethodList.getListItems().stream().filter(elem -> elem.getLabel()
            .equals(HearingMethod.TELEPHONE.getLabel())).findFirst().orElse(null));

        SdoR2SmallClaimsJudgesRecital smallClaimsJudgesRecital = new SdoR2SmallClaimsJudgesRecital();
        smallClaimsJudgesRecital.setInput(SdoR2UiConstantSmallClaim.JUDGE_RECITAL);
        updatedData.setSdoR2SmallClaimsJudgesRecital(smallClaimsJudgesRecital);

        SdoR2SmallClaimsPPI smallClaimsPpi = new SdoR2SmallClaimsPPI();
        smallClaimsPpi.setPpiDate(LocalDate.now().plusDays(21));
        smallClaimsPpi.setText(SdoR2UiConstantSmallClaim.PPI_DESCRIPTION);
        updatedData.setSdoR2SmallClaimsPPI(smallClaimsPpi);

        SdoR2SmallClaimsUploadDoc smallClaimsUploadDoc = new SdoR2SmallClaimsUploadDoc();
        smallClaimsUploadDoc.setSdoUploadOfDocumentsTxt(SdoR2UiConstantSmallClaim.UPLOAD_DOC_DESCRIPTION);
        updatedData.setSdoR2SmallClaimsUploadDoc(smallClaimsUploadDoc);

        SdoR2SmallClaimsRestrictWitness restrictWitness = new SdoR2SmallClaimsRestrictWitness();
        restrictWitness.setPartyIsCountedAsWitnessTxt(SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT);

        SdoR2SmallClaimsRestrictPages restrictPages = new SdoR2SmallClaimsRestrictPages();
        restrictPages.setFontDetails(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2);
        restrictPages.setNoOfPages(12);
        restrictPages.setWitnessShouldNotMoreThanTxt(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1);

        SdoR2SmallClaimsWitnessStatements witnessStatements = new SdoR2SmallClaimsWitnessStatements();
        witnessStatements.setSdoStatementOfWitness(SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT);
        witnessStatements.setIsRestrictWitness(NO);
        witnessStatements.setIsRestrictPages(NO);
        witnessStatements.setSdoR2SmallClaimsRestrictWitness(restrictWitness);
        witnessStatements.setSdoR2SmallClaimsRestrictPages(restrictPages);
        witnessStatements.setText(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT);
        updatedData.setSdoR2SmallClaimsWitnessStatements(witnessStatements);

        SdoR2SmallClaimsHearingFirstOpenDateAfter firstOpenDateAfter = new SdoR2SmallClaimsHearingFirstOpenDateAfter();
        firstOpenDateAfter.setListFrom(LocalDate.now().plusDays(56));

        SdoR2SmallClaimsHearingWindow hearingWindow = new SdoR2SmallClaimsHearingWindow();
        hearingWindow.setDateTo(LocalDate.now().plusDays(70));
        hearingWindow.setListFrom(LocalDate.now().plusDays(56));

        SdoR2SmallClaimsBundleOfDocs bundleOfDocs = new SdoR2SmallClaimsBundleOfDocs();
        bundleOfDocs.setPhysicalBundlePartyTxt(SdoR2UiConstantSmallClaim.BUNDLE_TEXT);

        SdoR2SmallClaimsHearing smallClaimsHearing = new SdoR2SmallClaimsHearing();
        smallClaimsHearing.setTrialOnOptions(HearingOnRadioOptions.OPEN_DATE);
        smallClaimsHearing.setMethodOfHearing(hearingMethodList);
        smallClaimsHearing.setLengthList(SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES);
        smallClaimsHearing.setPhysicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY);
        smallClaimsHearing.setSdoR2SmallClaimsHearingFirstOpenDateAfter(firstOpenDateAfter);
        smallClaimsHearing.setSdoR2SmallClaimsHearingWindow(hearingWindow);
        smallClaimsHearing.setHearingCourtLocationList(courtList);
        smallClaimsHearing.setAltHearingCourtLocationList(getLocationList(
                                                    preferredCourt.orElse(null),
                                                    true,
                                                    locationRefDataList
        ));
        smallClaimsHearing.setSdoR2SmallClaimsBundleOfDocs(bundleOfDocs);
        updatedData.setSdoR2SmallClaimsHearing(smallClaimsHearing);

        SdoR2SmallClaimsImpNotes importantNotes = new SdoR2SmallClaimsImpNotes();
        importantNotes.setText(SdoR2UiConstantSmallClaim.IMP_NOTES_TEXT);
        importantNotes.setDate(LocalDate.now().plusDays(7));
        updatedData.setSdoR2SmallClaimsImpNotes(importantNotes);
        updatedData.setSdoR2SmallClaimsUploadDocToggle(includeInOrderToggle);
        updatedData.setSdoR2SmallClaimsHearingToggle(includeInOrderToggle);
        updatedData.setSdoR2SmallClaimsWitnessStatementsToggle(includeInOrderToggle);
        SdoR2WelshLanguageUsage smallClaimsWelshUsage = new SdoR2WelshLanguageUsage();
        smallClaimsWelshUsage.setDescription(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION);
        updatedData.setSdoR2DrhUseOfWelshLanguage(smallClaimsWelshUsage);

        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            updatedData.setSdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
            SdoR2SmallClaimsMediation mediationStatement = new SdoR2SmallClaimsMediation();
            mediationStatement.setInput(SdoR2UiConstantSmallClaim.CARM_MEDIATION_TEXT);
            updatedData.setSdoR2SmallClaimsMediationSectionStatement(mediationStatement);
        }
    }

    private void populatePpiFields(CaseData caseData) {
        PPI ppi = new PPI();
        ppi.setPpiDate(LocalDate.now().plusDays(28));
        ppi.setText(SdoR2UiConstantSmallClaim.PPI_DESCRIPTION);
        caseData.setSmallClaimsPPI(ppi);
        caseData.setFastTrackPPI(ppi);
    }

    private void resetPpiFields(CaseData caseData, boolean isSmallClaimsTrack, boolean isFastTrack) {
        if (isSmallClaimsTrack && !SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimPPI")) {
            caseData.setSmallClaimsPPI(null);
        }

        if (isFastTrack && !SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimPPI")) {
            caseData.setFastTrackPPI(null);
        }
    }

    private void prePopulateNihlFields(CaseData updatedData, DynamicList hearingMethodList,
                                       Optional<RequestedCourt> preferredCourt,
                                       List<LocationRefData> locationRefDataList) {
        DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream().filter(elem -> elem.getLabel()
            .equals(HearingMethod.IN_PERSON.getLabel())).findFirst().orElse(null);
        hearingMethodList.setValue(hearingMethodInPerson);
        FastTrackJudgesRecital fastTrackJudgesRecital = new FastTrackJudgesRecital();
        fastTrackJudgesRecital.setInput(SdoR2UiConstantFastTrack.JUDGE_RECITAL);
        updatedData.setSdoFastTrackJudgesRecital(fastTrackJudgesRecital);

        SdoR2DisclosureOfDocuments disclosureOfDocuments = new SdoR2DisclosureOfDocuments();
        disclosureOfDocuments.setStandardDisclosureTxt(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE);
        disclosureOfDocuments.setStandardDisclosureDate(LocalDate.now().plusDays(28));
        disclosureOfDocuments.setInspectionTxt(SdoR2UiConstantFastTrack.INSPECTION);
        disclosureOfDocuments.setInspectionDate(LocalDate.now().plusDays(42));
        disclosureOfDocuments.setRequestsWillBeCompiledLabel(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH);
        updatedData.setSdoR2DisclosureOfDocuments(disclosureOfDocuments);

        SdoR2RestrictNoOfWitnessDetails restrictNoOfWitnessDetails = new SdoR2RestrictNoOfWitnessDetails();
        restrictNoOfWitnessDetails.setNoOfWitnessClaimant(3);
        restrictNoOfWitnessDetails.setNoOfWitnessDefendant(3);
        restrictNoOfWitnessDetails.setPartyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT);

        SdoR2RestrictWitness restrictWitness = new SdoR2RestrictWitness();
        restrictWitness.setIsRestrictWitness(NO);
        restrictWitness.setRestrictNoOfWitnessDetails(restrictNoOfWitnessDetails);

        SdoR2RestrictNoOfPagesDetails restrictNoOfPagesDetails = new SdoR2RestrictNoOfPagesDetails();
        restrictNoOfPagesDetails.setWitnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1);
        restrictNoOfPagesDetails.setNoOfPages(12);
        restrictNoOfPagesDetails.setFontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2);

        SdoR2RestrictPages restrictPages = new SdoR2RestrictPages();
        restrictPages.setIsRestrictPages(NO);
        restrictPages.setRestrictNoOfPagesDetails(restrictNoOfPagesDetails);

        SdoR2WitnessOfFact witnessOfFact = new SdoR2WitnessOfFact();
        witnessOfFact.setSdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS);
        witnessOfFact.setSdoR2RestrictWitness(restrictWitness);
        witnessOfFact.setSdoRestrictPages(restrictPages);
        witnessOfFact.setSdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE);
        witnessOfFact.setSdoWitnessDeadlineDate(LocalDate.now().plusDays(70));
        witnessOfFact.setSdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE);
        updatedData.setSdoR2WitnessesOfFact(witnessOfFact);

        SdoR2ScheduleOfLoss scheduleOfLoss = new SdoR2ScheduleOfLoss();
        scheduleOfLoss.setSdoR2ScheduleOfLossClaimantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT);
        scheduleOfLoss.setIsClaimForPecuniaryLoss(NO);
        scheduleOfLoss.setSdoR2ScheduleOfLossClaimantDate(LocalDate.now().plusDays(364));
        scheduleOfLoss.setSdoR2ScheduleOfLossDefendantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT);
        scheduleOfLoss.setSdoR2ScheduleOfLossDefendantDate(LocalDate.now().plusDays(378));
        scheduleOfLoss.setSdoR2ScheduleOfLossPecuniaryLossTxt(SdoR2UiConstantFastTrack.PECUNIARY_LOSS);
        updatedData.setSdoR2ScheduleOfLoss(scheduleOfLoss);

        SdoR2TrialFirstOpenDateAfter trialFirstOpenDateAfter = new SdoR2TrialFirstOpenDateAfter();
        trialFirstOpenDateAfter.setListFrom(LocalDate.now().plusDays(434));

        SdoR2TrialWindow trialWindow = new SdoR2TrialWindow();
        trialWindow.setListFrom(LocalDate.now().plusDays(434));
        trialWindow.setDateTo(LocalDate.now().plusDays(455));

        DynamicList baseCourtLocationList = getCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        DynamicList hearingCourtLocationList = new DynamicList();
        hearingCourtLocationList.setListItems(baseCourtLocationList.getListItems());
        if (!baseCourtLocationList.getListItems().isEmpty()) {
            hearingCourtLocationList.setValue(baseCourtLocationList.getListItems().get(0));
        }

        SdoR2Trial trial = new SdoR2Trial();
        trial.setTrialOnOptions(TrialOnRadioOptions.OPEN_DATE);
        trial.setLengthList(FastTrackHearingTimeEstimate.FIVE_HOURS);
        trial.setMethodOfHearing(hearingMethodList);
        trial.setPhysicalBundleOptions(PhysicalTrialBundleOptions.PARTY);
        trial.setSdoR2TrialFirstOpenDateAfter(trialFirstOpenDateAfter);
        trial.setSdoR2TrialWindow(trialWindow);
        trial.setHearingCourtLocationList(hearingCourtLocationList);
        trial.setAltHearingCourtLocationList(getAlternativeCourtLocationsForNihl(locationRefDataList));
        trial.setPhysicalBundlePartyTxt(SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE);
        updatedData.setSdoR2Trial(trial);

        updatedData.setSdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        updatedData.setSdoR2ImportantNotesDate(LocalDate.now().plusDays(7));

        SdoR2ExpertEvidence expertEvidence = new SdoR2ExpertEvidence();
        expertEvidence.setSdoClaimantPermissionToRelyTxt(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY);
        updatedData.setSdoR2ExpertEvidence(expertEvidence);

        SdoR2AddendumReport addendumReport = new SdoR2AddendumReport();
        addendumReport.setSdoAddendumReportTxt(SdoR2UiConstantFastTrack.ADDENDUM_REPORT);
        addendumReport.setSdoAddendumReportDate(LocalDate.now().plusDays(56));
        updatedData.setSdoR2AddendumReport(addendumReport);

        SdoR2FurtherAudiogram furtherAudiogram = new SdoR2FurtherAudiogram();
        furtherAudiogram.setSdoClaimantShallUndergoTxt(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO);
        furtherAudiogram.setSdoServiceReportTxt(SdoR2UiConstantFastTrack.SERVICE_REPORT);
        furtherAudiogram.setSdoClaimantShallUndergoDate(LocalDate.now().plusDays(42));
        furtherAudiogram.setSdoServiceReportDate(LocalDate.now().plusDays(98));
        updatedData.setSdoR2FurtherAudiogram(furtherAudiogram);

        SdoR2ApplicationToRelyOnFurtherDetails applicationDetails = new SdoR2ApplicationToRelyOnFurtherDetails();
        applicationDetails.setApplicationToRelyDetailsTxt(SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS);
        applicationDetails.setApplicationToRelyDetailsDate(LocalDate.now().plusDays(161));

        SdoR2ApplicationToRelyOnFurther applicationToRelyOnFurther = new SdoR2ApplicationToRelyOnFurther();
        applicationToRelyOnFurther.setDoRequireApplicationToRely(NO);
        applicationToRelyOnFurther.setApplicationToRelyOnFurtherDetails(applicationDetails);

        SdoR2QuestionsClaimantExpert questionsClaimantExpert = new SdoR2QuestionsClaimantExpert();
        questionsClaimantExpert.setSdoDefendantMayAskTxt(SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK);
        questionsClaimantExpert.setSdoDefendantMayAskDate(LocalDate.now().plusDays(126));
        questionsClaimantExpert.setSdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED);
        questionsClaimantExpert.setSdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(147));
        questionsClaimantExpert.setSdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL);
        questionsClaimantExpert.setSdoApplicationToRelyOnFurther(applicationToRelyOnFurther);
        updatedData.setSdoR2QuestionsClaimantExpert(questionsClaimantExpert);

        SdoR2PermissionToRelyOnExpert permissionToRelyOnExpert = new SdoR2PermissionToRelyOnExpert();
        permissionToRelyOnExpert.setSdoPermissionToRelyOnExpertTxt(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT);
        permissionToRelyOnExpert.setSdoPermissionToRelyOnExpertDate(LocalDate.now().plusDays(119));
        permissionToRelyOnExpert.setSdoJointMeetingOfExpertsTxt(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS);
        permissionToRelyOnExpert.setSdoJointMeetingOfExpertsDate(LocalDate.now().plusDays(147));
        permissionToRelyOnExpert.setSdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS);
        updatedData.setSdoR2PermissionToRelyOnExpert(permissionToRelyOnExpert);

        SdoR2EvidenceAcousticEngineer evidenceAcousticEngineer = new SdoR2EvidenceAcousticEngineer();
        evidenceAcousticEngineer.setSdoEvidenceAcousticEngineerTxt(SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER);
        evidenceAcousticEngineer.setSdoInstructionOfTheExpertTxt(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT);
        evidenceAcousticEngineer.setSdoInstructionOfTheExpertDate(LocalDate.now().plusDays(42));
        evidenceAcousticEngineer.setSdoInstructionOfTheExpertTxtArea(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA);
        evidenceAcousticEngineer.setSdoExpertReportTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT);
        evidenceAcousticEngineer.setSdoExpertReportDate(LocalDate.now().plusDays(280));
        evidenceAcousticEngineer.setSdoExpertReportDigitalPortalTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL);
        evidenceAcousticEngineer.setSdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS);
        evidenceAcousticEngineer.setSdoWrittenQuestionsDate(LocalDate.now().plusDays(294));
        evidenceAcousticEngineer.setSdoWrittenQuestionsDigitalPortalTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL);
        evidenceAcousticEngineer.setSdoRepliesTxt(SdoR2UiConstantFastTrack.REPLIES);
        evidenceAcousticEngineer.setSdoRepliesDate(LocalDate.now().plusDays(315));
        evidenceAcousticEngineer.setSdoRepliesDigitalPortalTxt(SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL);
        evidenceAcousticEngineer.setSdoServiceOfOrderTxt(SdoR2UiConstantFastTrack.SERVICE_OF_ORDER);
        updatedData.setSdoR2EvidenceAcousticEngineer(evidenceAcousticEngineer);

        SdoR2QuestionsToEntExpert questionsToEntExpert = new SdoR2QuestionsToEntExpert();
        questionsToEntExpert.setSdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS);
        questionsToEntExpert.setSdoWrittenQuestionsDate(LocalDate.now().plusDays(336));
        questionsToEntExpert.setSdoWrittenQuestionsDigPortalTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL);
        questionsToEntExpert.setSdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED);
        questionsToEntExpert.setSdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(350));
        questionsToEntExpert.setSdoShallBeUploadedTxt(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED);
        updatedData.setSdoR2QuestionsToEntExpert(questionsToEntExpert);

        SdoR2UploadOfDocuments uploadOfDocuments = new SdoR2UploadOfDocuments();
        uploadOfDocuments.setSdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS);
        updatedData.setSdoR2UploadOfDocuments(uploadOfDocuments);

        SdoR2WelshLanguageUsage nihlWelshUsage = new SdoR2WelshLanguageUsage();
        nihlWelshUsage.setDescription(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION);
        updatedData.setSdoR2NihlUseOfWelshLanguage(nihlWelshUsage);

    }

    private void updateDeductionValue(CaseData caseData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(d -> d + "%")
            .ifPresent(deductionPercentage -> {
                DisposalHearingJudgementDeductionValue disposalValue = new DisposalHearingJudgementDeductionValue();
                disposalValue.setValue(deductionPercentage);
                caseData.setDisposalHearingJudgementDeductionValue(disposalValue);

                FastTrackJudgementDeductionValue fastTrackValue = new FastTrackJudgementDeductionValue();
                fastTrackValue.setValue(deductionPercentage);
                caseData.setFastTrackJudgementDeductionValue(fastTrackValue);

                SmallClaimsJudgementDeductionValue smallClaimsValue = new SmallClaimsJudgementDeductionValue();
                smallClaimsValue.setValue(deductionPercentage);
                caseData.setSmallClaimsJudgementDeductionValue(smallClaimsValue);
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

        if (getAllCourts) {
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

        if (isMultiOrIntermediateTrackClaim(caseData)
            && OrderType.DISPOSAL.equals(caseData.getOrderType())
            && CaseState.JUDICIAL_REFERRAL.equals(caseData.getCcdState())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_MINTI_DISPOSAL_NOT_ALLOWED))
                .build();
        }

        updateDeductionValue(caseData);
        caseData.setSetSmallClaimsFlag(NO);
        caseData.setSetFastTrackFlag(NO);
        caseData.setIsSdoR2NewScreen(NO);

        boolean isSmallClaimsTrack = SdoHelper.isSmallClaimsTrack(caseData);
        boolean isFastTrack = SdoHelper.isFastTrack(caseData);

        if (isSmallClaimsTrack) {
            caseData.setSetSmallClaimsFlag(YES);
            if (SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)) {
                caseData.setIsSdoR2NewScreen(YES);
            }
        } else if (isFastTrack) {
            caseData.setSetFastTrackFlag(YES);
            if (SdoHelper.isNihlFastTrack(caseData)) {
                caseData.setIsSdoR2NewScreen(YES);
            }
        }

        resetPpiFields(caseData, isSmallClaimsTrack, isFastTrack);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
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
        log.info("generateSdoOrder ccdCaseReference: {} legacyCaseReference: {}",
                 callbackParams.getCaseData().getCcdCaseReference(), callbackParams.getCaseData().getLegacyCaseReference());
        CaseData caseData = V_1.equals(callbackParams.getVersion())
            ? mapHearingMethodFields(callbackParams.getCaseData())
            : callbackParams.getCaseData();

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
        } else if (SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)) {
            errors.addAll(validateDRHFields(caseData));
        }

        if (SdoHelper.isNihlFastTrack(caseData)) {
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
                caseData.setSdoOrderDocument(document);
            }
            assignCategoryId.assignCategoryIdToCaseDocument(document, "caseManagementOrders");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseData.toMap(objectMapper))
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
        if (caseData.getHearingMethodValuesDisposalHearing() != null
            && caseData.getHearingMethodValuesDisposalHearing().getValue() != null) {
            String disposalHearingMethodLabel = caseData.getHearingMethodValuesDisposalHearing().getValue().getLabel();
            if (disposalHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                caseData.setDisposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                caseData.setDisposalHearingMethod(DisposalHearingMethod.disposalHearingMethodVideoConferenceHearing);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                caseData.setDisposalHearingMethod(DisposalHearingMethod.disposalHearingMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesFastTrack() != null
            && caseData.getHearingMethodValuesFastTrack().getValue() != null) {
            String fastTrackHearingMethodLabel = caseData.getHearingMethodValuesFastTrack().getValue().getLabel();
            if (fastTrackHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                caseData.setFastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                caseData.setFastTrackMethod(FastTrackMethod.fastTrackMethodVideoConferenceHearing);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                caseData.setFastTrackMethod(FastTrackMethod.fastTrackMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesSmallClaims() != null
            && caseData.getHearingMethodValuesSmallClaims().getValue() != null) {
            String smallClaimsHearingMethodLabel = caseData.getHearingMethodValuesSmallClaims().getValue().getLabel();
            if (smallClaimsHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                caseData.setSmallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                caseData.setSmallClaimsMethod(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                caseData.setSmallClaimsMethod(SmallClaimsMethod.smallClaimsMethodTelephoneHearing);
            }
        }

        return caseData;
    }

    private CallbackResponse submitSDO(CallbackParams callbackParams) {
        CaseData caseData = getSharedData(callbackParams);

        CaseDocument document = caseData.getSdoOrderDocument();
        if (document != null) {
            if (featureToggleService.isWelshEnabledForMainCase()
                && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual())) {
                List<Element<CaseDocument>> sdoDocuments = caseData.getPreTranslationDocuments();
                sdoDocuments.add(element(document));
                caseData.setPreTranslationDocuments(sdoDocuments);
            } else {
                List<Element<CaseDocument>> generatedDocuments = caseData.getSystemGeneratedCaseDocuments();
                generatedDocuments.add(element(document));
                caseData.setSystemGeneratedCaseDocuments(generatedDocuments);
            }
        }
        // null/remove preview SDO document, otherwise it will show as duplicate within case file view
        caseData.setSdoOrderDocument(null);

        caseData.setHearingNotes(getHearingNotes(caseData));
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            // LiP check ensures LiP cases will not automatically get whitelisted, and instead will have their own ea court check.
            boolean isLipCase = (caseData.isApplicantLiP() || caseData.isRespondent1LiP() || caseData.isRespondent2LiP());
            if (featureToggleService.isWelshEnabledForMainCase()) {
                caseData.setEaCourtLocation(YES);
            } else {
                if (!isLipCase) {
                    log.info("Case {} is whitelisted for case progression.", caseData.getCcdCaseReference());
                    caseData.setEaCourtLocation(YES);
                } else {
                    boolean isLipCaseEaCourt = isLipCaseWithProgressionEnabledAndCourtWhiteListed(caseData);
                    caseData.setEaCourtLocation(isLipCaseEaCourt ? YesOrNo.YES : YesOrNo.NO);
                }
            }
        }

        caseData.setDisposalHearingMethodInPerson(deleteLocationList(
            caseData.getDisposalHearingMethodInPerson()));
        caseData.setFastTrackMethodInPerson(deleteLocationList(
            caseData.getFastTrackMethodInPerson()));
        caseData.setSmallClaimsMethodInPerson(deleteLocationList(
            caseData.getSmallClaimsMethodInPerson()));
        if (SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)
            && caseData.getSdoR2SmallClaimsHearing() != null) {
            caseData.setSdoR2SmallClaimsHearing(updateHearingAfterDeletingLocationList(caseData.getSdoR2SmallClaimsHearing()));
        }
        setClaimsTrackBasedOnJudgeSelection(caseData);

        // Avoid location lists (listItems) from being saved in caseData, just save the selected values
        if (caseData.getSdoR2Trial() != null) {
            SdoR2Trial sdoR2Trial = caseData.getSdoR2Trial();
            if (caseData.getSdoR2Trial().getHearingCourtLocationList() != null) {
                DynamicList hearingList = new DynamicList();
                hearingList.setValue(caseData.getSdoR2Trial().getHearingCourtLocationList().getValue());
                sdoR2Trial.setHearingCourtLocationList(hearingList);
            }
            if (caseData.getSdoR2Trial().getAltHearingCourtLocationList() != null) {
                DynamicList altHearingList = new DynamicList();
                altHearingList.setValue(caseData.getSdoR2Trial().getAltHearingCourtLocationList().getValue());
                sdoR2Trial.setAltHearingCourtLocationList(altHearingList);
            }
            caseData.setSdoR2Trial(sdoR2Trial);
        }

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                caseData
            ));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private boolean isLipCaseWithProgressionEnabledAndCourtWhiteListed(CaseData caseData) {
        return (caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne()
            || (caseData.isLipvLROneVOne() && featureToggleService.isDefendantNoCOnlineForCase(caseData)))
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
    private void setClaimsTrackBasedOnJudgeSelection(CaseData caseData) {
        CaseCategory caseAccessCategory = caseData.getCaseAccessCategory();
        switch (caseAccessCategory) {
            case UNSPEC_CLAIM:// unspec use allocatedTrack to hold claims track value
                if (SdoHelper.isSmallClaimsTrack(caseData)) {
                    caseData.setAllocatedTrack(SMALL_CLAIM);
                } else if (SdoHelper.isFastTrack(caseData)) {
                    caseData.setAllocatedTrack(FAST_CLAIM);
                }
                break;
            case SPEC_CLAIM:// spec claims use responseClaimTrack to hold claims track value
                if (SdoHelper.isSmallClaimsTrack(caseData)) {
                    caseData.setResponseClaimTrack(SMALL_CLAIM.name());
                } else if (SdoHelper.isFastTrack(caseData)) {
                    caseData.setResponseClaimTrack(FAST_CLAIM.name());
                }
                break;
            default:
                break;
        }
    }

    private DynamicList deleteLocationList(DynamicList list) {
        if (isNull(list)) {
            return null;
        }
        DynamicList cleanedList = new DynamicList();
        cleanedList.setValue(list.getValue());
        return cleanedList;
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

    private CaseData getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        caseData.setBusinessProcess(BusinessProcess.ready(CREATE_SDO));

        return caseData;
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
        CaseData updatedData,
        List<OrderDetailsPagesSectionsToggle> checkList
    ) {
        updatedData.setFastTrackAltDisputeResolutionToggle(checkList);
        updatedData.setFastTrackVariationOfDirectionsToggle(checkList);
        updatedData.setFastTrackSettlementToggle(checkList);
        updatedData.setFastTrackDisclosureOfDocumentsToggle(checkList);
        updatedData.setFastTrackWitnessOfFactToggle(checkList);
        updatedData.setFastTrackSchedulesOfLossToggle(checkList);
        updatedData.setFastTrackCostsToggle(checkList);
        updatedData.setFastTrackTrialToggle(checkList);
        updatedData.setFastTrackTrialBundleToggle(checkList);
        updatedData.setFastTrackMethodToggle(checkList);
        updatedData.setDisposalHearingDisclosureOfDocumentsToggle(checkList);
        updatedData.setDisposalHearingWitnessOfFactToggle(checkList);
        updatedData.setDisposalHearingMedicalEvidenceToggle(checkList);
        updatedData.setDisposalHearingQuestionsToExpertsToggle(checkList);
        updatedData.setDisposalHearingSchedulesOfLossToggle(checkList);
        updatedData.setDisposalHearingFinalDisposalHearingToggle(checkList);
        updatedData.setDisposalHearingMethodToggle(checkList);
        updatedData.setDisposalHearingBundleToggle(checkList);
        updatedData.setDisposalHearingClaimSettlingToggle(checkList);
        updatedData.setDisposalHearingCostsToggle(checkList);
        updatedData.setSmallClaimsHearingToggle(checkList);
        updatedData.setSmallClaimsMethodToggle(checkList);
        updatedData.setSmallClaimsDocumentsToggle(checkList);
        updatedData.setSmallClaimsWitnessStatementToggle(checkList);
        updatedData.setSmallClaimsFlightDelayToggle(checkList);

        if (featureToggleService.isCarmEnabledForCase(updatedData)) {
            updatedData.setSmallClaimsMediationSectionToggle(checkList);
        }
    }

    private void setCheckListNihl(
        CaseData updatedData,
        List<IncludeInOrderToggle> includeInOrderToggle
    ) {
        SdoR2FastTrackAltDisputeResolution altDisputeResolution = new SdoR2FastTrackAltDisputeResolution();
        altDisputeResolution.setIncludeInOrderToggle(includeInOrderToggle);
        updatedData.setSdoAltDisputeResolution(altDisputeResolution);

        SdoR2VariationOfDirections variationOfDirections = new SdoR2VariationOfDirections();
        variationOfDirections.setIncludeInOrderToggle(includeInOrderToggle);
        updatedData.setSdoVariationOfDirections(variationOfDirections);

        SdoR2Settlement settlement = new SdoR2Settlement();
        settlement.setIncludeInOrderToggle(includeInOrderToggle);
        updatedData.setSdoR2Settlement(settlement);
        updatedData.setSdoR2DisclosureOfDocumentsToggle(includeInOrderToggle);
        updatedData.setSdoR2SeparatorWitnessesOfFactToggle(includeInOrderToggle);
        updatedData.setSdoR2SeparatorExpertEvidenceToggle(includeInOrderToggle);
        updatedData.setSdoR2SeparatorAddendumReportToggle(includeInOrderToggle);
        updatedData.setSdoR2SeparatorFurtherAudiogramToggle(includeInOrderToggle);
        updatedData.setSdoR2SeparatorQuestionsClaimantExpertToggle(includeInOrderToggle);
        updatedData.setSdoR2SeparatorPermissionToRelyOnExpertToggle(includeInOrderToggle);
        updatedData.setSdoR2SeparatorEvidenceAcousticEngineerToggle(includeInOrderToggle);
        updatedData.setSdoR2SeparatorQuestionsToEntExpertToggle(includeInOrderToggle);
        updatedData.setSdoR2ScheduleOfLossToggle(includeInOrderToggle);
        updatedData.setSdoR2SeparatorUploadOfDocumentsToggle(includeInOrderToggle);
        updatedData.setSdoR2TrialToggle(includeInOrderToggle);
        if (featureToggleService.isCarmEnabledForCase(updatedData)) {
            updatedData.setSdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
        }
    }

    private Optional<RequestedCourt> updateCaseManagementLocationIfLegalAdvisorSdo(CallbackParams callbackParams,
                                                                                   CaseData caseData) {
        Optional<RequestedCourt> preferredCourt;
        if (isSpecClaim1000OrLessAndCcmcc(ccmccAmount).test(caseData)) {
            preferredCourt = locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData);

            preferredCourt.ifPresent(requestedCourt -> {
                caseData.setCaseManagementLocation(requestedCourt.getCaseLocation());

                String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
                String epimmsId = requestedCourt.getCaseLocation().getBaseLocation();

                List<LocationRefData> locations = locationRefDataService.getCourtLocationsByEpimmsId(authToken, epimmsId);
                Optional.ofNullable(locations)
                    .orElseGet(Collections::emptyList)
                    .stream().findFirst()
                    .ifPresent(locationRefData -> caseData.setLocationName(locationRefData.getSiteName()));
            });

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

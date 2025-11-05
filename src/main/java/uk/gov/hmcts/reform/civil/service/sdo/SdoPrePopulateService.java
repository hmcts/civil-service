package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
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
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
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
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT;
import static uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle.SHOW;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

@Service
@RequiredArgsConstructor
public class SdoPrePopulateService {

    private static final String UPON_CONSIDERING =
        "Upon considering the claim form, particulars of claim, statements of case and Directions questionnaires";
    public static final String HEARING_TIME_TEXT_AFTER =
        "The claimant must by no later than 4 weeks before the hearing date, pay the court the "
            + "required hearing fee or submit a fully completed application for Help with Fees. \nIf the "
            + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
            + "struck without further order.";
    private static final String HEARING_CHANNEL = "HearingChannel";
    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";

    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final LocationHelper locationHelper;
    private final SdoLocationService sdoLocationService;
    private final SdoFeatureToggleService sdoFeatureToggleService;
    private final CategoryService categoryService;

    private final List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);
    private final List<IncludeInOrderToggle> includeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);
    static final String witnessStatementString = "This witness statement is limited to 10 pages per party, including any appendices.";
    static final String laterThanFourPmString = "later than 4pm on";
    static final String claimantEvidenceString = "and the claimant's evidence in reply if so advised to be uploaded by 4pm on";
    static final String partiesLiaseString = "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no ";

    @Value("${genApp.lrd.ccmcc.amountPounds}")
    BigDecimal ccmccAmount;
    @Value("${court-location.unspecified-claim.epimms-id}")
    String ccmccEpimsId;

    public CaseData prePopulate(DirectionsOrderTaskContext context) {
        CaseData caseData = context.caseData();
        CallbackParams callbackParams = context.callbackParams();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        initialiseTrackDefaults(updatedData);
        applyFeatureFlags(caseData, updatedData);

        Optional<RequestedCourt> preferredCourt = updateCaseManagementLocationIfLegalAdvisorSdo(updatedData, caseData);

        DynamicList hearingMethodList = getDynamicHearingMethodList(callbackParams, caseData);
        applyVersionSpecificHearingDefaults(callbackParams, updatedData, hearingMethodList);

        List<LocationRefData> locationRefDataList = populateHearingLocations(preferredCourt, authToken, updatedData);

        List<OrderDetailsPagesSectionsToggle> checkList = List.of(SHOW);
        setCheckList(updatedData, checkList);
        updateDeductionValue(caseData, updatedData);

        populateDisposalOrderDetails(updatedData);
        populateFastTrackOrderDetails(updatedData);
        populateSmallClaimsOrderDetails(caseData, updatedData, checkList);

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
        return updatedData.build();
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
        updatedData.fastTrackTrialBundleToggle(checkList);
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
        updatedData.smallClaimsFlightDelayToggle(checkList);

        if (sdoFeatureToggleService.isCarmEnabled(updatedData.build())) {
            updatedData.smallClaimsMediationSectionToggle(checkList);
        }
    }

    private void initialiseTrackDefaults(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData
            .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
            .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
    }

    private void applyFeatureFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (sdoFeatureToggleService.isCarmEnabled(caseData)) {
            updatedData.showCarmFields(YES);
        } else {
            updatedData.showCarmFields(NO);
        }

        if (sdoFeatureToggleService.isWelshJourneyEnabled(caseData)) {
            updatedData.bilingualHint(YesOrNo.YES);
        }
    }

    private void applyVersionSpecificHearingDefaults(CallbackParams callbackParams,
                                                     CaseData.CaseDataBuilder<?, ?> updatedData,
                                                     DynamicList hearingMethodList) {
        if (V_1.equals(callbackParams.getVersion())) {
            DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream()
                .filter(elem -> elem.getLabel().equals(HearingMethod.IN_PERSON.getLabel()))
                .findFirst()
                .orElse(null);
            hearingMethodList.setValue(hearingMethodInPerson);
            updatedData.hearingMethodValuesFastTrack(hearingMethodList);
            updatedData.hearingMethodValuesDisposalHearing(hearingMethodList);
            updatedData.hearingMethodValuesSmallClaims(hearingMethodList);
        }
    }

    private List<LocationRefData> populateHearingLocations(Optional<RequestedCourt> preferredCourt,
                                                           String authToken,
                                                           CaseData.CaseDataBuilder<?, ?> updatedData) {
        List<LocationRefData> locationRefDataList = sdoLocationService.fetchHearingLocations(authToken);
        DynamicList locationsList = sdoLocationService.buildLocationList(
            preferredCourt.orElse(null), false, locationRefDataList);
        updatedData.disposalHearingMethodInPerson(locationsList);
        updatedData.fastTrackMethodInPerson(locationsList);
        updatedData.smallClaimsMethodInPerson(locationsList);
        return locationRefDataList;
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
        if (sdoFeatureToggleService.isCarmEnabled(updatedData.build())) {
            updatedData.sdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
        }
    }

    private void populateDisposalOrderDetails(CaseData.CaseDataBuilder<?, ?> updatedData) {
        DisposalHearingJudgesRecital tempDisposalHearingJudgesRecital = DisposalHearingJudgesRecital.builder()
            .input(UPON_CONSIDERING)
            .build();

        updatedData.disposalHearingJudgesRecital(tempDisposalHearingJudgesRecital).build();

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
    }

    private void populateFastTrackOrderDetails(CaseData.CaseDataBuilder<?, ?> updatedData) {
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
        updatedData.sdoR2FastTrackWitnessOfFact(getSdoR2WitnessOfFact()).build();

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

        updatedData.fastTrackClinicalNegligence(tempFastTrackClinicalNegligence).build();

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
            .input5("If the parties fail to agree basic hire rates pursuant to the paragraph above, "
                        + "each party may rely upon written evidence by way of witness statement of one witness to"
                        + " provide evidence of basic hire rates available within the claimant's geographical location,"
                        + " from a mainstream supplier, or a local reputable supplier if none is available.")
            .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
            .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
            .input7(claimantEvidenceString)
            .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
            .input8(witnessStatementString)
            .detailsShowToggle(addOrRemoveToggleList)
            .sdoR2FastTrackCreditHireDetails(tempSdoR2FastTrackCreditHireDetails)
            .build();

        updatedData.sdoR2FastTrackCreditHire(tempSdoR2FastTrackCreditHire).build();

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
    }

    private void populateSmallClaimsOrderDetails(CaseData caseData,
                                                 CaseData.CaseDataBuilder<?, ?> updatedData,
                                                 List<OrderDetailsPagesSectionsToggle> checkList) {
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

        if (sdoFeatureToggleService.isCarmEnabled(caseData)) {
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

        if (CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
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
    }

    private Optional<RequestedCourt> updateCaseManagementLocationIfLegalAdvisorSdo(
        CaseData.CaseDataBuilder<?, ?> updatedData,
        CaseData caseData
    ) {
        Optional<RequestedCourt> preferredCourt;
        if (isSpecClaim1000OrLessAndCcmcc(ccmccAmount).test(caseData)) {
            preferredCourt = locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData);
            preferredCourt.map(RequestedCourt::getCaseLocation)
                .ifPresent(updatedData::caseManagementLocation);
            return preferredCourt;
        } else {
            return locationHelper.getCaseManagementLocation(caseData);
        }
    }

    private Predicate<CaseData> isSpecClaim1000OrLessAndCcmcc(BigDecimal threshold) {
        return caseData ->
            CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && threshold.compareTo(caseData.getTotalClaimAmount()) >= 0
                && caseData.getCaseManagementLocation().getBaseLocation().equals(ccmccEpimsId);
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
        DynamicList courtList = sdoLocationService.buildCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
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
                                                .altHearingCourtLocationList(sdoLocationService.buildLocationList(
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
        if (sdoFeatureToggleService.isCarmEnabled(caseData)) {
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
        DynamicList trialCourtList = sdoLocationService.buildCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        if (trialCourtList != null && trialCourtList.getListItems() != null && !trialCourtList.getListItems().isEmpty()) {
            trialCourtList.setValue(trialCourtList.getListItems().get(0));
        }

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
                                   .hearingCourtLocationList(trialCourtList)

                                   .altHearingCourtLocationList(sdoLocationService.buildAlternativeCourtLocations(locationRefDataList))
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

}

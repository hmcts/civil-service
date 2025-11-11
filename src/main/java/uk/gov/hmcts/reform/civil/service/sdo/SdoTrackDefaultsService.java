package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SdoTrackDefaultsService {

    private final SdoDeadlineService sdoDeadlineService;
    private final SdoJourneyToggleService sdoJourneyToggleService;
    private final SdoDisposalOrderDefaultsService sdoDisposalOrderDefaultsService;
    private final SdoFastTrackOrderDefaultsService sdoFastTrackOrderDefaultsService;
    private final SdoSmallClaimsOrderDefaultsService sdoSmallClaimsOrderDefaultsService;

    private static final List<IncludeInOrderToggle> INCLUDE_IN_ORDER_TOGGLE = List.of(IncludeInOrderToggle.INCLUDE);

    public void applyBaseTrackDefaults(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        initialiseTrackDefaults(updatedData);
        sdoJourneyToggleService.applyJourneyFlags(caseData, updatedData);

        List<OrderDetailsPagesSectionsToggle> checkList = List.of(OrderDetailsPagesSectionsToggle.SHOW);
        setCheckList(caseData, updatedData, checkList);
        updateDeductionValue(caseData, updatedData);

        sdoDisposalOrderDefaultsService.populateDisposalOrderDetails(updatedData);
        sdoFastTrackOrderDefaultsService.populateFastTrackOrderDetails(updatedData);
        sdoSmallClaimsOrderDefaultsService.populateSmallClaimsOrderDetails(caseData, updatedData, checkList);

        updateExpertEvidenceFields(updatedData);
        updateDisclosureOfDocumentFields(updatedData);
    }

    public void applyR2Defaults(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        setCheckListNihl(caseData, updatedData, INCLUDE_IN_ORDER_TOGGLE);
        updatedData.sdoR2FastTrackUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
            SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
        updatedData.sdoR2SmallClaimsUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
            SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
        updatedData.sdoR2DisposalHearingUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
            SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
    }

    public List<IncludeInOrderToggle> defaultIncludeInOrderToggle() {
        return INCLUDE_IN_ORDER_TOGGLE;
    }

    private void setCheckList(
        CaseData caseData,
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

        sdoJourneyToggleService.applySmallClaimsChecklistToggle(caseData, updatedData, checkList);
    }


    private void initialiseTrackDefaults(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData
            .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
            .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
    }


    private void setCheckListNihl(
        CaseData caseData,
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
        sdoJourneyToggleService.applyR2SmallClaimsMediation(caseData, updatedData, includeInOrderToggle);
    }


    private void updateExpertEvidenceFields(CaseData.CaseDataBuilder<?, ?> updatedData) {
        FastTrackPersonalInjury tempFastTrackPersonalInjury = FastTrackPersonalInjury.builder()
            .input1("The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                        + " Digital Portal with the particulars of claim")
            .input2("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert " +
                        "directly and uploaded to the Digital Portal by 4pm on")
            .date2(sdoDeadlineService.nextWorkingDayFromNowDays(14))
            .input3("The answers to the questions shall be answered by the Expert by")
            .date3(sdoDeadlineService.nextWorkingDayFromNowDays(42))
            .input4("and uploaded to the Digital Portal by the party who has asked the question by")
            .date4(sdoDeadlineService.nextWorkingDayFromNowDays(49))
            .build();

        updatedData.fastTrackPersonalInjury(tempFastTrackPersonalInjury).build();
    }


    private void updateDisclosureOfDocumentFields(CaseData.CaseDataBuilder<?, ?> updatedData) {
        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
            .input1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on")
            .date1(sdoDeadlineService.nextWorkingDayFromNowWeeks(4))
            .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on")
            .date2(sdoDeadlineService.nextWorkingDayFromNowWeeks(5))
            .input3("Requests will be complied with within 7 days of the receipt of the request.")
            .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on")
            .date3(sdoDeadlineService.nextWorkingDayFromNowWeeks(8))
            .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();
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

}

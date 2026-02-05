package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;

import java.util.List;

/**
 * Applies the repeating checklist/IncludeInOrder toggles for each track so the track defaults
 * service can orchestrate without rebuilding the same toggle lists.
 */
@Service
@RequiredArgsConstructor
public class SdoChecklistService {

    private final SdoJourneyToggleService sdoJourneyToggleService;

    public void applyOrderChecklists(
        CaseData caseData,
        List<OrderDetailsPagesSectionsToggle> checkList
    ) {
        caseData.setFastTrackAltDisputeResolutionToggle(checkList);
        caseData.setFastTrackVariationOfDirectionsToggle(checkList);
        caseData.setFastTrackSettlementToggle(checkList);
        caseData.setFastTrackDisclosureOfDocumentsToggle(checkList);
        caseData.setFastTrackWitnessOfFactToggle(checkList);
        caseData.setFastTrackSchedulesOfLossToggle(checkList);
        caseData.setFastTrackCostsToggle(checkList);
        caseData.setFastTrackTrialToggle(checkList);
        caseData.setFastTrackTrialBundleToggle(checkList);
        caseData.setFastTrackMethodToggle(checkList);
        caseData.setDisposalHearingDisclosureOfDocumentsToggle(checkList);
        caseData.setDisposalHearingWitnessOfFactToggle(checkList);
        caseData.setDisposalHearingMedicalEvidenceToggle(checkList);
        caseData.setDisposalHearingQuestionsToExpertsToggle(checkList);
        caseData.setDisposalHearingSchedulesOfLossToggle(checkList);
        caseData.setDisposalHearingFinalDisposalHearingToggle(checkList);
        caseData.setDisposalHearingMethodToggle(checkList);
        caseData.setDisposalHearingBundleToggle(checkList);
        caseData.setDisposalHearingClaimSettlingToggle(checkList);
        caseData.setDisposalHearingCostsToggle(checkList);
        caseData.setSmallClaimsHearingToggle(checkList);
        caseData.setSmallClaimsMethodToggle(checkList);
        caseData.setSmallClaimsDocumentsToggle(checkList);
        caseData.setSmallClaimsWitnessStatementToggle(checkList);
        caseData.setSmallClaimsFlightDelayToggle(checkList);

        sdoJourneyToggleService.applySmallClaimsChecklistToggle(caseData, checkList);
    }

    public void applyR2Checklists(
        CaseData caseData,
        List<IncludeInOrderToggle> includeInOrderToggle
    ) {
        caseData.setSdoAltDisputeResolution(
            SdoR2FastTrackAltDisputeResolution.builder()
                .includeInOrderToggle(includeInOrderToggle)
                .build()
        );
        caseData.setSdoVariationOfDirections(
            SdoR2VariationOfDirections.builder()
                .includeInOrderToggle(includeInOrderToggle)
                .build()
        );
        caseData.setSdoR2Settlement(
            SdoR2Settlement.builder()
                .includeInOrderToggle(includeInOrderToggle)
                .build()
        );
        caseData.setSdoR2DisclosureOfDocumentsToggle(includeInOrderToggle);
        caseData.setSdoR2SeparatorWitnessesOfFactToggle(includeInOrderToggle);
        caseData.setSdoR2SeparatorExpertEvidenceToggle(includeInOrderToggle);
        caseData.setSdoR2SeparatorAddendumReportToggle(includeInOrderToggle);
        caseData.setSdoR2SeparatorFurtherAudiogramToggle(includeInOrderToggle);
        caseData.setSdoR2SeparatorQuestionsClaimantExpertToggle(includeInOrderToggle);
        caseData.setSdoR2SeparatorPermissionToRelyOnExpertToggle(includeInOrderToggle);
        caseData.setSdoR2SeparatorEvidenceAcousticEngineerToggle(includeInOrderToggle);
        caseData.setSdoR2SeparatorQuestionsToEntExpertToggle(includeInOrderToggle);
        caseData.setSdoR2ScheduleOfLossToggle(includeInOrderToggle);
        caseData.setSdoR2SeparatorUploadOfDocumentsToggle(includeInOrderToggle);
        caseData.setSdoR2TrialToggle(includeInOrderToggle);

        sdoJourneyToggleService.applyR2SmallClaimsMediation(caseData, includeInOrderToggle);
    }
}

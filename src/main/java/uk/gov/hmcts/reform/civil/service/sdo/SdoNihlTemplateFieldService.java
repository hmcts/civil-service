package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import java.util.List;
import java.util.Optional;

@Service
public class SdoNihlTemplateFieldService {

    public boolean hasAltDisputeResolution(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoAltDisputeResolution())
            .map(SdoR2FastTrackAltDisputeResolution::getIncludeInOrderToggle)
            .map(this::hasToggle)
            .orElse(false);
    }

    public boolean hasVariationOfDirections(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoVariationOfDirections())
            .map(SdoR2VariationOfDirections::getIncludeInOrderToggle)
            .map(this::hasToggle)
            .orElse(false);
    }

    public boolean hasSettlement(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoR2Settlement())
            .map(SdoR2Settlement::getIncludeInOrderToggle)
            .map(this::hasToggle)
            .orElse(false);
    }

    public boolean hasDisclosureOfDocuments(CaseData caseData) {
        return hasToggle(caseData.getSdoR2DisclosureOfDocumentsToggle());
    }

    public boolean hasWitnessOfFact(CaseData caseData) {
        return hasToggle(caseData.getSdoR2SeparatorWitnessesOfFactToggle());
    }

    public boolean hasExpertEvidence(CaseData caseData) {
        return hasToggle(caseData.getSdoR2SeparatorExpertEvidenceToggle());
    }

    public boolean hasAddendumReport(CaseData caseData) {
        return hasToggle(caseData.getSdoR2SeparatorAddendumReportToggle());
    }

    public boolean hasFurtherAudiogram(CaseData caseData) {
        return hasToggle(caseData.getSdoR2SeparatorFurtherAudiogramToggle());
    }

    public boolean hasQuestionsOfClaimantExpert(CaseData caseData) {
        return hasToggle(caseData.getSdoR2SeparatorQuestionsClaimantExpertToggle());
    }

    public boolean hasPermissionFromEntExpert(CaseData caseData) {
        return hasToggle(caseData.getSdoR2SeparatorPermissionToRelyOnExpertToggle());
    }

    public boolean hasEvidenceFromAcousticEngineer(CaseData caseData) {
        return hasToggle(caseData.getSdoR2SeparatorEvidenceAcousticEngineerToggle());
    }

    public boolean hasQuestionsToEntAfterReport(CaseData caseData) {
        return hasToggle(caseData.getSdoR2SeparatorQuestionsToEntExpertToggle());
    }

    public boolean hasScheduleOfLoss(CaseData caseData) {
        return hasToggle(caseData.getSdoR2ScheduleOfLossToggle());
    }

    public boolean hasUploadDocuments(CaseData caseData) {
        return hasToggle(caseData.getSdoR2SeparatorUploadOfDocumentsToggle());
    }

    public boolean hasTrial(CaseData caseData) {
        return hasToggle(caseData.getSdoR2TrialToggle());
    }

    public boolean hasNewDirections(CaseData caseData) {
        List<?> directions = caseData.getSdoR2AddNewDirection();
        return directions != null && !directions.isEmpty();
    }

    public boolean hasTrialWindow(CaseData caseData) {
        if (!hasTrial(caseData)) {
            return false;
        }
        return Optional.ofNullable(caseData.getSdoR2Trial())
            .map(SdoR2Trial::getTrialOnOptions)
            .filter(TrialOnRadioOptions.TRIAL_WINDOW::equals)
            .isPresent();
    }

    public boolean hasTrialPhysicalBundleParty(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoR2Trial())
            .map(SdoR2Trial::getPhysicalBundleOptions)
            .filter(PhysicalTrialBundleOptions.PARTY::equals)
            .isPresent();
    }

    public boolean hasWelshLanguageToggle(CaseData caseData) {
        return hasToggle(caseData.getSdoR2NihlUseOfWelshIncludeInOrderToggle());
    }

    public String getWelshLanguageDescription(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoR2NihlUseOfWelshLanguage())
            .map(SdoR2WelshLanguageUsage::getDescription)
            .orElse(null);
    }

    private boolean hasToggle(List<IncludeInOrderToggle> toggles) {
        return toggles != null && !toggles.isEmpty();
    }

}

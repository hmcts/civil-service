package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFastNihl;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoNihlTemplateFieldService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoR2TrialTemplateFieldService;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SdoNihlTemplateService {

    private final DocumentHearingLocationHelper locationHelper;
    private final SdoCaseClassificationService caseClassificationService;
    private final SdoR2TrialTemplateFieldService trialTemplateFieldService;
    private final SdoNihlTemplateFieldService nihlTemplateFieldService;

    public SdoDocumentFormFastNihl buildTemplate(
        CaseData caseData,
        String judgeName,
        boolean isJudge,
        String authorisation
    ) {
        SdoDocumentFormFastNihl template = new SdoDocumentFormFastNihl()
            .setWrittenByJudge(isJudge)
            .setCurrentDate(LocalDate.now())
            .setJudgeName(judgeName)
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setApplicant1(caseData.getApplicant1())
            .setHasApplicant2(caseClassificationService.hasApplicant2(caseData))
            .setApplicant2(caseData.getApplicant2())
            .setRespondent1(caseData.getRespondent1())
            .setHasRespondent2(caseClassificationService.hasRespondent2(caseData))
            .setRespondent2(caseData.getRespondent2())
            .setDrawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .setDrawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .setClaimsTrack(caseData.getClaimsTrack())
            .setFastClaims(caseData.getFastClaims())
            .setSdoFastTrackJudgesRecital(caseData.getSdoFastTrackJudgesRecital())
            .setSdoR2DisclosureOfDocuments(caseData.getSdoR2DisclosureOfDocuments())
            .setSdoR2WitnessesOfFact(caseData.getSdoR2WitnessesOfFact())
            .setSdoR2ExpertEvidence(caseData.getSdoR2ExpertEvidence())
            .setSdoR2AddendumReport(caseData.getSdoR2AddendumReport())
            .setSdoR2FurtherAudiogram(caseData.getSdoR2FurtherAudiogram())
            .setSdoR2QuestionsClaimantExpert(caseData.getSdoR2QuestionsClaimantExpert())
            .setSdoR2PermissionToRelyOnExpert(caseData.getSdoR2PermissionToRelyOnExpert())
            .setSdoR2EvidenceAcousticEngineer(caseData.getSdoR2EvidenceAcousticEngineer())
            .setSdoR2QuestionsToEntExpert(caseData.getSdoR2QuestionsToEntExpert())
            .setSdoR2ScheduleOfLoss(caseData.getSdoR2ScheduleOfLoss())
            .setSdoR2UploadOfDocuments(caseData.getSdoR2UploadOfDocuments())
            .setSdoR2AddNewDirection(caseData.getSdoR2AddNewDirection())
            .setSdoR2Trial(caseData.getSdoR2Trial())
            .setSdoR2ImportantNotesTxt(caseData.getSdoR2ImportantNotesTxt())
            .setSdoR2ImportantNotesDate(caseData.getSdoR2ImportantNotesDate())
            .setHasAltDisputeResolution(nihlTemplateFieldService.hasAltDisputeResolution(caseData))
            .setHasVariationOfDirections(nihlTemplateFieldService.hasVariationOfDirections(caseData))
            .setHasSettlement(nihlTemplateFieldService.hasSettlement(caseData))
            .setHasDisclosureOfDocuments(nihlTemplateFieldService.hasDisclosureOfDocuments(caseData))
            .setHasWitnessOfFact(nihlTemplateFieldService.hasWitnessOfFact(caseData))
            .setHasRestrictWitness(trialTemplateFieldService.hasRestrictWitness(caseData))
            .setHasRestrictPages(trialTemplateFieldService.hasRestrictPages(caseData))
            .setHasExpertEvidence(nihlTemplateFieldService.hasExpertEvidence(caseData))
            .setHasAddendumReport(nihlTemplateFieldService.hasAddendumReport(caseData))
            .setHasFurtherAudiogram(nihlTemplateFieldService.hasFurtherAudiogram(caseData))
            .setHasQuestionsOfClaimantExpert(nihlTemplateFieldService.hasQuestionsOfClaimantExpert(caseData))
            .setIsApplicationToRelyOnFurther(
                trialTemplateFieldService.hasApplicationToRelyOnFurther(caseData) ? "Yes" : "No"
            )
            .setHasPermissionFromENT(nihlTemplateFieldService.hasPermissionFromEntExpert(caseData))
            .setHasEvidenceFromAcousticEngineer(nihlTemplateFieldService.hasEvidenceFromAcousticEngineer(caseData))
            .setHasQuestionsToENTAfterReport(nihlTemplateFieldService.hasQuestionsToEntAfterReport(caseData))
            .setHasScheduleOfLoss(nihlTemplateFieldService.hasScheduleOfLoss(caseData))
            .setHasClaimForPecuniaryLoss(trialTemplateFieldService.hasClaimForPecuniaryLoss(caseData))
            .setHasUploadDocuments(nihlTemplateFieldService.hasUploadDocuments(caseData))
            .setHasSdoTrial(nihlTemplateFieldService.hasTrial(caseData))
            .setHasNewDirections(nihlTemplateFieldService.hasNewDirections(caseData))
            .setSdoR2AddNewDirection(caseData.getSdoR2AddNewDirection())
            .setHasSdoR2TrialWindow(nihlTemplateFieldService.hasTrialWindow(caseData))
            .setSdoTrialHearingTimeAllocated(trialTemplateFieldService.getTrialHearingTimeAllocated(caseData))
            .setSdoTrialMethodOfHearing(trialTemplateFieldService.getTrialMethodOfHearing(caseData))
            .setHasSdoR2TrialPhysicalBundleParty(nihlTemplateFieldService.hasTrialPhysicalBundleParty(caseData))
            .setPhysicalBundlePartyTxt(trialTemplateFieldService.getPhysicalBundlePartyText(caseData))
            .setHasNihlWelshLangToggle(nihlTemplateFieldService.hasWelshLanguageToggle(caseData))
            .setWelshLanguageDescription(nihlTemplateFieldService.getWelshLanguageDescription(caseData));

        if (caseData.getSdoR2Trial() != null) {
            template.setHearingLocation(locationHelper.getHearingLocation(
                Optional.ofNullable(trialTemplateFieldService.getHearingLocation(caseData))
                    .map(DynamicList::getValue)
                    .map(DynamicListElement::getLabel)
                    .orElse(null),
                caseData,
                authorisation
            ));
        }

        template.setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return template;
    }
}

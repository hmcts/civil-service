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
        SdoDocumentFormFastNihl.SdoDocumentFormFastNihlBuilder builder = SdoDocumentFormFastNihl.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(caseClassificationService.hasApplicant2(caseData))
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(caseClassificationService.hasRespondent2(caseData))
            .respondent2(caseData.getRespondent2())
            .drawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .drawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .claimsTrack(caseData.getClaimsTrack())
            .fastClaims(caseData.getFastClaims())
            .sdoFastTrackJudgesRecital(caseData.getSdoFastTrackJudgesRecital())
            .sdoR2DisclosureOfDocuments(caseData.getSdoR2DisclosureOfDocuments())
            .sdoR2WitnessesOfFact(caseData.getSdoR2WitnessesOfFact())
            .sdoR2ExpertEvidence(caseData.getSdoR2ExpertEvidence())
            .sdoR2AddendumReport(caseData.getSdoR2AddendumReport())
            .sdoR2FurtherAudiogram(caseData.getSdoR2FurtherAudiogram())
            .sdoR2QuestionsClaimantExpert(caseData.getSdoR2QuestionsClaimantExpert())
            .sdoR2PermissionToRelyOnExpert(caseData.getSdoR2PermissionToRelyOnExpert())
            .sdoR2EvidenceAcousticEngineer(caseData.getSdoR2EvidenceAcousticEngineer())
            .sdoR2QuestionsToEntExpert(caseData.getSdoR2QuestionsToEntExpert())
            .sdoR2ScheduleOfLoss(caseData.getSdoR2ScheduleOfLoss())
            .sdoR2UploadOfDocuments(caseData.getSdoR2UploadOfDocuments())
            .sdoR2AddNewDirection(caseData.getSdoR2AddNewDirection())
            .sdoR2Trial(caseData.getSdoR2Trial())
            .sdoR2ImportantNotesTxt(caseData.getSdoR2ImportantNotesTxt())
            .sdoR2ImportantNotesDate(caseData.getSdoR2ImportantNotesDate())
            .hasAltDisputeResolution(nihlTemplateFieldService.hasAltDisputeResolution(caseData))
            .hasVariationOfDirections(nihlTemplateFieldService.hasVariationOfDirections(caseData))
            .hasSettlement(nihlTemplateFieldService.hasSettlement(caseData))
            .hasDisclosureOfDocuments(nihlTemplateFieldService.hasDisclosureOfDocuments(caseData))
            .hasWitnessOfFact(nihlTemplateFieldService.hasWitnessOfFact(caseData))
            .hasRestrictWitness(trialTemplateFieldService.hasRestrictWitness(caseData))
            .hasRestrictPages(trialTemplateFieldService.hasRestrictPages(caseData))
            .hasExpertEvidence(nihlTemplateFieldService.hasExpertEvidence(caseData))
            .hasAddendumReport(nihlTemplateFieldService.hasAddendumReport(caseData))
            .hasFurtherAudiogram(nihlTemplateFieldService.hasFurtherAudiogram(caseData))
            .hasQuestionsOfClaimantExpert(nihlTemplateFieldService.hasQuestionsOfClaimantExpert(caseData))
            .isApplicationToRelyOnFurther(
                trialTemplateFieldService.hasApplicationToRelyOnFurther(caseData) ? "Yes" : "No"
            )
            .hasPermissionFromENT(nihlTemplateFieldService.hasPermissionFromEntExpert(caseData))
            .hasEvidenceFromAcousticEngineer(nihlTemplateFieldService.hasEvidenceFromAcousticEngineer(caseData))
            .hasQuestionsToENTAfterReport(nihlTemplateFieldService.hasQuestionsToEntAfterReport(caseData))
            .hasScheduleOfLoss(nihlTemplateFieldService.hasScheduleOfLoss(caseData))
            .hasClaimForPecuniaryLoss(trialTemplateFieldService.hasClaimForPecuniaryLoss(caseData))
            .hasUploadDocuments(nihlTemplateFieldService.hasUploadDocuments(caseData))
            .hasSdoTrial(nihlTemplateFieldService.hasTrial(caseData))
            .hasNewDirections(nihlTemplateFieldService.hasNewDirections(caseData))
            .sdoR2AddNewDirection(caseData.getSdoR2AddNewDirection())
            .hasSdoR2TrialWindow(nihlTemplateFieldService.hasTrialWindow(caseData))
            .sdoTrialHearingTimeAllocated(trialTemplateFieldService.getTrialHearingTimeAllocated(caseData))
            .sdoTrialMethodOfHearing(trialTemplateFieldService.getTrialMethodOfHearing(caseData))
            .hasSdoR2TrialPhysicalBundleParty(nihlTemplateFieldService.hasTrialPhysicalBundleParty(caseData))
            .physicalBundlePartyTxt(trialTemplateFieldService.getPhysicalBundlePartyText(caseData))
            .hasNihlWelshLangToggle(nihlTemplateFieldService.hasWelshLanguageToggle(caseData))
            .welshLanguageDescription(nihlTemplateFieldService.getWelshLanguageDescription(caseData));

        if (caseData.getSdoR2Trial() != null) {
            builder.hearingLocation(locationHelper.getHearingLocation(
                Optional.ofNullable(trialTemplateFieldService.getHearingLocation(caseData))
                    .map(DynamicList::getValue)
                    .map(DynamicListElement::getLabel)
                    .orElse(null),
                caseData,
                authorisation
            ));
        }

        builder.caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return builder.build();
    }
}

package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoDisposalDirectionsService;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SdoDisposalTemplateService {

    private final DocumentHearingLocationHelper locationHelper;
    private final SdoCaseClassificationService caseClassificationService;
    private final SdoDisposalDirectionsService disposalDirectionsService;

    public SdoDocumentFormDisposal buildTemplate(
        CaseData caseData,
        String judgeName,
        boolean isJudge,
        String authorisation
    ) {
        SdoDocumentFormDisposal template = new SdoDocumentFormDisposal()
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
            .setDisposalHearingJudgesRecital(caseData.getDisposalHearingJudgesRecital())
            .setDisposalHearingDisclosureOfDocuments(caseData.getDisposalHearingDisclosureOfDocuments())
            .setDisposalHearingWitnessOfFact(caseData.getDisposalHearingWitnessOfFact())
            .setDisposalHearingMedicalEvidence(caseData.getDisposalHearingMedicalEvidence())
            .setDisposalHearingQuestionsToExperts(caseData.getDisposalHearingQuestionsToExperts())
            .setDisposalHearingSchedulesOfLoss(caseData.getDisposalHearingSchedulesOfLoss())
            .setDisposalHearingFinalDisposalHearing(caseData.getDisposalHearingFinalDisposalHearing())
            .setDisposalHearingFinalDisposalHearingTime(disposalDirectionsService.getFinalHearingTimeLabel(caseData))
            .setDisposalHearingMethod(caseData.getDisposalHearingMethod())
            .setDisposalHearingMethodInPerson(caseData.getDisposalHearingMethodInPerson())
            .setDisposalHearingMethodTelephoneHearing(disposalDirectionsService.getTelephoneHearingLabel(caseData))
            .setDisposalHearingMethodVideoConferenceHearing(
                disposalDirectionsService.getVideoConferenceHearingLabel(caseData)
            )
            .setDisposalHearingBundle(caseData.getDisposalHearingBundle())
            .setDisposalHearingBundleTypeText(disposalDirectionsService.getBundleTypeText(caseData))
            .setHasNewDirections(disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingAddNewDirections"))
            .setDisposalHearingAddNewDirections(caseData.getDisposalHearingAddNewDirections())
            .setDisposalHearingNotes(caseData.getDisposalHearingNotes())
            .setDisposalHearingDisclosureOfDocumentsToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingDisclosureOfDocumentsToggle")
            )
            .setDisposalHearingWitnessOfFactToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle")
            )
            .setDisposalHearingMedicalEvidenceToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingMedicalEvidenceToggle")
            )
            .setDisposalHearingQuestionsToExpertsToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingQuestionsToExpertsToggle")
            )
            .setDisposalHearingSchedulesOfLossToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingSchedulesOfLossToggle")
            )
            .setDisposalHearingFinalDisposalHearingToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingFinalDisposalHearingToggle")
            )
            .setDisposalHearingMethodToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingMethodToggle")
            )
            .setDisposalHearingBundleToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingBundleToggle")
            )
            .setDisposalHearingClaimSettlingToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingClaimSettlingToggle")
            )
            .setDisposalHearingCostsToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingCostsToggle")
            )
            .setDisposalOrderWithoutHearing(caseData.getDisposalOrderWithoutHearing())
            .setDisposalHearingTime(caseData.getDisposalHearingHearingTime())
            .setHasDisposalWelshToggle(caseData.getSdoR2DisposalHearingUseOfWelshToggle() != null)
            .setWelshLanguageDescription(
                Optional.ofNullable(caseData.getSdoR2DisposalHearingUseOfWelshLanguage())
                    .map(SdoR2WelshLanguageUsage::getDescription)
                    .orElse(null)
            );

        Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel)
            .ifPresent(template::setDisposalHearingTimeEstimate);

        Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .filter(hearingTime -> DisposalHearingFinalDisposalHearingTimeEstimate.OTHER.equals(hearingTime.getTime()))
            .ifPresent(hearingTime -> template.setDisposalHearingTimeEstimate(
                String.format("%s hours %s minutes", hearingTime.getOtherHours(), hearingTime.getOtherMinutes())
            ));

        template
            .setHearingLocation(
                locationHelper.getHearingLocation(
                    Optional.ofNullable(caseData.getDisposalHearingMethodInPerson())
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ))
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return template;
    }
}

package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
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
        SdoDocumentFormDisposal.SdoDocumentFormDisposalBuilder builder = SdoDocumentFormDisposal.builder()
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
            .disposalHearingJudgesRecital(caseData.getDisposalHearingJudgesRecital())
            .disposalHearingDisclosureOfDocuments(caseData.getDisposalHearingDisclosureOfDocuments())
            .disposalHearingWitnessOfFact(caseData.getDisposalHearingWitnessOfFact())
            .disposalHearingMedicalEvidence(caseData.getDisposalHearingMedicalEvidence())
            .disposalHearingQuestionsToExperts(caseData.getDisposalHearingQuestionsToExperts())
            .disposalHearingSchedulesOfLoss(caseData.getDisposalHearingSchedulesOfLoss())
            .disposalHearingFinalDisposalHearing(caseData.getDisposalHearingFinalDisposalHearing())
            .disposalHearingFinalDisposalHearingTime(disposalDirectionsService.getFinalHearingTimeLabel(caseData))
            .disposalHearingMethod(caseData.getDisposalHearingMethod())
            .disposalHearingMethodInPerson(caseData.getDisposalHearingMethodInPerson())
            .disposalHearingMethodTelephoneHearing(disposalDirectionsService.getTelephoneHearingLabel(caseData))
            .disposalHearingMethodVideoConferenceHearing(
                disposalDirectionsService.getVideoConferenceHearingLabel(caseData)
            )
            .disposalHearingBundle(caseData.getDisposalHearingBundle())
            .disposalHearingBundleTypeText(disposalDirectionsService.getBundleTypeText(caseData))
            .hasNewDirections(disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingAddNewDirections"))
            .disposalHearingAddNewDirections(caseData.getDisposalHearingAddNewDirections())
            .disposalHearingNotes(caseData.getDisposalHearingNotes())
            .disposalHearingDisclosureOfDocumentsToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingDisclosureOfDocumentsToggle")
            )
            .disposalHearingWitnessOfFactToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle")
            )
            .disposalHearingMedicalEvidenceToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingMedicalEvidenceToggle")
            )
            .disposalHearingQuestionsToExpertsToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingQuestionsToExpertsToggle")
            )
            .disposalHearingSchedulesOfLossToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingSchedulesOfLossToggle")
            )
            .disposalHearingFinalDisposalHearingToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingFinalDisposalHearingToggle")
            )
            .disposalHearingMethodToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingMethodToggle")
            )
            .disposalHearingBundleToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingBundleToggle")
            )
            .disposalHearingClaimSettlingToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingClaimSettlingToggle")
            )
            .disposalHearingCostsToggle(
                disposalDirectionsService.hasDisposalVariable(caseData, "disposalHearingCostsToggle")
            )
            .disposalOrderWithoutHearing(caseData.getDisposalOrderWithoutHearing())
            .disposalHearingTime(caseData.getDisposalHearingHearingTime())
            .hasDisposalWelshToggle(caseData.getSdoR2DisposalHearingUseOfWelshToggle() != null)
            .welshLanguageDescription(
                Optional.ofNullable(caseData.getSdoR2DisposalHearingUseOfWelshLanguage())
                    .map(value -> value.getDescription())
                    .orElse(null)
            );

        Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel)
            .ifPresent(builder::disposalHearingTimeEstimate);

        Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .filter(hearingTime -> DisposalHearingFinalDisposalHearingTimeEstimate.OTHER.equals(hearingTime.getTime()))
            .ifPresent(hearingTime -> builder.disposalHearingTimeEstimate(
                String.format("%s hours %s minutes", hearingTime.getOtherHours(), hearingTime.getOtherMinutes())
            ));

        builder.hearingLocation(
                locationHelper.getHearingLocation(
                    Optional.ofNullable(caseData.getDisposalHearingMethodInPerson())
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ))
            .caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return builder.build();
    }
}


package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DjDisposalDirectionsService {

    private final DjDeadlineService deadlineService;
    private final DjWelshLanguageService welshLanguageService;
    private final DjDisposalNarrativeService disposalNarrativeService;

    public void populateDisposalDirections(CaseData caseData, String judgeNameTitle) {
        caseData.setDisposalHearingJudgesRecitalDJ(
            disposalNarrativeService.buildJudgesRecital(judgeNameTitle));
        caseData.setDisposalHearingDisclosureOfDocumentsDJ(
            disposalNarrativeService.buildDisclosureOfDocuments());
        caseData.setDisposalHearingWitnessOfFactDJ(disposalNarrativeService.buildWitnessOfFact());
        caseData.setDisposalHearingMedicalEvidenceDJ(disposalNarrativeService.buildMedicalEvidence());
        caseData.setDisposalHearingQuestionsToExpertsDJ(disposalNarrativeService.buildQuestionsToExperts());
        caseData.setDisposalHearingSchedulesOfLossDJ(disposalNarrativeService.buildSchedulesOfLoss());
        caseData.setDisposalHearingFinalDisposalHearingDJ(disposalNarrativeService.buildFinalDisposalHearing());
        caseData.setDisposalHearingFinalDisposalHearingTimeDJ(
            disposalNarrativeService.buildFinalDisposalHearingTime());
        caseData.setDisposalHearingBundleDJ(disposalNarrativeService.buildBundle());
        caseData.setDisposalHearingNotesDJ(disposalNarrativeService.buildNotes());

        // copy of disposalHearingNotesDJ field to update order made without hearing field without breaking
        // existing cases
        LocalDate orderDeadline = deadlineService.workingDaysFromNow(5);
        caseData.setDisposalHearingOrderMadeWithoutHearingDJ(
            new DisposalHearingOrderMadeWithoutHearingDJ()
                .setInput(welshLanguageService.buildOrderMadeWithoutHearingText(orderDeadline)));

        caseData.setSdoR2DisposalHearingWelshLanguageDJ(welshLanguageService.buildWelshUsage());
    }
}

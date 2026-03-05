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

    public void populateDisposalDirections(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, String judgeNameTitle) {
        caseDataBuilder.disposalHearingJudgesRecitalDJ(
            disposalNarrativeService.buildJudgesRecital(judgeNameTitle));
        caseDataBuilder.disposalHearingDisclosureOfDocumentsDJ(
            disposalNarrativeService.buildDisclosureOfDocuments());
        caseDataBuilder.disposalHearingWitnessOfFactDJ(disposalNarrativeService.buildWitnessOfFact());
        caseDataBuilder.disposalHearingMedicalEvidenceDJ(disposalNarrativeService.buildMedicalEvidence());
        caseDataBuilder.disposalHearingQuestionsToExpertsDJ(disposalNarrativeService.buildQuestionsToExperts());
        caseDataBuilder.disposalHearingSchedulesOfLossDJ(disposalNarrativeService.buildSchedulesOfLoss());
        caseDataBuilder.disposalHearingFinalDisposalHearingDJ(disposalNarrativeService.buildFinalDisposalHearing());
        caseDataBuilder.disposalHearingFinalDisposalHearingTimeDJ(
            disposalNarrativeService.buildFinalDisposalHearingTime());
        caseDataBuilder.disposalHearingBundleDJ(disposalNarrativeService.buildBundle());
        caseDataBuilder.disposalHearingNotesDJ(disposalNarrativeService.buildNotes());

        // copy of disposalHearingNotesDJ field to update order made without hearing field without breaking
        // existing cases
        LocalDate orderDeadline = deadlineService.workingDaysFromNow(5);
        caseDataBuilder.disposalHearingOrderMadeWithoutHearingDJ(
            new DisposalHearingOrderMadeWithoutHearingDJ()
                .setInput(welshLanguageService.buildOrderMadeWithoutHearingText(orderDeadline)));

        caseDataBuilder.sdoR2DisposalHearingWelshLanguageDJ(
            welshLanguageService.buildWelshUsage());
    }
}

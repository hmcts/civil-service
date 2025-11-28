package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
@RequiredArgsConstructor
public class SdoDisposalOrderDefaultsService {

    private final SdoDisposalNarrativeService sdoDisposalNarrativeService;

    public void populateDisposalOrderDetails(CaseData caseData) {
        sdoDisposalNarrativeService.applyJudgesRecital(caseData);
        sdoDisposalNarrativeService.applyDisclosureOfDocuments(caseData);
        sdoDisposalNarrativeService.applyWitnessOfFact(caseData);
        sdoDisposalNarrativeService.applyMedicalEvidence(caseData);
        sdoDisposalNarrativeService.applyQuestionsToExperts(caseData);
        sdoDisposalNarrativeService.applySchedulesOfLoss(caseData);
        sdoDisposalNarrativeService.applyFinalDisposalHearing(caseData);
        sdoDisposalNarrativeService.applyHearingTime(caseData);
        sdoDisposalNarrativeService.applyOrderWithoutHearing(caseData);
        sdoDisposalNarrativeService.applyBundle(caseData);
        sdoDisposalNarrativeService.applyNotes(caseData);
    }
}

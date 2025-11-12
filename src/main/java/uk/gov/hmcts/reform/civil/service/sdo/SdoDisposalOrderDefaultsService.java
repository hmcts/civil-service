package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
@RequiredArgsConstructor
public class SdoDisposalOrderDefaultsService {

    private final SdoDisposalNarrativeService sdoDisposalNarrativeService;

    public void populateDisposalOrderDetails(CaseData.CaseDataBuilder<?, ?> updatedData) {
        sdoDisposalNarrativeService.applyJudgesRecital(updatedData);
        sdoDisposalNarrativeService.applyDisclosureOfDocuments(updatedData);
        sdoDisposalNarrativeService.applyWitnessOfFact(updatedData);
        sdoDisposalNarrativeService.applyMedicalEvidence(updatedData);
        sdoDisposalNarrativeService.applyQuestionsToExperts(updatedData);
        sdoDisposalNarrativeService.applySchedulesOfLoss(updatedData);
        sdoDisposalNarrativeService.applyFinalDisposalHearing(updatedData);
        sdoDisposalNarrativeService.applyHearingTime(updatedData);
        sdoDisposalNarrativeService.applyOrderWithoutHearing(updatedData);
        sdoDisposalNarrativeService.applyBundle(updatedData);
        sdoDisposalNarrativeService.applyNotes(updatedData);
    }
}

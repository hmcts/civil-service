package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;

@Service
@RequiredArgsConstructor
public class DjSpecialistDirectionsService {

    private final DjSpecialistNarrativeService narrativeService;

    public void populateSpecialistDirections(CaseData caseData) {
        caseData.setTrialBuildingDispute(narrativeService.buildTrialBuildingDispute());
        caseData.setTrialClinicalNegligence(narrativeService.buildTrialClinicalNegligence());
        caseData.setSdoDJR2TrialCreditHire(narrativeService.buildCreditHireDirections());
        caseData.setTrialPersonalInjury(narrativeService.buildTrialPersonalInjury());

        TrialRoadTrafficAccident roadTrafficAccident = narrativeService.buildTrialRoadTrafficAccident();
        caseData.setTrialRoadTrafficAccident(roadTrafficAccident);

        TrialHousingDisrepair housingDisrepair = narrativeService.buildTrialHousingDisrepair();
        caseData.setTrialHousingDisrepair(housingDisrepair);
    }
}

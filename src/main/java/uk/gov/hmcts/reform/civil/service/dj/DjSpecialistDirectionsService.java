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

    public void populateSpecialistDirections(CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        caseDataBuilder.trialBuildingDispute(narrativeService.buildTrialBuildingDispute());
        caseDataBuilder.trialClinicalNegligence(narrativeService.buildTrialClinicalNegligence());
        caseDataBuilder.sdoDJR2TrialCreditHire(narrativeService.buildCreditHireDirections());
        caseDataBuilder.trialPersonalInjury(narrativeService.buildTrialPersonalInjury());

        TrialRoadTrafficAccident roadTrafficAccident = narrativeService.buildTrialRoadTrafficAccident();
        caseDataBuilder.trialRoadTrafficAccident(roadTrafficAccident);

        TrialHousingDisrepair housingDisrepair = narrativeService.buildTrialHousingDisrepair();
        caseDataBuilder.trialHousingDisrepair(housingDisrepair);
    }
}

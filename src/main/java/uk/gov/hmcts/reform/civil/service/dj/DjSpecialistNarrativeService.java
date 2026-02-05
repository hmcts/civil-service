package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;

@Service
@RequiredArgsConstructor
public class DjSpecialistNarrativeService {

    private final DjBuildingDisputeDirectionsService buildingDisputeDirectionsService;
    private final DjClinicalDirectionsService clinicalDirectionsService;
    private final DjRoadTrafficAccidentDirectionsService roadTrafficAccidentDirectionsService;
    private final DjCreditHireDirectionsService creditHireDirectionsService;

    public TrialBuildingDispute buildTrialBuildingDispute() {
        return buildingDisputeDirectionsService.buildTrialBuildingDispute();
    }

    public TrialClinicalNegligence buildTrialClinicalNegligence() {
        return clinicalDirectionsService.buildTrialClinicalNegligence();
    }

    public SdoDJR2TrialCreditHire buildCreditHireDirections() {
        return creditHireDirectionsService.buildCreditHireDirections();
    }

    public TrialPersonalInjury buildTrialPersonalInjury() {
        return clinicalDirectionsService.buildTrialPersonalInjury();
    }

    public TrialRoadTrafficAccident buildTrialRoadTrafficAccident() {
        return roadTrafficAccidentDirectionsService.buildTrialRoadTrafficAccident();
    }

    public TrialHousingDisrepair buildTrialHousingDisrepair() {
        return buildingDisputeDirectionsService.buildTrialHousingDisrepair();
    }
}

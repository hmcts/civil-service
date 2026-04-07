package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DjDirectionsToggleService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DjSpecialistDirectionsService {

    private final DjSpecialistNarrativeService narrativeService;
    private final FeatureToggleService featureToggleService;
    private final DjDirectionsToggleService directionsToggleService;

    public void populateSpecialistDirections(
        CaseData caseData,
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder
    ) {
        caseDataBuilder.trialBuildingDispute(narrativeService.buildTrialBuildingDispute());
        caseDataBuilder.trialClinicalNegligence(narrativeService.buildTrialClinicalNegligence());
        caseDataBuilder.sdoDJR2TrialCreditHire(narrativeService.buildCreditHireDirections());
        caseDataBuilder.trialPersonalInjury(narrativeService.buildTrialPersonalInjury());

        TrialRoadTrafficAccident roadTrafficAccident = narrativeService.buildTrialRoadTrafficAccident();
        caseDataBuilder.trialRoadTrafficAccident(roadTrafficAccident);

        if (featureToggleService.isOtherRemedyEnabled()) {
            TrialHousingDisrepair housingDisrepairOtherRemedy =
                narrativeService.buildTrialHousingDisrepairOtherRemedy();
            caseDataBuilder.trialHousingDisrepair(housingDisrepairOtherRemedy);
            caseDataBuilder.trialPPI(narrativeService.buildTrialPPI());
        } else {
            caseDataBuilder.trialHousingDisrepair(narrativeService.buildTrialHousingDisrepair());
            caseDataBuilder.trialPPI(null);
        }

        resetPpiAndHousingIfNotSelected(caseData.getCaseManagementOrderAdditional(), caseDataBuilder);
    }

    private void resetPpiAndHousingIfNotSelected(
        List<CaseManagementOrderAdditional> additionalDirections,
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder
    ) {
        if (!directionsToggleService.hasPpi(additionalDirections)) {
            caseDataBuilder.trialPPI(null);
        }
        if (!directionsToggleService.hasHousingDisrepair(additionalDirections)) {
            caseDataBuilder.trialHousingDisrepair(null);
        }
    }
}

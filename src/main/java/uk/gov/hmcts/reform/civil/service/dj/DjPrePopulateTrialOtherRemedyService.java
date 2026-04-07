package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DjDirectionsToggleService;

@Service
@RequiredArgsConstructor
public class DjPrePopulateTrialOtherRemedyService {

    private final DjSpecialistNarrativeService narrativeService;
    private final FeatureToggleService featureToggleService;
    private final DjDirectionsToggleService directionsToggleService;

    public void applyOtherRemedyTrialDefaults(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        if (!featureToggleService.isOtherRemedyEnabled()) {
            builder.trialPPI(null);
            builder.trialHousingDisrepair(null);
            return;
        }
        if (directionsToggleService.hasPpi(caseData.getCaseManagementOrderAdditional())) {
            builder.trialPPI(narrativeService.buildTrialPPI());
        } else {
            builder.trialPPI(null);
        }
        if (directionsToggleService.hasHousingDisrepair(caseData.getCaseManagementOrderAdditional())) {
            builder.trialHousingDisrepair(narrativeService.buildTrialHousingDisrepairOtherRemedy());
        } else {
            builder.trialHousingDisrepair(null);
        }
    }
}

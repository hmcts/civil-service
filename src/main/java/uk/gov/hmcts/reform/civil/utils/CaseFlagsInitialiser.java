package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addApplicantExpertAndWitnessFlagsStructure;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addRespondentDQPartiesFlagStructure;

@Component
@AllArgsConstructor
public class CaseFlagsInitialiser {

    private final FeatureToggleService featureToggleService;

    public void initialiseCaseFlags(CaseEvent caseEvent, CaseData.CaseDataBuilder dataBuilder) {
        if (!featureToggleService.isCaseFlagsEnabled()) {
            return;
        }
        CaseData caseData = dataBuilder.build();
        switch (caseEvent) {
            case DEFENDANT_RESPONSE_SPEC:
            case DEFENDANT_RESPONSE: {
                addRespondentDQPartiesFlagStructure(dataBuilder, caseData);
            }
                break;
            case CLAIMANT_RESPONSE:
            case CLAIMANT_RESPONSE_SPEC: {
                addApplicantExpertAndWitnessFlagsStructure(dataBuilder, caseData);
            }
                break;
            default:
        }
    }
}

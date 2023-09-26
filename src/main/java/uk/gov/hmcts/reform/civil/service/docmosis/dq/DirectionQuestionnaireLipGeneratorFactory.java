package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Component
@RequiredArgsConstructor
public class DirectionQuestionnaireLipGeneratorFactory {

    private final FeatureToggleService featureToggleService;
    private final DirectionsQuestionnaireLipGenerator directionsQuestionnaireLipGenerator;
    private final DirectionQuestionnaireLipResponseGenerator directionQuestionnaireLipResponseGenerator;

    public DirectionsQuestionnaireGenerator getDirectionQuestionnaire() {
        if (featureToggleService.isLipVLipEnabled()) {
            return directionQuestionnaireLipResponseGenerator;
        }
        return directionsQuestionnaireLipGenerator;
    }
}

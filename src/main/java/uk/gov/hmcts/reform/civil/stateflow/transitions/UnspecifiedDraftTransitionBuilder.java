package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UnspecifiedDraftTransitionBuilder extends DraftTransitionBuilder {

    public UnspecifiedDraftTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.DRAFT, featureToggleService);
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;

@Component
public class ClaimDismissedAllLegalRepsEmailGenerator extends AllLegalRepsEmailGenerator {

    private final SimpleStateFlowEngine stateFlowEngine;

    public ClaimDismissedAllLegalRepsEmailGenerator(
        ClaimDismissedAppSolOneEmailDTOGenerator claimDismissedAppSolOneEmailGenerator,
        ClaimDismissedRespSolOneEmailDTOGenerator claimDismissedRespSolOneEmailGenerator,
        ClaimDismissedRespSolTwoEmailDTOGenerator claimDismissedRespSolTwoEmailGenerator,
        SimpleStateFlowEngine stateFlowEngine
    ) {
        super(claimDismissedAppSolOneEmailGenerator,
            claimDismissedRespSolOneEmailGenerator,
            claimDismissedRespSolTwoEmailGenerator,
            stateFlowEngine);
        this.stateFlowEngine = stateFlowEngine;
    }

    protected boolean shouldNotifyRespondents(CaseData caseData) {
        String stateName = stateFlowEngine.evaluate(caseData).getState().getName();
        return CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName().equals(stateName);
    }
}

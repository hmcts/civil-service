package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;

@Component
@Slf4j
public class ClaimDismissedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    private final SimpleStateFlowEngine stateFlowEngine;

    public ClaimDismissedAllPartiesEmailGenerator(
        ClaimDismissedAppSolOneEmailDTOGenerator claimDismissedAppSolOneEmailGenerator,
        ClaimDismissedRespSolOneEmailDTOGenerator claimDismissedRespSolOneEmailGenerator,
        ClaimDismissedRespSolTwoEmailDTOGenerator claimDismissedRespSolTwoEmailGenerator,
        SimpleStateFlowEngine stateFlowEngine
    ) {
        super(claimDismissedAppSolOneEmailGenerator,
              claimDismissedRespSolOneEmailGenerator,
              claimDismissedRespSolTwoEmailGenerator,
              null,
              null,
              null,
              stateFlowEngine);
        this.stateFlowEngine = stateFlowEngine;
    }

    @Override
    protected boolean shouldNotifyRespondents(CaseData caseData) {
        String stateName = stateFlowEngine.evaluate(caseData).getState().getName();
        log.info("State name: {} for caseId: {}", stateName, caseData.getCcdCaseReference());
        return CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName().equals(stateName);
    }
}

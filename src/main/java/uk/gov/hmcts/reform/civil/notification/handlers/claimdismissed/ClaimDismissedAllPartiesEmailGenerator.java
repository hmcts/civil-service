package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

@Component
@Slf4j
public class ClaimDismissedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

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
              null);
    }

    @Override
    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return caseData.getClaimDismissedDate() != null;
    }
}

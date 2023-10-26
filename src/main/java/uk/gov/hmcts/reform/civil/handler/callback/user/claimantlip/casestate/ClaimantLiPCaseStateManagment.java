package uk.gov.hmcts.reform.civil.handler.callback.user.claimantlip.casestate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Component
@AllArgsConstructor
public class ClaimantLiPCaseStateManagment {

    public AboutToStartOrSubmitCallbackResponse caseStateHandlerForClaimantResponse(
        AboutToStartOrSubmitCallbackResponse response, CaseData caseData) {
        if ((caseData.isClaimantNotSettlePartAdmitClaim() || caseData.isFullDefence())
            && caseData.getCaseDataLiP().hasClaimantNotAgreedToFreeMediation()) {
            response.setState(CaseState.JUDICIAL_REFERRAL.name());
        }
        return response;
    }
}

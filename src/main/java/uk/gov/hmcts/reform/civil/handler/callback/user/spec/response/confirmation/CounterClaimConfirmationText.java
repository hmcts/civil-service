package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

@Component
public class CounterClaimConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!isCounterClaim(caseData)) {
            return Optional.empty();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<br>You've chosen to counterclaim - this means your defence cannot continue online.");
        sb.append(" Use form N9B to counterclaim, do not create a new claim.");
        sb.append("<br><br><a href=\"https://www.gov.uk/respond-money-claim\" target=\"_blank\">Download form N9B (opens in a new tab)</a>");
        return Optional.of(sb.toString());
    }

    private boolean isCounterClaim(CaseData caseData) {
        if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            if (caseData.getRespondent2() == null) {
                return true;
            } else {
                if (YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())) {
                    return true;
                } else {
                    return RespondentResponseTypeSpec.COUNTER_CLAIM.equals(
                        caseData.getRespondent2ClaimResponseTypeForSpec());
                }
            }
        }
        return false;
    }
}

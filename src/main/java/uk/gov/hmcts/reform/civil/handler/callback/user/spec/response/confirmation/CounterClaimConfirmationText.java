package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

@Component
public class CounterClaimConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    private static final String TEXT =
        "<br>You've chosen to counterclaim - this means your defence cannot continue online."
        + " Use form N9B to counterclaim, do not create a new claim."
        + "<br><br><a href=\"https://www.gov.uk/respond-money-claim\" target=\"_blank\">Download form N9B (opens in a new tab)</a>";

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!isCounterClaim(caseData)) {
            return Optional.empty();
        }
        return Optional.of(TEXT);
    }

    private boolean isCounterClaim(CaseData caseData) {
        return RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            && (caseData.getRespondent2() == null
            || YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            );
    }
}

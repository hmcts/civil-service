package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

@Component
public class CounterClaimConfirmationHeaderText implements RespondToClaimConfirmationHeaderSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!isCounterClaim(caseData)) {
            return Optional.empty();
        }

        String header = String.format(
            "# You have chosen to counterclaim%n## Claim number: %s",
            caseData.getLegacyCaseReference()
        );
        return Optional.of(header);
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

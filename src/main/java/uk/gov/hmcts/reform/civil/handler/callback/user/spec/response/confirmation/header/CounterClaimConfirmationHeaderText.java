package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

@Component
public class CounterClaimConfirmationHeaderText implements RespondToClaimConfirmationHeaderSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            return Optional.empty();
        }
        String header = String.format(
            "# You have chosen to counterclaim%n## Claim number: %s",
            caseData.getLegacyCaseReference()
        );
        return Optional.of(header);
    }
}

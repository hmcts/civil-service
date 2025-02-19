package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

/**
 * generator for the header text of response to specified claim, 2v1, with different responses for each
 * claimant.
 */
@Component
public class SpecResponse1v2DivergentHeaderText implements RespondToClaimConfirmationHeaderSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
            return Optional.empty();
        }

        String claimNumber = caseData.getLegacyCaseReference();
        return Optional.of(format(
            "# The defendants have chosen their responses%n## Claim number <br>%s",
            claimNumber
        ));
    }
}

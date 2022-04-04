package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;
import java.util.Optional;

/**
 * This generator prepares the text to answer a 2v1 claim in which the defendant has different responses for each
 * claimant.
 */
@Component
public class SpecResponse2v1DifferentText implements RespondToClaimConfirmationTextSpecGenerator {

    /**
     * in this case text is fixed, the method does only need to care about the conditions to apply.
     */
    private static final String TEXT =
        "<br>The defendant has chosen different responses for each claimant and the claim cannot continue online."
            + "Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to counterclaim.<br><br>"
            + "<a href=\"http://www.gov.uk/respond-money-claim\" target=\"_blank\">"
            + "Download form N9A (opens in a new tab)</a><br>"
            + "<a href=\"http://www.gov.uk/respond-money-claim\" target=\"_blank\">"
            + "Download form N9B (opens in a new tab)</a><br><br>"
            + "Post the completed form to:<br><br>"
            + "County Court Business Centre<br>"
            + "St. Katherine's House<br>"
            + "21-27 St. Katherine Street<br>"
            + "Northampton<br>"
            + "NN1 2LH<br>";

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (caseData.getApplicant2() == null
            || YesOrNo.YES.equals(caseData.getDefendantSingleResponseToBothClaimants())
            || Objects.equals(
            caseData.getClaimant1ClaimResponseTypeForSpec(),
            caseData.getClaimant2ClaimResponseTypeForSpec()
        )) {
            return Optional.empty();
        }

        return Optional.of(TEXT);
    }
}

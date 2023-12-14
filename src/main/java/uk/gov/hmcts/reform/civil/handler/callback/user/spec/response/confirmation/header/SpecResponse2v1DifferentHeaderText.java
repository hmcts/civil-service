package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;
import java.util.Optional;

/**
 * generator for the header text of response to specified claim, 2v1, with different responses for each
 * claimant.
 */
@Component
public class SpecResponse2v1DifferentHeaderText implements RespondToClaimConfirmationHeaderSpecGenerator {

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

        return Optional.of("The defendant has chosen different responses for each claimant");
    }
}

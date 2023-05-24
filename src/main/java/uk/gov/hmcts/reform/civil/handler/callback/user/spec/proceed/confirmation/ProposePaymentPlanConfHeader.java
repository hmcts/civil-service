package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class ProposePaymentPlanConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (caseData.isDefendantPaymentPlanYes()
            || caseData.hasClaimantAgreedToFreeMediation()) {
            return Optional.empty();
        }

        String claimNumber = caseData.getLegacyCaseReference();
        return Optional.of(format(
            "# Payment plan rejected %n## The proposed payment plan for case %s has been rejected by the claimant."
                + "%n## A new payment plan has been submitted and will be determined manually by the Court.",
            claimNumber
        ));
    }
}

package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class JudgmentSubmittedConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (caseData.isDefendantPaymentPlanNo()
            || caseData.hasClaimantAgreedToFreeMediation()) {
            return Optional.empty();
        }

        String claimNumber = caseData.getLegacyCaseReference();
        return Optional.of(format(
            "# Judgment Submitted %n## A county court judgment(ccj) has been submitted for case %s",
            claimNumber
        ));
    }
}


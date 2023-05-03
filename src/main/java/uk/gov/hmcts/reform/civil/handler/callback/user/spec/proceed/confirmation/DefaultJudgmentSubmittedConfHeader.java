package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

public class DefaultJudgmentSubmittedConfHeader implements RespondToResponseConfirmationHeaderGenerator {
    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if(!caseData.hasClaimantAgreedToFreeMediation() && caseData.hasApplicantRejectedRepaymentPlan()) {
            return Optional.of(format(
                "# Default judgement requested %n# %s",
                caseData.getLegacyCaseReference()
            ));
        }
        return Optional.empty();
    }
}

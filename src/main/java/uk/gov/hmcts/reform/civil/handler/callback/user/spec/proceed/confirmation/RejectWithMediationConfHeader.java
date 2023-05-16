package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class RejectWithMediationConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (caseData.hasClaimantAgreedToFreeMediation()) {
            return Optional.of(format(
                "# You have rejected their response %n## Your Claim Number : %s",
                caseData.getLegacyCaseReference()
            ));
        }
        return Optional.empty();
    }
}

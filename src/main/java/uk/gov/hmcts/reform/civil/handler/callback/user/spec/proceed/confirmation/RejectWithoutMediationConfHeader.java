package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class RejectWithoutMediationConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!caseData.isRejectWithNoMediation()) {
            return Optional.empty();
        }

        String claimNumber = caseData.getLegacyCaseReference();
        return Optional.of(format(
            "# You have decided to proceed with the claim%n## Claim number: %s",
            claimNumber
        ));
    }
}

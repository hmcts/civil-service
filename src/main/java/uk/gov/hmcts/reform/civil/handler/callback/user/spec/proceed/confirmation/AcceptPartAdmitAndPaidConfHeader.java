package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class AcceptPartAdmitAndPaidConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!caseData.isPartAdmitClaimSpec() || caseData.isPartAdmitClaimNotSettled()) {
            return Optional.empty();
        }
        String claimNumber = caseData.getLegacyCaseReference();
        return Optional.of(format(
            "# You have accepted the defendant's response%n## Claim number: %s",
            claimNumber
        ));
    }
}

package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class RejectWithMediationConfText implements RespondToResponseConfirmationTextGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (caseData.hasClaimantAgreedToFreeMediation()) {
            return Optional.of(format(
                "<br />You have agreed to try free mediation.<br>" +
                    "<br>Your mediation appointment will be arranged within 28 days.<br>"
            ));
        }
        return Optional.empty();
    }
}

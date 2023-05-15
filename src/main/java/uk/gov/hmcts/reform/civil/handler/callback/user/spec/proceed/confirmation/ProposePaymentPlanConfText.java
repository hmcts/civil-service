package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class ProposePaymentPlanConfText implements RespondToResponseConfirmationTextGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (caseData.isDefendantPaymentPlanYes()
            || caseData.hasClaimantAgreedToFreeMediation()) {
            return Optional.empty();
        }

        return Optional.of(format(
            "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "This case will now proceed offline. Any updates will be sent by post.<br><br>"
        ));
    }
}

package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class RejectWithoutMediationConfText implements RespondToResponseConfirmationTextGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!caseData.isRejectWithNoMediation()) {
            return Optional.empty();
        }

        return Optional.of("<h2 class=\"govuk-heading-m\">What happens next</h2>"
                               + "We'll review the case and contact you about what to do next.<br>"
                               + format(
            "%n%n<a href=\"%s\" target=\"_blank\">View Directions questionnaire</a>",
            format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
        ));
    }
}

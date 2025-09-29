package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class JudgmentSubmittedConfText implements RespondToResponseConfirmationTextGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData, FeatureToggleService featureToggleService) {
        if (caseData.isDefendantPaymentPlanNo()
            || caseData.hasClaimantAgreedToFreeMediation()
            || caseData.isCcjRequestJudgmentByAdmissionDefendantNotPaid()) {
            return Optional.empty();
        }
        return Optional.of(format(
            "<br /><h2 class=\"govuk-heading-m\"><u>What happens next</u></h2>"
                + "<br>This case will now proceed offline. Any updates will be sent by post.<br><br>"
        ));
    }
}


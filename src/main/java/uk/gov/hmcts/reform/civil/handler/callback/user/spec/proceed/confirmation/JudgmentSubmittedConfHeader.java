package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class JudgmentSubmittedConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData, FeatureToggleService featureToggleService) {
        if (caseData.isDefendantPaymentPlanNo()
            || caseData.hasClaimantAgreedToFreeMediation()
            || caseData.isCcjRequestJudgmentByAdmissionDefendantNotPaid()) {
            return Optional.empty();
        }

        String claimNumber = caseData.getLegacyCaseReference();
        return Optional.of(format(
            "# Judgment Submitted %n## A county court judgment(CCJ) has been submitted for case %s",
            claimNumber
        ));
    }
}


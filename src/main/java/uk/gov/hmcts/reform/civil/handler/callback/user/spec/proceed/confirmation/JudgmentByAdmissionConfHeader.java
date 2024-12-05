package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class JudgmentByAdmissionConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (CaseState.All_FINAL_ORDERS_ISSUED == caseData.getCcdState()
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment())) {
            String claimNumber = caseData.getLegacyCaseReference();
            return Optional.of(format(
                "# Judgment Submitted %n## A county court judgment(CCJ) has been submitted for case %s",
                claimNumber
            ));
        }
        return Optional.empty();
    }
}


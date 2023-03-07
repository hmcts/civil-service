package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class RejectWithMediationConfText implements RespondToResponseConfirmationTextGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (caseData.getApplicant1ClaimMediationSpecRequired() == null
            || !YesOrNo.YES.equals(caseData.getApplicant1ClaimMediationSpecRequired().getHasAgreedFreeMediation())) {
            return Optional.empty();
        }
        return Optional.of(format(
            "<br />You have agreed to try free mediation.<br>" +
                "<br>Your mediation appointment will be arranged within 28 days.<br>"
        ));
    }
}

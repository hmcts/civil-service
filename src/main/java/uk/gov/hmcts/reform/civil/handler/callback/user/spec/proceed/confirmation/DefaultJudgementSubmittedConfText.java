package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class DefaultJudgementSubmittedConfText implements RespondToResponseConfirmationTextGenerator {
    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if(!caseData.hasClaimantAgreedToFreeMediation() && caseData.hasApplicantRejectedRepaymentPlan()) {
            return Optional.of(format(
                "<br />A request for default judgement has been sent to the court for review.<br>" +
                    "<br>The claim will now progress offline<br>"
            ));
        }
        return Optional.empty();
    }
}

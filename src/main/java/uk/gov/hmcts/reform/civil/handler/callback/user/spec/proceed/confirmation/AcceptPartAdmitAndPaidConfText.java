package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static java.lang.String.format;

@Component
public class AcceptPartAdmitAndPaidConfText implements RespondToResponseConfirmationTextGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!caseData.isPartAdmitClaimSpec() || caseData.isPartAdmitClaimNotSettled()) {
            return Optional.empty();
        }
        String respondentName = caseData.getRespondent1().getPartyName();
        return Optional.of(format(
            "<br>The claim is now settled. We have emailed %s to tell them.",
            respondentName
        ));
    }
}

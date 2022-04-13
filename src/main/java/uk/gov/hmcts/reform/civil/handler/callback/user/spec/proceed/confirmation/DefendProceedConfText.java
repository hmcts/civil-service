package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

@Component
public class DefendProceedConfText implements RespondToResponseConfirmationTextGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        return Optional.empty();
    }
}

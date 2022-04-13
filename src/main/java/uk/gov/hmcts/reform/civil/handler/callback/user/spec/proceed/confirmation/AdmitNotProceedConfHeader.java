package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

@Component
public class AdmitNotProceedConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        return Optional.empty();
    }
}

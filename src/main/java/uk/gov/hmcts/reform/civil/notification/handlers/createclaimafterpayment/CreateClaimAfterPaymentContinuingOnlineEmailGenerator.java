package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CreateClaimAfterPaymentContinuingOnlineEmailGenerator implements PartiesEmailGenerator {

    private final CreateClaimAfterPaymentContinuingOnlineAppSolOneEmailDTOGenerator appSolOneGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        return Set.of(appSolOneGenerator.buildEmailDTO(caseData));
    }
}

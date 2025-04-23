package uk.gov.hmcts.reform.civil.notification.handlers.claimantLipHelpWithFees;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.Set;

@Component
public class NotifyClaimantLipHelpWithFeesPartiesEmailGenerator
        implements PartiesEmailGenerator {

    private final NotifyClaimantLipHelpWithFeesEmailDTOGenerator dtoGenerator;

    public NotifyClaimantLipHelpWithFeesPartiesEmailGenerator(
            NotifyClaimantLipHelpWithFeesEmailDTOGenerator dtoGenerator
    ) {
        this.dtoGenerator = dtoGenerator;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        return Set.of(dtoGenerator.buildEmailDTO(caseData));
    }
}

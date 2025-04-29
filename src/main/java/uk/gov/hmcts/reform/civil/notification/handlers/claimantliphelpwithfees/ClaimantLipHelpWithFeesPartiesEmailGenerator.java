package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.Set;

@Component
public class ClaimantLipHelpWithFeesPartiesEmailGenerator implements PartiesEmailGenerator {

    private final ClaimantLipHelpWithFeesEmailDTOGenerator dtoGenerator;

    public ClaimantLipHelpWithFeesPartiesEmailGenerator(
            ClaimantLipHelpWithFeesEmailDTOGenerator dtoGenerator
    ) {
        this.dtoGenerator = dtoGenerator;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        return Set.of(dtoGenerator.buildEmailDTO(caseData));
    }
}

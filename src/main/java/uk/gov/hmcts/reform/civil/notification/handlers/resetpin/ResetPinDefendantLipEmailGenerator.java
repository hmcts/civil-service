package uk.gov.hmcts.reform.civil.notification.handlers.resetpin;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.HashSet;
import java.util.Set;

@Component
@AllArgsConstructor
public class ResetPinDefendantLipEmailGenerator implements PartiesEmailGenerator {

    private final ResetPinDefendantLipEmailDTOGenerator resetPinDefendantLipEmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(resetPinDefendantLipEmailDTOGenerator.buildEmailDTO(caseData));
        return partiesToEmail;
    }
}

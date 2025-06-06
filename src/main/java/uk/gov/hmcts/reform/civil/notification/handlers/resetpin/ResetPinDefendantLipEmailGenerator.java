package uk.gov.hmcts.reform.civil.notification.handlers.resetpin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.HashSet;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class ResetPinDefendantLipEmailGenerator implements PartiesEmailGenerator {

    private final ResetPinDefendantLipEmailDTOGenerator resetPinDefendantLipEmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating reset pin email for Defendant LiP for case id: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(resetPinDefendantLipEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        return partiesToEmail;
    }
}

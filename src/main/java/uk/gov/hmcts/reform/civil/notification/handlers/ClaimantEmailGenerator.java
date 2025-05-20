package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class ClaimantEmailGenerator implements PartiesEmailGenerator {

    private final ClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(claimantEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        return partiesToEmail;
    }
}

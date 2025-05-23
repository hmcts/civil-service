package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Slf4j
public class LRvLipEmailGenerator implements PartiesEmailGenerator {

    private final AppSolOneEmailDTOGenerator appSolOneEmailGenerator;
    private final DefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(appSolOneEmailGenerator.buildEmailDTO(caseData, taskId));
        partiesToEmail.add(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        return partiesToEmail;
    }
}

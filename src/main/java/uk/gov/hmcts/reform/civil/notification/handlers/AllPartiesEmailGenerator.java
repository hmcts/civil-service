package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class AllPartiesEmailGenerator implements PartiesEmailGenerator {

    private final List<EmailDTOGenerator> generators;

    public AllPartiesEmailGenerator(List<EmailDTOGenerator> generators) {
        this.generators = generators;
    }

    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        for (EmailDTOGenerator generator : generators) {
            if ((generator != null) && generator.getShouldNotify(caseData)) {
                log.info("Generating email for party [{}] for case ID: {}",
                    generator.getClass().getSimpleName(), caseData.getCcdCaseReference());
                partiesToEmail.add(generator.buildEmailDTO(caseData, taskId));
            }
        }
        return partiesToEmail;
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class StandardDirectionOrderDJAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    private final StandardDirectionOrderDJAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;
    private final StandardDirectionOrderDJRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;
    private final StandardDirectionOrderDJRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    public StandardDirectionOrderDJAllPartiesEmailGenerator(
        StandardDirectionOrderDJAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        StandardDirectionOrderDJRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        StandardDirectionOrderDJRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(List.of(
            appSolOneEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator
        ));
        this.appSolOneEmailDTOGenerator = appSolOneEmailDTOGenerator;
        this.respSolOneEmailDTOGenerator = respSolOneEmailDTOGenerator;
        this.respSolTwoEmailDTOGenerator = respSolTwoEmailDTOGenerator;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating standard direction order DJ emails for case ID: {}", caseData.getCcdCaseReference());

        // Notify claimant
        if (appSolOneEmailDTOGenerator.getShouldNotify(caseData)) {
            partiesToEmail.add(appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId));
            log.info("Added claimant notification for case ID: {}", caseData.getCcdCaseReference());
        }

        // Notify defendant 1
        if (respSolOneEmailDTOGenerator.getShouldNotify(caseData)) {
            partiesToEmail.add(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId));
            log.info("Added respondent 1 notification for case ID: {}", caseData.getCcdCaseReference());
        }

        // Notify defendant 2 if applicable
        if (respSolTwoEmailDTOGenerator.getShouldNotify(caseData)) {
            partiesToEmail.add(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId));
            log.info("Added respondent 2 notification for case ID: {}", caseData.getCcdCaseReference());
        }

        return partiesToEmail;
    }
}

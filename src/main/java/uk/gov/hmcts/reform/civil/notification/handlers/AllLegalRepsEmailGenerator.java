package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@AllArgsConstructor
@Slf4j
public class AllLegalRepsEmailGenerator implements PartiesEmailGenerator {

    private final AppSolOneEmailDTOGenerator appSolOneEmailGenerator;
    private final RespSolOneEmailDTOGenerator respSolOneEmailGenerator;
    private final RespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(appSolOneEmailGenerator.buildEmailDTO(caseData, taskId));
        if (shouldNotifyRespondents(caseData)) {
            log.info("Generating email for respondents for case ID: {}", caseData.getCcdCaseReference());
            partiesToEmail.addAll(getRespondents(caseData, taskId));
        }
        return partiesToEmail;
    }

    private Set<EmailDTO> getRespondents(CaseData caseData, String taskId) {
        Set<EmailDTO> recipients = new HashSet<>();
        recipients.add(respSolOneEmailGenerator.buildEmailDTO(caseData, taskId));
        if (isOneVTwoTwoLegalRep(caseData)) {
            recipients.add(respSolTwoEmailGenerator.buildEmailDTO(caseData, taskId));
        }
        return recipients;
    }

    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return Boolean.TRUE;
    }
}

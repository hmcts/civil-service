package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@AllArgsConstructor
public class AllLegalRepsEmailGenerator implements PartiesEmailGenerator {

    private final AppSolOneEmailDTOGenerator appSolOneEmailGenerator;
    private final RespSolOneEmailDTOGenerator respSolOneEmailGenerator;
    private final RespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;
    protected final SimpleStateFlowEngine stateFlowEngine;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(appSolOneEmailGenerator.buildEmailDTO(caseData));
        if (shouldNotifyRespondents(caseData)) {
            partiesToEmail.addAll(getRespondents(caseData));
        }
        return partiesToEmail;
    }

    private Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        recipients.add(respSolOneEmailGenerator.buildEmailDTO(caseData));
        if (stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)) {
            recipients.add(respSolTwoEmailGenerator.buildEmailDTO(caseData));
        }
        return recipients;
    }

    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return Boolean.TRUE;
    }
}

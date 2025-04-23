package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@AllArgsConstructor
@Slf4j
public class AllPartiesEmailGenerator implements PartiesEmailGenerator {

    private final AppSolOneEmailDTOGenerator appSolOneEmailGenerator;
    private final RespSolOneEmailDTOGenerator respSolOneEmailGenerator;
    private final RespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;

    private final ClaimantEmailDTOGenerator claimantEmailDTOGenerator;
    private final DefendantEmailDTOGenerator defendantEmailDTOGenerator;

    private final Set<EmailDTOGenerator> otherEmailDTOGenerators;

    protected final SimpleStateFlowEngine stateFlowEngine;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        addIfPartyNeedsNotification(caseData, appSolOneEmailGenerator, partiesToEmail);
        if (shouldNotifyRespondents(caseData)) {
            log.info("Generating email for respondent LR for case ID: {}", caseData.getCcdCaseReference());
            partiesToEmail.addAll(getRespondents(caseData));
        }
        addIfPartyNeedsNotification(caseData, claimantEmailDTOGenerator, partiesToEmail);
        addIfPartyNeedsNotification(caseData, defendantEmailDTOGenerator, partiesToEmail);
        for (EmailDTOGenerator otherGenerator : otherEmailDTOGenerators) {
            addIfPartyNeedsNotification(caseData, otherGenerator, partiesToEmail);
        }
        return partiesToEmail;
    }

    private Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        addIfPartyNeedsNotification(caseData, respSolOneEmailGenerator, recipients);
        if (stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)) {
            addIfPartyNeedsNotification(caseData, respSolTwoEmailGenerator, recipients);
        }
        return recipients;
    }

    private void addIfPartyNeedsNotification(CaseData caseData,
                                             EmailDTOGenerator generator,
                                             Set<EmailDTO> partiesToEmail) {
        if ((generator != null) && generator.getShouldNotify()) {
            log.info("Generating email for party [{}] for case ID: {}", generator.getClass().getSimpleName(), caseData.getCcdCaseReference());
            partiesToEmail.add(generator.buildEmailDTO(caseData));
        }
    }

    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return Boolean.TRUE;
    }
}

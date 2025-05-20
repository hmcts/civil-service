package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Slf4j
public class AllPartiesEmailGenerator implements PartiesEmailGenerator {

    private final AppSolOneEmailDTOGenerator appSolOneEmailGenerator;
    private final RespSolOneEmailDTOGenerator respSolOneEmailGenerator;
    private final RespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;

    private final ClaimantEmailDTOGenerator claimantEmailDTOGenerator;
    private final DefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        addIfPartyNeedsNotification(caseData, appSolOneEmailGenerator, partiesToEmail, taskId);
        addIfPartyNeedsNotification(caseData, claimantEmailDTOGenerator, partiesToEmail, taskId);
        if (shouldNotifyRespondents(caseData)) {
            log.info("Generating email for respondents for case ID: {}", caseData.getCcdCaseReference());
            partiesToEmail.addAll(getRespondents(caseData, taskId));
        }
        return partiesToEmail;
    }

    private Set<EmailDTO> getRespondents(CaseData caseData, String taskId) {
        Set<EmailDTO> recipients = new HashSet<>();
        addIfPartyNeedsNotification(caseData, respSolOneEmailGenerator, recipients, taskId);
        addIfPartyNeedsNotification(caseData, respSolTwoEmailGenerator, recipients, taskId);
        addIfPartyNeedsNotification(caseData, defendantEmailDTOGenerator, recipients, taskId);
        return recipients;
    }

    private void addIfPartyNeedsNotification(CaseData caseData,
                                             EmailDTOGenerator generator,
                                             Set<EmailDTO> partiesToEmail,
                                             String taskId) {
        if ((generator != null) && generator.getShouldNotify(caseData)) {
            log.info("Generating email for party [{}] for case ID: {}",
                     generator.getClass().getSimpleName(), caseData.getCcdCaseReference());
            partiesToEmail.add(generator.buildEmailDTO(caseData, taskId));
        }
    }

    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return Boolean.TRUE;
    }
}

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
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        addIfPartyNeedsNotification(caseData, appSolOneEmailGenerator, partiesToEmail);
        addIfPartyNeedsNotification(caseData, claimantEmailDTOGenerator, partiesToEmail);
        if (shouldNotifyRespondents(caseData)) {
            log.info("Generating email for respondents for case ID: {}", caseData.getCcdCaseReference());
            partiesToEmail.addAll(getRespondents(caseData));
        }
        return partiesToEmail;
    }

    private Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        addIfPartyNeedsNotification(caseData, respSolOneEmailGenerator, recipients);
        addIfPartyNeedsNotification(caseData, respSolTwoEmailGenerator, recipients);
        addIfPartyNeedsNotification(caseData, defendantEmailDTOGenerator, recipients);
        return recipients;
    }

    private void addIfPartyNeedsNotification(CaseData caseData,
                                             EmailDTOGenerator generator,
                                             Set<EmailDTO> partiesToEmail) {
        if ((generator != null) && generator.getShouldNotify(caseData)) {
            log.info("Generating email for party [{}] for case ID: {}", generator.getClass().getSimpleName(), caseData.getCcdCaseReference());
            partiesToEmail.add(generator.buildEmailDTO(caseData));
        }
    }

    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return Boolean.TRUE;
    }
}

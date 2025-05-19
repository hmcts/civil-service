package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Slf4j
@Component
public class CreateSDOPartiesEmailGenerator implements PartiesEmailGenerator {

    protected final CreateSDOAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    protected final CreateSDOClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    protected final CreateSDORespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    protected final CreateSDORespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    protected final CreateSDODefendantEmailDTOGenerator defendantEmailDTOGenerator;

    protected final CreateSDODefendantTwoEmailDTOGenerator defendantTwoEmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondent(caseData));
        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        return caseData.isApplicantLiP() ? claimantEmailDTOGenerator.buildEmailDTO(caseData) : appSolOneEmailDTOGenerator.buildEmailDTO(caseData);
    }

    private Set<EmailDTO> getRespondent(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();

        if (caseData.isRespondent1LiP()) {
            recipients.add(defendantEmailDTOGenerator.buildEmailDTO(caseData));
        } else {
            recipients.add(respSolOneEmailDTOGenerator.buildEmailDTO(caseData));
        }

        if (caseData.getRespondent2() != null) {
            if (caseData.isRespondent2LiP()) {
                recipients.add(defendantTwoEmailDTOGenerator.buildEmailDTO(caseData));
            } else {
                recipients.add(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData));
            }
        }

        return recipients;
    }
}

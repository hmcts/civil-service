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
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(getApplicant(caseData, taskId));
        partiesToEmail.addAll(getRespondents(caseData, taskId));
        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData, String taskId) {
        return caseData.isApplicantLiP() ? claimantEmailDTOGenerator.buildEmailDTO(caseData, taskId) : appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId);
    }

    private Set<EmailDTO> getRespondents(CaseData caseData, String taskId) {
        Set<EmailDTO> recipients = new HashSet<>();

        if (caseData.isRespondent1LiP()) {
            recipients.add(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        } else {
            recipients.add(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        }

        if (caseData.getRespondent2() != null) {
            if (caseData.isRespondent2LiP()) {
                recipients.add(defendantTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId));
            } else {
                recipients.add(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId));
            }
        }

        return recipients;
    }
}

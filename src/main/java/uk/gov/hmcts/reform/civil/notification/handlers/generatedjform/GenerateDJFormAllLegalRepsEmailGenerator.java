package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
@AllArgsConstructor
@Slf4j
public class GenerateDJFormAllLegalRepsEmailGenerator extends PartiesEmailGenerator {

    private final GenerateDJFormApprovedAppSolOneEmailDTOGenerator approvedAppSolOneEmailDTOGenerator;
    private final GenerateDJFormApprovedRespSolOneEmailDTOGenerator approvedRespSolOneEmailDTOGenerator;
    private final GenerateDJFormApprovedRespSolTwoEmailDTOGenerator approvedRespSolOneEmailDTOGenerator;

    private final GenerateDJFormRequestedAppSolOneEmailDTOGenerator requestedAppSolOneEmailDTOGenerator;
    private final GenerateDJFormRequestedRespSolOneEmailDTOGenerator requestedRespSolOneEmailDTOGenerator
    private final GenerateDJFormRequestedRespSolTwoEmailDTOGenerator requestedRespSolTwoEmailDTOGenerator;

    private final GenerateDJFormHelper generateDJFormHelper;


    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());

        Set<EmailDTO> applicantEmailDTO = getApplicant(caseData);
        partiesToEmail.addAll(applicantEmailDTO);
        partiesToEmail.addAll(getRespondents(caseData));

        return partiesToEmail;
    }

    private Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();

        return recipients;
    }

    private Set<EmailDTO> getApplicant(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (isOneVTwoTwoLegalRep(caseData)) {
            if (generateDJFormHelper.checkDefendantRequested(caseData, true)
            || generateDJFormHelper.checkIfBothDefendants(caseData)) {
                recipients.add(requestedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData));
            }
            if (generateDJFormHelper.checkDefendantRequested(caseData, false)
                || generateDJFormHelper.checkIfBothDefendants(caseData)) {
                recipients.add(requestedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData));
            }
        } else {
            recipients.add(approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData));
        }
        return recipients;
    }
}

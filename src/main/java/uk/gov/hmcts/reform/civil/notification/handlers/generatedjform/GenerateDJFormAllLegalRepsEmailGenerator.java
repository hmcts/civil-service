package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
@AllArgsConstructor
@Slf4j
public class GenerateDJFormAllLegalRepsEmailGenerator implements PartiesEmailGenerator {

    private final GenerateDJFormApprovedAppSolOneEmailDTOGenerator approvedAppSolOneEmailDTOGenerator;
    private final GenerateDJFormApprovedRespSolOneEmailDTOGenerator approvedRespSolOneEmailDTOGenerator;
    private final GenerateDJFormApprovedRespSolTwoEmailDTOGenerator approvedRespSolTwoEmailDTOGenerator;

    private final GenerateDJFormRequestedAppSolOneEmailDTOGenerator requestedAppSolOneEmailDTOGenerator;
    private final GenerateDJFormRequestedRespSolOneEmailDTOGenerator requestedRespSolOneEmailDTOGenerator;
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
        if (isOneVTwoTwoLegalRep(caseData)) {
            if (generateDJFormHelper.checkDefendantRequested(caseData, true)) {
                recipients.add(requestedRespSolOneEmailDTOGenerator.buildEmailDTO(caseData));
            } else if (generateDJFormHelper.checkDefendantRequested(caseData, false)) {
                recipients.add(requestedRespSolTwoEmailDTOGenerator.buildEmailDTO(caseData));
            } else if (generateDJFormHelper.checkIfBothDefendants(caseData)) {
                recipients.add(approvedRespSolOneEmailDTOGenerator.buildEmailDTO(caseData));
                recipients.add(approvedRespSolTwoEmailDTOGenerator.buildEmailDTO(caseData));
            }
        } else {
            recipients.add(approvedRespSolOneEmailDTOGenerator.buildEmailDTO(caseData));
        }
        return recipients;
    }

    private Set<EmailDTO> getApplicant(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (isOneVTwoTwoLegalRep(caseData)) {
            if (generateDJFormHelper.checkDefendantRequested(caseData, true)
                || generateDJFormHelper.checkDefendantRequested(caseData, false)) {
                recipients.add(requestedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData));
            } else if (generateDJFormHelper.checkIfBothDefendants(caseData)) {
                recipients.add(approvedRespSolOneEmailDTOGenerator.buildEmailDTO(caseData));
                EmailDTO emailDTO = approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData);

                //If checkIfBothDefendants true - update defendant name key
                Map<String, String> updateProps = new HashMap<>(emailDTO.getParameters());;
                emailDTO.setParameters(generateDJFormHelper.updateRespondent2Properties(updateProps, caseData));
                recipients.add(emailDTO);
            }
        } else {
            recipients.add(approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData));
        }
        return recipients;
    }
}

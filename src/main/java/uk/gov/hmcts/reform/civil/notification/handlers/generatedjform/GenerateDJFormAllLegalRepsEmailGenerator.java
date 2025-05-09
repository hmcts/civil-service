package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
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

    public Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (isOneVTwoTwoLegalRep(caseData)) {
            if (generateDJFormHelper.checkDefendantRequested(caseData, true)) {
                addIfPartyNeedsNotification(caseData,
                                            requestedRespSolOneEmailDTOGenerator,
                                            recipients);
            } else if (generateDJFormHelper.checkDefendantRequested(caseData, false)) {
                addIfPartyNeedsNotification(caseData,
                                            requestedRespSolTwoEmailDTOGenerator,
                                            recipients);
            } else if (generateDJFormHelper.checkIfBothDefendants(caseData)) {
                addIfPartyNeedsNotification(caseData,
                                            approvedRespSolOneEmailDTOGenerator,
                                            recipients);
                addIfPartyNeedsNotification(caseData,
                                            approvedRespSolTwoEmailDTOGenerator,
                                            recipients);
            }
        } else {
            addIfPartyNeedsNotification(caseData,
                                        approvedRespSolOneEmailDTOGenerator,
                                        recipients);
        }
        return recipients;
    }

    public Set<EmailDTO> getApplicant(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (isOneVTwoTwoLegalRep(caseData)) {
            if (generateDJFormHelper.checkDefendantRequested(caseData, true)
                || generateDJFormHelper.checkDefendantRequested(caseData, false)) {
                recipients.add(requestedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData));
            } else if (generateDJFormHelper.checkIfBothDefendants(caseData)) {
                //Add respondent1 properties and send app sol email
                recipients.add(approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData));

                //Add respondent2 properties and send new app solicitor email
                EmailDTO emailDTO = approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData);

                //If checkIfBothDefendants true - update defendant name key
                Map<String, String> updateProps = new HashMap<>(emailDTO.getParameters());
                emailDTO.setParameters(generateDJFormHelper.updateRespondent2Properties(updateProps, caseData));
                recipients.add(emailDTO);
            }
        } else {
            recipients.add(approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData));
        }
        return recipients;
    }

    public void addIfPartyNeedsNotification(CaseData caseData,
                                             EmailDTOGenerator generator,
                                             Set<EmailDTO> partiesToEmail) {
        if ((generator != null) && generator.getShouldNotify(caseData)) {
            log.info("Generating email for party [{}] for case ID: {}",
                     generator.getClass().getSimpleName(), caseData.getCcdCaseReference());
            partiesToEmail.add(generator.buildEmailDTO(caseData));
        }
    }
}

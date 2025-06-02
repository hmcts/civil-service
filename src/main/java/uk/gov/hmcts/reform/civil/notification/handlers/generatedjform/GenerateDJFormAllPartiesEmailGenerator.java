package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
@Slf4j
public class GenerateDJFormAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    private final GenerateDJFormApprovedAppSolOneEmailDTOGenerator approvedAppSolOneEmailDTOGenerator;
    private final GenerateDJFormApprovedRespSolOneEmailDTOGenerator approvedRespSolOneEmailDTOGenerator;
    private final GenerateDJFormApprovedRespSolTwoEmailDTOGenerator approvedRespSolTwoEmailDTOGenerator;

    private final GenerateDJFormRequestedAppSolOneEmailDTOGenerator requestedAppSolOneEmailDTOGenerator;
    private final GenerateDJFormRequestedRespSolOneEmailDTOGenerator requestedRespSolOneEmailDTOGenerator;
    private final GenerateDJFormRequestedRespSolTwoEmailDTOGenerator requestedRespSolTwoEmailDTOGenerator;

    private final GenerateDJFormHelper generateDJFormHelper;

    public GenerateDJFormAllPartiesEmailGenerator(
        GenerateDJFormApprovedAppSolOneEmailDTOGenerator approvedAppSolOneEmailDTOGenerator,
        GenerateDJFormApprovedRespSolOneEmailDTOGenerator approvedRespSolOneEmailDTOGenerator,
        GenerateDJFormApprovedRespSolTwoEmailDTOGenerator approvedRespSolTwoEmailDTOGenerator,
        GenerateDJFormRequestedAppSolOneEmailDTOGenerator requestedAppSolOneEmailDTOGenerator,
        GenerateDJFormRequestedRespSolOneEmailDTOGenerator requestedRespSolOneEmailDTOGenerator,
        GenerateDJFormRequestedRespSolTwoEmailDTOGenerator requestedRespSolTwoEmailDTOGenerator,
        GenerateDJFormHelper generateDJFormHelper
    ) {
        super(List.of(
            approvedAppSolOneEmailDTOGenerator,
            approvedRespSolOneEmailDTOGenerator,
            approvedRespSolTwoEmailDTOGenerator,
            requestedAppSolOneEmailDTOGenerator,
            requestedRespSolOneEmailDTOGenerator,
            requestedRespSolTwoEmailDTOGenerator
        ));
        this.approvedAppSolOneEmailDTOGenerator = approvedAppSolOneEmailDTOGenerator;
        this.approvedRespSolOneEmailDTOGenerator = approvedRespSolOneEmailDTOGenerator;
        this.approvedRespSolTwoEmailDTOGenerator = approvedRespSolTwoEmailDTOGenerator;
        this.requestedAppSolOneEmailDTOGenerator = requestedAppSolOneEmailDTOGenerator;
        this.requestedRespSolOneEmailDTOGenerator = requestedRespSolOneEmailDTOGenerator;
        this.requestedRespSolTwoEmailDTOGenerator = requestedRespSolTwoEmailDTOGenerator;
        this.generateDJFormHelper = generateDJFormHelper;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());

        Set<EmailDTO> applicantEmailDTO = getApplicant(caseData, taskId);
        partiesToEmail.addAll(applicantEmailDTO);
        partiesToEmail.addAll(getRespondents(caseData, taskId));

        return partiesToEmail;
    }

    public Set<EmailDTO> getRespondents(CaseData caseData, String taskId) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (isOneVTwoLegalRep(caseData) || isOneVTwoTwoLegalRep(caseData)) {
            if (generateDJFormHelper.checkDefendantRequested(caseData, true)) {
                addIfPartyNeedsNotification(caseData,
                                            taskId,
                                            requestedRespSolOneEmailDTOGenerator,
                                            recipients);
            } else if (generateDJFormHelper.checkDefendantRequested(caseData, false)) {
                addIfPartyNeedsNotification(caseData,
                                            taskId,
                                            requestedRespSolTwoEmailDTOGenerator,
                                            recipients);
            } else if (generateDJFormHelper.checkIfBothDefendants(caseData)) {
                addIfPartyNeedsNotification(caseData,
                                            taskId,
                                            approvedRespSolOneEmailDTOGenerator,
                                            recipients);
                addIfPartyNeedsNotification(caseData,
                                            taskId,
                                            approvedRespSolTwoEmailDTOGenerator,
                                            recipients);
            }
        } else {
            addIfPartyNeedsNotification(caseData,
                                        taskId,
                                        approvedRespSolOneEmailDTOGenerator,
                                        recipients);
        }
        return recipients;
    }

    public Set<EmailDTO> getApplicant(CaseData caseData, String taskId) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (isOneVTwoLegalRep(caseData) || isOneVTwoTwoLegalRep(caseData)) {
            if (generateDJFormHelper.checkDefendantRequested(caseData, true)
                || generateDJFormHelper.checkDefendantRequested(caseData, false)) {
                recipients.add(requestedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId));
            } else if (generateDJFormHelper.checkIfBothDefendants(caseData)) {
                //Add respondent1 properties and send app sol email
                recipients.add(approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId));

                //Add respondent2 properties and send new app solicitor email
                EmailDTO emailDTO = approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId);

                //If checkIfBothDefendants true - update defendant name key
                Map<String, String> updateProps = new HashMap<>(emailDTO.getParameters());
                emailDTO.setParameters(generateDJFormHelper.updateRespondent2Properties(updateProps, caseData));
                recipients.add(emailDTO);
            }
        } else {
            recipients.add(approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        }
        return recipients;
    }

    public void addIfPartyNeedsNotification(CaseData caseData, String taskId,
                                             EmailDTOGenerator generator,
                                             Set<EmailDTO> partiesToEmail) {
        if ((generator != null) && generator.getShouldNotify(caseData)) {
            log.info("Generating email for party [{}] for case ID: {}",
                     generator.getClass().getSimpleName(), caseData.getCcdCaseReference());
            partiesToEmail.add(generator.buildEmailDTO(caseData, taskId));
        }
    }
}

package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCase;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCases;
import uk.gov.hmcts.reform.civil.service.mediation.MediationDTO;
import uk.gov.hmcts.reform.civil.service.mediation.MediationJsonService;
import uk.gov.hmcts.reform.civil.service.search.MediationCasesSearchService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.List.of;
import static uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment.json;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateMediationJsonAndTransferTaskHandler extends BaseExternalTaskHandler {

    private final MediationCasesSearchService caseSearchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final MediationJsonService mediationJsonService;
    private final SendGridClient sendGridClient;
    private final MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    private static final String SUBJECT = "OCMC Mediation Data";
    private static final String FILENAME = "ocmc_mediation_data.json";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        LocalDate claimMovedDate;
        if (externalTask.getVariable("claimMovedDate") != null) {
            claimMovedDate = LocalDate.parse(externalTask.getVariable("claimMovedDate").toString(), DATE_FORMATTER);
        } else {
            claimMovedDate = LocalDate.now().minusDays(1);
        }
        List<CaseDetails> cases = caseSearchService.getInMediationCases(claimMovedDate, true);
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());
        if (!cases.isEmpty()) {
            StringBuilder sb = new StringBuilder().append("JSON case IDs: ");
            for (CaseDetails caseDetail : cases) {
                sb.append(caseDetail.getId());
                sb.append("\n");
            }
            log.info(sb.toString());
        }
        List<CaseData> inMediationCases = cases.stream()
            .map(caseDetailsConverter::toCaseData)
            .toList();
        try {
            if (!inMediationCases.isEmpty()) {
                List<MediationCase> casesList = new ArrayList<>();
                for (CaseData caseData : inMediationCases) {
                    MediationCase mediationCase = generateJsonForCase(caseData);
                    casesList.add(mediationCase);
                }

                MediationDTO mediationDTO = convertToMediationDTO(casesList);

                Optional<EmailData> emailData = prepareEmail(mediationDTO);

                if (externalTask.getVariable("dontSendEmail") == null) {
                    emailData.ifPresent(data -> sendGridClient.sendEmail(
                        mediationCSVEmailConfiguration.getSender(),
                        data
                    ));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ExternalTaskData.builder().build();
    }

    private Optional<EmailData> prepareEmail(MediationDTO mediationDTO) {
        return Optional.of(EmailData.builder()
                               .to("madhan.mahadevan@hmcts.net")
                               .subject(SUBJECT)
                               .attachments(of(json(mediationDTO.getJsonRawData(), FILENAME)))
                               .build());
    }

    private MediationCase generateJsonForCase(CaseData caseData) {
        return mediationJsonService.generateJsonContent(caseData);
    }

    private MediationDTO convertToMediationDTO(List<MediationCase> list) throws JsonProcessingException {
        MediationCases cases = MediationCases.builder()
            .cases(list)
            .build();
        return MediationDTO.builder()
            .jsonRawData(cases.toJsonString().getBytes())
            .build();
    }
}

package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCSVService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCsvServiceFactory;
import uk.gov.hmcts.reform.civil.service.search.MediationCasesSearchService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class GenerateCsvAndTransferTaskHandler extends GenerateMediationFileAndTransferTaskHandler {

    private final MediationCsvServiceFactory mediationCsvServiceFactory;

    private final MediationCSVEmailConfiguration mediationCSVEmailConfiguration;

    private static final String FILENAME = "ocmc_mediation_data.csv";

    protected GenerateCsvAndTransferTaskHandler(MediationCasesSearchService caseSearchService,
                                                CoreCaseDataService coreCaseDataService,
                                                CaseDetailsConverter caseDetailsConverter,
                                                SendGridClient sendGridClient,
                                                MediationCSVEmailConfiguration mediationCSVEmailConfiguration,
                                                MediationCsvServiceFactory mediationCsvServiceFactory,
                                                MediationCSVEmailConfiguration mediationCSVEmailConfiguration1) {
        super(
            caseSearchService,
            coreCaseDataService,
            caseDetailsConverter,
            sendGridClient,
            mediationCSVEmailConfiguration
        );
        this.mediationCsvServiceFactory = mediationCsvServiceFactory;
        this.mediationCSVEmailConfiguration = mediationCSVEmailConfiguration1;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        LocalDate claimMovedDate;
        if (externalTask.getVariable("claimMovedDate") != null) {
            claimMovedDate = LocalDate.parse(externalTask.getVariable("claimMovedDate").toString(), DATE_FORMATTER);
        } else {
            claimMovedDate = LocalDate.now().minusDays(7);
        }
        List<CaseDetails> cases = caseSearchService.getInMediationCases(claimMovedDate, false);
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());
        if (!cases.isEmpty()) {
            StringBuilder sb = new StringBuilder().append("CSV case IDs: ");
            for (CaseDetails caseDetail : cases) {
                sb.append(caseDetail.getId());
                sb.append("\n");
            }
            log.info(sb.toString());
        }
        List<CaseData> inMediationCases = cases.stream()
            .map(caseDetailsConverter::toCaseData)
            .toList();
        String[] headers = getCSVHeaders();
        StringBuilder csvColContent = new StringBuilder();
        try {
            if (!inMediationCases.isEmpty()) {
                inMediationCases.forEach(caseData ->
                                             csvColContent.append(generateCsvContent(caseData)));

                String generateCsvData = generateCSVRow(headers) + csvColContent;
                Optional<EmailData> emailData = prepareEmail(generateCsvData);

                if (externalTask.getVariable("dontSendEmail") == null) {
                    emailData.ifPresent(data -> sendMediationFileEmail(data));
                }

                inMediationCases.stream().forEach(this::setMediationFileSent);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ExternalTaskData.builder().build();
    }

    private Optional<EmailData> prepareEmail(String generateCsvData) {
        InputStreamSource inputSource = new ByteArrayResource(generateCsvData.getBytes(StandardCharsets.UTF_8));

        return Optional.of(EmailData.builder()
                               .to(mediationCSVEmailConfiguration.getRecipient())
                               .subject(SUBJECT)
                               .attachments(List.of(new EmailAttachment(inputSource, "text/csv", FILENAME)))
                               .build());
    }

    private String generateCsvContent(CaseData caseData) {
        MediationCSVService mediationCSVService = mediationCsvServiceFactory.getMediationCSVService(caseData);
        return mediationCSVService.generateCSVContent(caseData);
    }

    private String generateCSVRow(String[] row) {
        StringBuilder builder = new StringBuilder();

        for (String s : row) {
            builder.append(s).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("\r\n");

        return builder.toString();
    }

    private String[] getCSVHeaders() {
        return new String[]{"SITE_ID", "CASE_TYPE", "CHECK_LIST", "PARTY_STATUS", "CASE_NUMBER", "AMOUNT", "PARTY_TYPE",
            "COMPANY_NAME", "CONTACT_NAME", "CONTACT_NUMBER", "CONTACT_EMAIL", "PILOT", "CASE_TITLE"};
    }
}

package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCSVService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCsvServiceFactory;
import uk.gov.hmcts.reform.civil.service.search.MediationCasesSearchService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateCsvAndTransferTaskHandler implements BaseExternalTaskHandler {

    private final MediationCasesSearchService caseSearchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final MediationCsvServiceFactory mediationCsvServiceFactory;
    private final SendGridClient sendGridClient;
    private final MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    private static final String subject = "OCMC Mediation Data";
    private static final String filename = "ocmc_mediation_data.csv";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final FeatureToggleService toggleService;
    private final RuntimeService runtimeService;

    @Override
    public void handleTask(ExternalTask externalTask) {

        List<CaseData> inMediationCases;
        LocalDate claimMovedDate;
        if (externalTask.getVariable("claimMovedDate") != null) {
            claimMovedDate = LocalDate.parse(externalTask.getVariable("claimMovedDate").toString(), DATE_FORMATTER);
        } else {
            claimMovedDate = LocalDate.now().minusDays(1);
        }
        List<CaseDetails> cases = caseSearchService.getInMediationCases(claimMovedDate, false);
        inMediationCases = cases.stream()
            .map(caseDetailsConverter::toCaseData)
            .toList();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), inMediationCases.size());
        String[] headers = getCSVHeaders();
        StringBuilder csvColContent = new StringBuilder();
        runtimeService.setVariable(externalTask.getProcessInstanceId(), "carmFeatureEnabled",
                                   toggleService.isFeatureEnabled("carm"));
        try {
            if (!inMediationCases.isEmpty()) {
                inMediationCases.forEach(caseData ->
                                             csvColContent.append(generateCsvContent(caseData)));

                String generateCsvData = generateCSVRow(headers) + csvColContent;
                Optional<EmailData> emailData = prepareEmail(generateCsvData);

                emailData.ifPresent(data -> sendGridClient.sendEmail(mediationCSVEmailConfiguration.getSender(), data));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private Optional<EmailData> prepareEmail(String generateCsvData) {
        InputStreamSource inputSource = new ByteArrayResource(generateCsvData.getBytes(StandardCharsets.UTF_8));

        return Optional.of(EmailData.builder()
                               .to(mediationCSVEmailConfiguration.getRecipient())
                               .subject(subject)
                               .attachments(List.of(new EmailAttachment(inputSource, "text/csv", filename)))
                               .build());
    }

    private String generateCsvContent(CaseData caseData) {
        boolean isR2FlagEnabled = toggleService.isLipVLipEnabled();
        MediationCSVService mediationCSVService = mediationCsvServiceFactory.getMediationCSVService(caseData);
        return mediationCSVService.generateCSVContent(caseData, isR2FlagEnabled);
    }

    private String generateCSVRow(String[] row) {
        StringBuilder builder = new StringBuilder();

        for (String s : row) {
            builder.append(s).append(",");
        }
        builder.append("\n");

        return builder.toString();
    }

    private String[] getCSVHeaders() {
        String[] csvHeaders = new String[] {"SITE_ID", "CASE_NUMBER", "CASE_TYPE", "AMOUNT", "PARTY_TYPE", "COMPANY_NAME",
            "CONTACT_NAME", "CONTACT_NUMBER", "CHECK_LIST", "PARTY_STATUS", "CONTACT_EMAIL", "PILOT"};
        if (toggleService.isLipVLipEnabled()) {
            String[] additionalCsvHeaders = Arrays.copyOf(csvHeaders, csvHeaders.length + 1);
            additionalCsvHeaders[csvHeaders.length] = "WELSH_FLAG";
            return additionalCsvHeaders;
        }
        return csvHeaders;
    }
}

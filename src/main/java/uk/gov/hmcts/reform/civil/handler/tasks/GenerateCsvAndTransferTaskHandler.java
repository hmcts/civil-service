package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
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
import uk.gov.hmcts.reform.civil.service.mediation.MediationCSVService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCsvServiceFactory;
import uk.gov.hmcts.reform.civil.service.search.CaseStateSearchService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.time.LocalDate.now;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateCsvAndTransferTaskHandler implements BaseExternalTaskHandler {

    private final CaseStateSearchService caseSearchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final MediationCsvServiceFactory mediationCsvServiceFactory;
    private final SendGridClient sendGridClient;
    private final MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    private static final String subject = "OCMC Mediation Data";
    private static final String filename = "ocmc_mediation_data.csv";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = caseSearchService.getCases();
        List<CaseData> inMediationCases;
        if (externalTask.getVariable("claimMovedDate") != null) {
            LocalDate erroredDate = LocalDate.parse(externalTask.getVariable("claimMovedDate").toString(), DATE_FORMATTER);
            inMediationCases = cases.stream()
               .map(caseDetailsConverter::toCaseData)
               .filter(checkErrorCasesWithDate(erroredDate)).toList();
        } else {
            inMediationCases = cases.stream()
               .map(caseDetailsConverter::toCaseData)
               .filter(checkMediationMovedDate).toList();
        }
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), inMediationCases.size());
        String[] headers = {"SITE_ID", "CASE_TYPE", "CHECK_LIST", "PARTY_STATUS", "CASE_NUMBER", "AMOUNT", "PARTY_TYPE",
            "COMPANY_NAME", "CONTACT_NAME", "CONTACT_NUMBER", "CONTACT_EMAIL", "PILOT"};
        StringBuilder csvColContent = new StringBuilder();
        if (!inMediationCases.isEmpty()) {
            inMediationCases.forEach(caseData ->
                csvColContent.append(generateCsvContent(caseData)));

            String generateCsvData = generateCSVRow(headers) + csvColContent;
            Optional<EmailData> emailData = prepareEmail(generateCsvData);

            emailData.ifPresent(data -> sendGridClient.sendEmail(mediationCSVEmailConfiguration.getSender(), data));
        }
    }

    private  Predicate<CaseData> checkMediationMovedDate = caseData ->
        caseData.getClaimMovedToMediationOn() != null
            && now().minusDays(1).equals(caseData.getClaimMovedToMediationOn());

    private Predicate<CaseData> checkErrorCasesWithDate(LocalDate errorDate) {
        return caseData ->
            caseData.getClaimMovedToMediationOn() != null && errorDate != null
                && errorDate.equals(caseData.getClaimMovedToMediationOn());
    }

    private Optional<EmailData> prepareEmail(String generateCsvData) {
        InputStreamSource inputSource = new ByteArrayResource(generateCsvData.getBytes(StandardCharsets.UTF_8));

        return Optional.of(EmailData.builder()
                               .to("Kiyrean.Dyer-Allen@HMCTS.net")
                               .subject(subject)
                               .attachments(List.of(new EmailAttachment(inputSource, "text/csv", filename)))
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
        builder.append("\n");

        return builder.toString();
    }

}

package uk.gov.hmcts.reform.civil.service.mediation;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import static java.util.List.of;
import static uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment.json;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediationFileTransferService {

    private static final String SUBJECT = "OCMC Mediation Data";
    private static final String CSV_FILENAME = "ocmc_mediation_data.csv";
    private static final String JSON_FILENAME = "ocmc_mediation_data.json";
    private static final String CSV_RECIPIENT_CONFIG_KEY = "mediation.emails.recipient";
    private static final String JSON_RECIPIENT_CONFIG_KEY = "mediation.emails.jsonRecipient";

    private final MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    private final MediationCsvServiceFactory mediationCsvServiceFactory;
    private final MediationJsonService mediationJsonService;
    private final SendGridClient sendGridClient;
    private final CaseTaskTrackingService caseTaskTrackingService;

    public List<CaseData> sendCsv(List<CaseData> cases) {
        if (cases.isEmpty()) {
            return List.of();
        }

        String[] headers = getCSVHeaders();
        StringBuilder csvColContent = new StringBuilder();
        List<CaseData> successfulCases = new ArrayList<>();
        cases.forEach(
            caseData -> {
                try {
                    csvColContent.append(generateCsvContent(caseData));
                    successfulCases.add(caseData);
                } catch (Exception e) {
                    log.error("Generate mediation CSV failed for case with id: '{}'",
                              caseData.getCcdCaseReference(), e);
                    trackGenerationFailure(caseData, CSV_FILENAME);
                }
            }
        );

        if (successfulCases.isEmpty()) {
            return List.of();
        }

        byte[] csvContent = (generateCSVRow(headers) + csvColContent).getBytes(StandardCharsets.UTF_8);
        InputStreamSource inputSource = new ByteArrayResource(csvContent);
        EmailAttachment attachment = new EmailAttachment(inputSource, "text/csv", CSV_FILENAME);
        EmailData emailData = new EmailData()
            .setTo(requireConfiguredRecipient(
                mediationCSVEmailConfiguration.getRecipient(),
                CSV_RECIPIENT_CONFIG_KEY
            ))
            .setSubject(SUBJECT)
            .setAttachments(List.of(attachment));

        logMediationEmailSendAttempt(
            "CSV",
            CSV_RECIPIENT_CONFIG_KEY,
            emailData.getTo(),
            attachment,
            csvContent.length,
            successfulCases.size()
        );
        sendMediationFileEmail(emailData);
        return successfulCases;
    }

    public List<CaseData> sendJson(List<CaseData> cases) {
        if (cases.isEmpty()) {
            return List.of();
        }

        List<MediationCase> casesList = new ArrayList<>();
        List<CaseData> successfulCases = new ArrayList<>();
        for (CaseData caseData : cases) {
            try {
                MediationCase mediationCase = mediationJsonService.generateJsonContent(caseData);
                casesList.add(mediationCase);
                successfulCases.add(caseData);
            } catch (Exception e) {
                log.error("Generate mediation JSON failed for case with id: '{}'",
                          caseData.getCcdCaseReference(), e);
                trackGenerationFailure(caseData, JSON_FILENAME);
            }
        }

        if (successfulCases.isEmpty()) {
            return List.of();
        }

        MediationDTO mediationDTO = convertToMediationDTO(casesList);
        EmailAttachment attachment = json(mediationDTO.getJsonRawData(), JSON_FILENAME);
        EmailData emailData = new EmailData()
            .setTo(requireConfiguredRecipient(
                mediationCSVEmailConfiguration.getJsonRecipient(),
                JSON_RECIPIENT_CONFIG_KEY
            ))
            .setSubject(SUBJECT)
            .setAttachments(of(attachment));

        logMediationEmailSendAttempt(
            "JSON",
            JSON_RECIPIENT_CONFIG_KEY,
            emailData.getTo(),
            attachment,
            mediationDTO.getJsonRawData().length,
            successfulCases.size()
        );
        sendMediationFileEmail(emailData);
        return successfulCases;
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

    private MediationDTO convertToMediationDTO(List<MediationCase> list) {
        try {
            MediationCases cases = new MediationCases(list);
            return new MediationDTO(cases.toJsonString().getBytes());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to generate mediation JSON", e);
        }
    }

    private void sendMediationFileEmail(EmailData data) {
        sendGridClient.sendEmail(
            mediationCSVEmailConfiguration.getSender(),
            data
        );
    }

    private String requireConfiguredRecipient(String recipient, String configKey) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalStateException("Missing mediation email recipient config: " + configKey);
        }
        return recipient;
    }

    private void logMediationEmailSendAttempt(
        String reportType,
        String recipientConfigKey,
        String recipient,
        EmailAttachment attachment,
        int attachmentBytes,
        int caseCount
    ) {
        log.info(
            "MMT_MEDIATION_EMAIL_SEND_ATTEMPT subject={} reportType={} recipientConfig={} recipientHash={} "
                + "recipientDomain={} attachmentName={} attachmentContentType={} attachmentBytes={} caseCount={}",
            SUBJECT,
            reportType,
            recipientConfigKey,
            recipientHash(recipient),
            recipientDomain(recipient),
            attachment.getFilename(),
            attachment.getContentType(),
            attachmentBytes,
            caseCount
        );
    }

    private String recipientHash(String recipient) {
        String normalizedRecipient = normalizeRecipient(recipient);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(normalizedRecipient.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String recipientDomain(String recipient) {
        String normalizedRecipient = normalizeRecipient(recipient);
        int atIndex = normalizedRecipient.lastIndexOf('@');
        if (atIndex < 0 || atIndex == normalizedRecipient.length() - 1) {
            return "unknown";
        }
        return normalizedRecipient.substring(atIndex + 1);
    }

    private String normalizeRecipient(String recipient) {
        return recipient.trim().toLowerCase(Locale.ROOT);
    }

    private void trackGenerationFailure(CaseData caseData, String fileName) {
        caseTaskTrackingService.trackCaseTask(
            caseData.getCcdCaseReference().toString(),
            SUBJECT,
            fileName,
            null
        );
    }
}

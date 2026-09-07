package uk.gov.hmcts.reform.civil.service.mediation;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment.json;

@Service
@RequiredArgsConstructor
public class MediationFileTransferService {

    private static final String SUBJECT = "OCMC Mediation Data";
    private static final String CSV_FILENAME = "ocmc_mediation_data.csv";
    private static final String JSON_FILENAME = "ocmc_mediation_data.json";

    private final MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    private final MediationCsvServiceFactory mediationCsvServiceFactory;
    private final MediationJsonService mediationJsonService;
    private final SendGridClient sendGridClient;

    public void sendCsv(List<CaseData> cases) {
        if (cases.isEmpty()) {
            return;
        }

        String[] headers = getCSVHeaders();
        StringBuilder csvColContent = new StringBuilder();
        cases.forEach(caseData -> csvColContent.append(generateCsvContent(caseData)));

        String generateCsvData = generateCSVRow(headers) + csvColContent;
        InputStreamSource inputSource = new ByteArrayResource(generateCsvData.getBytes(StandardCharsets.UTF_8));
        EmailData emailData = new EmailData()
            .setTo(mediationCSVEmailConfiguration.getRecipient())
            .setSubject(SUBJECT)
            .setAttachments(List.of(new EmailAttachment(inputSource, "text/csv", CSV_FILENAME)));

        sendMediationFileEmail(emailData);
    }

    public void sendJson(List<CaseData> cases) {
        if (cases.isEmpty()) {
            return;
        }

        List<MediationCase> casesList = new ArrayList<>();
        for (CaseData caseData : cases) {
            casesList.add(mediationJsonService.generateJsonContent(caseData));
        }

        MediationDTO mediationDTO = convertToMediationDTO(casesList);
        EmailData emailData = new EmailData()
            .setTo(mediationCSVEmailConfiguration.getJsonRecipient())
            .setSubject(SUBJECT)
            .setAttachments(of(json(mediationDTO.getJsonRawData(), JSON_FILENAME)));

        sendMediationFileEmail(emailData);
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
}

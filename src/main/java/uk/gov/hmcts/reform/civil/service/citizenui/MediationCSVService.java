package uk.gov.hmcts.reform.civil.service.citizenui;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class MediationCSVService {
    private final String toEmail = "smallclaimsmediation@justice.gov.uk";
    private final String subject = "OCMC Mediation Data";
    private final String filename = "ocmc_mediation_data.csv";

    public Optional<EmailData> prepareEmail(CaseData data) {
        String content = generateAttachmentContent(data);
        InputStreamSource inputSource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));

        return Optional.of(EmailData.builder()
                               .to(toEmail)
                               .subject(subject)
                               .attachments(List.of(new EmailAttachment(inputSource, "text/csv", filename)))
                               .build());
    }

    public String generateAttachmentContent(CaseData data) {

        String [] headers = {"SITE_ID", "CASE_NUMBER", "CASE_TYPE", "AMOUNT", "PARTY_TYPE", "COMPANY_NAME",
            "CONTACT_NAME", "CONTACT_NUMBER", "CHECK_LIST", "PART_STATUS", "CONTACT_EMAIL", "PILOT"};

        String [] claimantData = {
            "5", data.getLegacyCaseReference(), "1", data.getTotalClaimAmount().toString(),
            data.getApplicant1().getType().toString(), data.getApplicant1().getCompanyName(),
            data.getApplicant1().getPartyName(), data.getApplicant1().getPartyPhone(),
            "4", "5", data.getApplicant1().getPartyEmail(),
            data.getTotalClaimAmount().compareTo(new BigDecimal(10000)) < 0 ? "Yes" : "No"
        };

        String [] respondentData = {
            "5", data.getLegacyCaseReference(), "1", data.getTotalClaimAmount().toString(),
            data.getRespondent1().getType().toString(), data.getRespondent1().getCompanyName(),
            data.getRespondent1().getPartyName(), data.getRespondent1().getPartyPhone(),
            "4", "5", data.getRespondent1().getPartyEmail(),
            data.getTotalClaimAmount().compareTo(new BigDecimal(10000)) < 0 ? "Yes" : "No"
        };

        return generateCSVRow(headers)
            + generateCSVRow(claimantData)
            + generateCSVRow(respondentData);
    }

    private String generateCSVRow(String [] row) {
        StringBuilder builder = new StringBuilder();

        for (String s : row) {
            builder.append(s).append(",");
        }
        builder.append("\n");

        return builder.toString();
    }
}

package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediationFileTransferServiceTest {

    private static final String SENDER = "sender@example.com";
    private static final String CSV_RECIPIENT = "csv@example.com";
    private static final String JSON_RECIPIENT = "json@example.com";

    @Mock
    private MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    @Mock
    private MediationCsvServiceFactory mediationCsvServiceFactory;
    @Mock
    private MediationJsonService mediationJsonService;
    @Mock
    private SendGridClient sendGridClient;
    @Mock
    private MediationCSVService mediationCSVService;

    @InjectMocks
    private MediationFileTransferService mediationFileTransferService;

    @Test
    void shouldSendCsvAttachmentForCases() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        when(mediationCsvServiceFactory.getMediationCSVService(caseData)).thenReturn(mediationCSVService);
        when(mediationCSVService.generateCSVContent(caseData)).thenReturn("row-one\r\n");
        when(mediationCSVEmailConfiguration.getRecipient()).thenReturn(CSV_RECIPIENT);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(SENDER);

        mediationFileTransferService.sendCsv(List.of(caseData));

        EmailData emailData = captureEmailData();
        assertThat(emailData.getTo()).isEqualTo(CSV_RECIPIENT);
        assertThat(emailData.getSubject()).isEqualTo("OCMC Mediation Data");
        assertThat(emailData.getAttachments()).singleElement().satisfies(attachment -> {
            assertThat(attachment.getFilename()).isEqualTo("ocmc_mediation_data.csv");
            assertThat(attachment.getContentType()).isEqualTo("text/csv");
            assertThat(attachmentContent(attachment)).isEqualTo(
                "SITE_ID,CASE_TYPE,CHECK_LIST,PARTY_STATUS,CASE_NUMBER,AMOUNT,PARTY_TYPE,"
                    + "COMPANY_NAME,CONTACT_NAME,CONTACT_NUMBER,CONTACT_EMAIL,PILOT,CASE_TITLE\r\n"
                    + "row-one\r\n"
            );
        });
    }

    @Test
    void shouldSendJsonAttachmentForCases() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        MediationCase mediationCase = new MediationCase().setCcdCaseNumber(1L);
        when(mediationJsonService.generateJsonContent(caseData)).thenReturn(mediationCase);
        when(mediationCSVEmailConfiguration.getJsonRecipient()).thenReturn(JSON_RECIPIENT);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(SENDER);

        mediationFileTransferService.sendJson(List.of(caseData));

        EmailData emailData = captureEmailData();
        assertThat(emailData.getTo()).isEqualTo(JSON_RECIPIENT);
        assertThat(emailData.getSubject()).isEqualTo("OCMC Mediation Data");
        assertThat(emailData.getAttachments()).singleElement().satisfies(attachment -> {
            assertThat(attachment.getFilename()).isEqualTo("ocmc_mediation_data.json");
            assertThat(attachment.getContentType()).isEqualTo("application/json");
            assertThat(attachmentContent(attachment)).contains("\"ccdCaseNumber\":1");
        });
    }

    @Test
    void shouldNotSendCsvWhenThereAreNoCases() {
        mediationFileTransferService.sendCsv(List.of());

        verifyNoInteractions(sendGridClient, mediationCsvServiceFactory);
    }

    @Test
    void shouldNotSendJsonWhenThereAreNoCases() {
        mediationFileTransferService.sendJson(List.of());

        verifyNoInteractions(sendGridClient, mediationJsonService);
    }

    private EmailData captureEmailData() {
        ArgumentCaptor<EmailData> emailDataCaptor = ArgumentCaptor.forClass(EmailData.class);
        verify(sendGridClient).sendEmail(eq(SENDER), emailDataCaptor.capture());
        return emailDataCaptor.getValue();
    }

    private String attachmentContent(EmailAttachment attachment) {
        ByteArrayResource data = (ByteArrayResource) attachment.getData();
        return new String(data.getByteArray(), StandardCharsets.UTF_8);
    }
}

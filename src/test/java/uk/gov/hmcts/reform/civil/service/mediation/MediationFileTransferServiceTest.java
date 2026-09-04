package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
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
    @Mock
    private CaseTaskTrackingService caseTaskTrackingService;

    @InjectMocks
    private MediationFileTransferService mediationFileTransferService;

    @Test
    void shouldSendCsvAttachmentForCases(CapturedOutput output) {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        when(mediationCsvServiceFactory.getMediationCSVService(caseData)).thenReturn(mediationCSVService);
        when(mediationCSVService.generateCSVContent(caseData)).thenReturn("row-one\r\n");
        when(mediationCSVEmailConfiguration.getRecipient()).thenReturn(CSV_RECIPIENT);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(SENDER);

        List<CaseData> successfulCases = mediationFileTransferService.sendCsv(List.of(caseData));

        assertThat(successfulCases).containsExactly(caseData);
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
        assertThat(output)
            .contains("MMT_MEDIATION_EMAIL_SEND_ATTEMPT")
            .contains("subject=OCMC Mediation Data")
            .contains("reportType=CSV")
            .contains("recipientConfig=mediation.emails.recipient")
            .contains("recipientHash=")
            .contains("recipientDomain=example.com")
            .contains("attachmentName=ocmc_mediation_data.csv")
            .contains("attachmentContentType=text/csv")
            .contains("attachmentBytes=154")
            .contains("caseCount=1")
            .doesNotContain(CSV_RECIPIENT);
    }

    @Test
    void shouldSendJsonAttachmentForCases(CapturedOutput output) {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        MediationCase mediationCase = new MediationCase().setCcdCaseNumber(1L);
        when(mediationJsonService.generateJsonContent(caseData)).thenReturn(mediationCase);
        when(mediationCSVEmailConfiguration.getJsonRecipient()).thenReturn(JSON_RECIPIENT);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(SENDER);

        List<CaseData> successfulCases = mediationFileTransferService.sendJson(List.of(caseData));

        assertThat(successfulCases).containsExactly(caseData);
        EmailData emailData = captureEmailData();
        assertThat(emailData.getTo()).isEqualTo(JSON_RECIPIENT);
        assertThat(emailData.getSubject()).isEqualTo("OCMC Mediation Data");
        assertThat(emailData.getAttachments()).singleElement().satisfies(attachment -> {
            assertThat(attachment.getFilename()).isEqualTo("ocmc_mediation_data.json");
            assertThat(attachment.getContentType()).isEqualTo("application/json");
            assertThat(attachmentContent(attachment)).contains("\"ccdCaseNumber\":1");
        });
        assertThat(output)
            .contains("MMT_MEDIATION_EMAIL_SEND_ATTEMPT")
            .contains("subject=OCMC Mediation Data")
            .contains("reportType=JSON")
            .contains("recipientConfig=mediation.emails.jsonRecipient")
            .contains("recipientHash=")
            .contains("recipientDomain=example.com")
            .contains("attachmentName=ocmc_mediation_data.json")
            .contains("attachmentContentType=application/json")
            .contains("attachmentBytes=")
            .contains("caseCount=1")
            .doesNotContain(JSON_RECIPIENT);
    }

    @Test
    void shouldNotSendCsvWhenThereAreNoCases() {
        List<CaseData> successfulCases = mediationFileTransferService.sendCsv(List.of());

        assertThat(successfulCases).isEmpty();
        verifyNoInteractions(sendGridClient, mediationCsvServiceFactory);
    }

    @Test
    void shouldNotSendJsonWhenThereAreNoCases() {
        List<CaseData> successfulCases = mediationFileTransferService.sendJson(List.of());

        assertThat(successfulCases).isEmpty();
        verifyNoInteractions(sendGridClient, mediationJsonService);
    }

    @Test
    void shouldTrackCsvGenerationFailureAndSendSuccessfulCases() {
        CaseData failedCase = CaseData.builder().ccdCaseReference(1L).build();
        CaseData successfulCase = CaseData.builder().ccdCaseReference(2L).build();
        when(mediationCsvServiceFactory.getMediationCSVService(failedCase)).thenReturn(mediationCSVService);
        when(mediationCsvServiceFactory.getMediationCSVService(successfulCase)).thenReturn(mediationCSVService);
        when(mediationCSVService.generateCSVContent(failedCase))
            .thenThrow(new RuntimeException("Unable to generate CSV"));
        when(mediationCSVService.generateCSVContent(successfulCase)).thenReturn("row-two\r\n");
        when(mediationCSVEmailConfiguration.getRecipient()).thenReturn(CSV_RECIPIENT);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(SENDER);

        List<CaseData> successfulCases = mediationFileTransferService.sendCsv(List.of(failedCase, successfulCase));

        assertThat(successfulCases).containsExactly(successfulCase);
        verify(caseTaskTrackingService).trackCaseTask("1", "OCMC Mediation Data", "ocmc_mediation_data.csv", null);
        assertThat(attachmentContent(captureEmailData().getAttachments().getFirst()))
            .contains("row-two\r\n")
            .doesNotContain("row-one\r\n");
    }

    @Test
    void shouldNotSendCsvWhenAllCaseGenerationFails() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        when(mediationCsvServiceFactory.getMediationCSVService(caseData)).thenReturn(mediationCSVService);
        when(mediationCSVService.generateCSVContent(caseData))
            .thenThrow(new RuntimeException("Unable to generate CSV"));

        List<CaseData> successfulCases = mediationFileTransferService.sendCsv(List.of(caseData));

        assertThat(successfulCases).isEmpty();
        verify(caseTaskTrackingService).trackCaseTask("1", "OCMC Mediation Data", "ocmc_mediation_data.csv", null);
        verifyNoInteractions(sendGridClient);
    }

    @Test
    void shouldTrackJsonGenerationFailureAndSendSuccessfulCases() {
        CaseData failedCase = CaseData.builder().ccdCaseReference(1L).build();
        CaseData successfulCase = CaseData.builder().ccdCaseReference(2L).build();
        MediationCase mediationCase = new MediationCase().setCcdCaseNumber(2L);
        when(mediationJsonService.generateJsonContent(failedCase))
            .thenThrow(new RuntimeException("Unable to generate JSON"));
        when(mediationJsonService.generateJsonContent(successfulCase)).thenReturn(mediationCase);
        when(mediationCSVEmailConfiguration.getJsonRecipient()).thenReturn(JSON_RECIPIENT);
        when(mediationCSVEmailConfiguration.getSender()).thenReturn(SENDER);

        List<CaseData> successfulCases = mediationFileTransferService.sendJson(List.of(failedCase, successfulCase));

        assertThat(successfulCases).containsExactly(successfulCase);
        verify(caseTaskTrackingService).trackCaseTask("1", "OCMC Mediation Data", "ocmc_mediation_data.json", null);
        assertThat(attachmentContent(captureEmailData().getAttachments().getFirst()))
            .contains("\"ccdCaseNumber\":2")
            .doesNotContain("\"ccdCaseNumber\":1");
    }

    @Test
    void shouldNotSendJsonWhenAllCaseGenerationFails() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        when(mediationJsonService.generateJsonContent(caseData))
            .thenThrow(new RuntimeException("Unable to generate JSON"));

        List<CaseData> successfulCases = mediationFileTransferService.sendJson(List.of(caseData));

        assertThat(successfulCases).isEmpty();
        verify(caseTaskTrackingService).trackCaseTask("1", "OCMC Mediation Data", "ocmc_mediation_data.json", null);
        verifyNoInteractions(sendGridClient);
    }

    @Test
    void shouldNotSendCsvWhenRecipientConfigIsBlank() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        when(mediationCsvServiceFactory.getMediationCSVService(caseData)).thenReturn(mediationCSVService);
        when(mediationCSVService.generateCSVContent(caseData)).thenReturn("row-one\r\n");
        when(mediationCSVEmailConfiguration.getRecipient()).thenReturn(" ");

        assertThatThrownBy(() -> mediationFileTransferService.sendCsv(List.of(caseData)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Missing mediation email recipient config: mediation.emails.recipient");
        verifyNoInteractions(sendGridClient);
    }

    @Test
    void shouldNotSendJsonWhenRecipientConfigIsBlank() {
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        MediationCase mediationCase = new MediationCase().setCcdCaseNumber(1L);
        when(mediationJsonService.generateJsonContent(caseData)).thenReturn(mediationCase);
        when(mediationCSVEmailConfiguration.getJsonRecipient()).thenReturn(" ");

        assertThatThrownBy(() -> mediationFileTransferService.sendJson(List.of(caseData)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Missing mediation email recipient config: mediation.emails.jsonRecipient");
        verifyNoInteractions(sendGridClient);
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

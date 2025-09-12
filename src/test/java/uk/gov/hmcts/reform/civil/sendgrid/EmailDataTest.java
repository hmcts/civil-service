package uk.gov.hmcts.reform.civil.sendgrid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailDataTest {

    @Test
    void shouldReturnTrue_whenHasAnAttachment() {
        EmailData emailData = EmailData.builder()
            .to("to@server.net")
            .subject("my email")
            .message("My email message")
            .attachments(List.of(EmailAttachment.pdf(new byte[]{1, 2, 3}, "test.pdf")))
            .build();

        assertTrue(emailData.hasAttachments());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalse_whenHasNoAttachment(List<EmailAttachment> attachments) {
        EmailData emailData = EmailData.builder()
            .to("to@server.net")
            .subject("my email")
            .message("My email message")
            .attachments(attachments)
            .build();

        assertFalse(emailData.hasAttachments());
    }

    @Test
    void shouldMakeAttachmentsListUnmodifiable() {
        List<EmailAttachment> mutableList = new ArrayList<>();
        mutableList.add(EmailAttachment.pdf(new byte[]{1}, "file.pdf"));

        EmailData emailData = EmailData.builder()
            .to("to@server.net")
            .subject("subject")
            .message("message")
            .attachments(mutableList)
            .build();

        EmailAttachment attachment = EmailAttachment.pdf(new byte[]{2}, "another.pdf");
        List<EmailAttachment> attachments = emailData.getAttachments();
        assertThrows(UnsupportedOperationException.class, () ->
            attachments.add(attachment)
        );
    }

    @Test
    void shouldReturnEmptyListWhenAttachmentsIsNull() {
        EmailData emailData = EmailData.builder()
            .to("to@server.net")
            .subject("subject")
            .message("message")
            .attachments(null)
            .build();

        assertThat(emailData.getAttachments()).isEmpty();
        assertFalse(emailData.hasAttachments());
    }

    @Test
    void shouldSetAllFieldsCorrectly() {
        List<EmailAttachment> attachments = List.of(EmailAttachment.pdf(new byte[]{1}, "file.pdf"));

        EmailData emailData = EmailData.builder()
            .to("recipient@example.com")
            .subject("Test Subject")
            .message("Test Message")
            .attachments(attachments)
            .build();

        assertThat(emailData.getTo()).isEqualTo("recipient@example.com");
        assertThat(emailData.getSubject()).isEqualTo("Test Subject");
        assertThat(emailData.getMessage()).isEqualTo("Test Message");
        assertThat(emailData.getAttachments()).hasSize(1);
        assertTrue(emailData.hasAttachments());
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        EmailData emailData1 = EmailData.builder()
            .to("to@server.net")
            .subject("subject")
            .message("message")
            .attachments(List.of(EmailAttachment.pdf(new byte[]{1}, "file.pdf")))
            .build();

        EmailData emailData2 = EmailData.builder()
            .to("to@server.net")
            .subject("subject")
            .message("message")
            .attachments(List.of(EmailAttachment.pdf(new byte[]{1}, "file.pdf")))
            .build();

        EmailData emailData3 = EmailData.builder()
            .to("different@server.net")
            .subject("subject")
            .message("message")
            .attachments(List.of(EmailAttachment.pdf(new byte[]{1}, "file.pdf")))
            .build();

        assertThat(emailData1).isEqualTo(emailData2)
            .hasSameHashCodeAs(emailData2)
            .isNotEqualTo(emailData3);
    }

    @Test
    void shouldReturnFalseWhenAttachmentsListIsEmpty() {
        EmailData emailData = EmailData.builder()
            .to("to@server.net")
            .subject("subject")
            .message("message")
            .attachments(Collections.emptyList())
            .build();

        assertFalse(emailData.hasAttachments());
        assertThat(emailData.getAttachments()).isEmpty();
    }

    @Test
    void shouldHandleMultipleAttachments() {
        List<EmailAttachment> attachments = List.of(
            EmailAttachment.pdf(new byte[]{1}, "file1.pdf"),
            EmailAttachment.pdf(new byte[]{2}, "file2.pdf"),
            EmailAttachment.pdf(new byte[]{3}, "file3.pdf")
        );

        EmailData emailData = EmailData.builder()
            .to("to@server.net")
            .subject("subject")
            .message("message")
            .attachments(attachments)
            .build();

        assertTrue(emailData.hasAttachments());
        assertThat(emailData.getAttachments()).hasSize(3);
    }
}

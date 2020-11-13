package uk.gov.hmcts.reform.unspec.sendgrid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}

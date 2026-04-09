package uk.gov.hmcts.reform.civil.sendgrid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailDataTest {

    @Test
    void shouldReturnTrue_whenHasAnAttachment() {
        EmailData emailData = new EmailData()
            .setTo("to@server.net")
            .setSubject("my email")
            .setMessage("My email message")
            .setAttachments(List.of(EmailAttachment.pdf(new byte[]{1, 2, 3}, "test.pdf")));

        assertTrue(emailData.hasAttachments());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalse_whenHasNoAttachment(List<EmailAttachment> attachments) {
        EmailData emailData = new EmailData()
            .setTo("to@server.net")
            .setSubject("my email")
            .setMessage("My email message")
            .setAttachments(attachments);

        assertFalse(emailData.hasAttachments());
    }
}

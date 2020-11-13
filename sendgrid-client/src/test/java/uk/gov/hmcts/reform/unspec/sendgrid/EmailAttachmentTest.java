package uk.gov.hmcts.reform.unspec.sendgrid;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailAttachmentTest {

    private static final ByteArrayResource CONTENT = new ByteArrayResource(new byte[]{1, 2, 3, 4});
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};
    private static final String PDF_FILE_NAME = "document.pdf";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String JSON_FILE_NAME = "document.json";
    private static final String JSON_CONTENT = "{'field':'value'}";

    @Nested
    class MissingRequiredProperties {

        @Test
        void shouldThrowNPE_whenContentIsNull() {
            assertThrows(
                NullPointerException.class,
                () -> new EmailAttachment(null, PDF_CONTENT_TYPE, PDF_FILE_NAME)
            );
        }

        @Test
        void shouldThrowNPE_whenContentTypeIsNull() {
            assertThrows(
                NullPointerException.class,
                () -> new EmailAttachment(CONTENT, null, PDF_FILE_NAME)
            );
        }

        @Test
        void shouldThrowNPE_whenFileNameIsNull() {
            assertThrows(
                NullPointerException.class,
                () -> new EmailAttachment(CONTENT, PDF_CONTENT_TYPE, null)
            );
        }
    }

    @Nested
    class ValidEmailAttachment {

        @Test
        void shouldCreateEmailAttachment_whenPdfAttachmentIsProvided() {
            assertThat(EmailAttachment.pdf(PDF_CONTENT, PDF_FILE_NAME).getContentType())
                .isEqualTo(PDF_CONTENT_TYPE);
        }

        @Test
        void shouldCreateEmailAttachment_whenJsonAttachmentIsProvided() {
            assertThat(EmailAttachment.json(JSON_CONTENT.getBytes(), JSON_FILE_NAME).getContentType())
                .isEqualTo(JSON_CONTENT_TYPE);
        }
    }
}

package uk.gov.hmcts.reform.civil.sendgrid;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import static java.util.Collections.unmodifiableList;

@EqualsAndHashCode
@Getter
public class EmailData {

    private final String to;
    private final String subject;
    private final String message;
    private final List<EmailAttachment> attachments;

    @Builder
    public EmailData(
        String to,
        String subject,
        String message,
        List<EmailAttachment> attachments
    ) {
        this.to = to;
        this.subject = subject;
        this.message = message;
        this.attachments = attachments != null ? unmodifiableList(attachments) : List.of();
    }

    public boolean hasAttachments() {
        return this.attachments != null && !this.attachments.isEmpty();
    }
}

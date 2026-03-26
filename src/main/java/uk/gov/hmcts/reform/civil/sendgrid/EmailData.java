package uk.gov.hmcts.reform.civil.sendgrid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

import static java.util.Collections.unmodifiableList;

@EqualsAndHashCode
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class EmailData {

    private String to;
    private String subject;
    private String message;
    private List<EmailAttachment> attachments = List.of();

    public EmailData setTo(String to) {
        this.to = to;
        return this;
    }

    public EmailData setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public EmailData setMessage(String message) {
        this.message = message;
        return this;
    }

    public EmailData setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments != null ? unmodifiableList(attachments) : List.of();
        return this;
    }

    public boolean hasAttachments() {
        return this.attachments != null && !this.attachments.isEmpty();
    }
}

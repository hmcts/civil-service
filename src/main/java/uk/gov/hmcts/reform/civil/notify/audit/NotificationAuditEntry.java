package uk.gov.hmcts.reform.civil.notify.audit;

import java.time.Instant;

public record NotificationAuditEntry(
    String templateId,
    String recipientEmail,
    String reference,
    String notificationId,
    Instant sentAt
) {
}

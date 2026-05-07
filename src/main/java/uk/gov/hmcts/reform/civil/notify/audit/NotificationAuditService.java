package uk.gov.hmcts.reform.civil.notify.audit;

import java.util.List;

public interface NotificationAuditService {

    void record(String templateId, String recipientEmail, String reference, String notificationId);

    List<NotificationAuditEntry> query(String referenceContains);

    void clear();
}

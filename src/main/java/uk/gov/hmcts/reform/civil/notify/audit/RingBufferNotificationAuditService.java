package uk.gov.hmcts.reform.civil.notify.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Service
@ConditionalOnExpression("${testing.support.enabled:false}")
public class RingBufferNotificationAuditService implements NotificationAuditService {

    static final int MAX_ENTRIES = 10_000;

    private final Deque<NotificationAuditEntry> buffer = new ConcurrentLinkedDeque<>();

    @Override
    public void record(String templateId, String recipientEmail, String reference, String notificationId) {
        NotificationAuditEntry entry = new NotificationAuditEntry(
            templateId,
            recipientEmail,
            reference,
            notificationId,
            Instant.now()
        );
        buffer.addLast(entry);
        while (buffer.size() > MAX_ENTRIES) {
            buffer.pollFirst();
        }
    }

    @Override
    public List<NotificationAuditEntry> query(String referenceContains) {
        if (referenceContains == null || referenceContains.isBlank()) {
            return Collections.unmodifiableList(new ArrayList<>(buffer));
        }
        return buffer.stream()
            .filter(entry -> entry.reference() != null && entry.reference().contains(referenceContains))
            .toList();
    }

    @Override
    public void clear() {
        buffer.clear();
    }
}

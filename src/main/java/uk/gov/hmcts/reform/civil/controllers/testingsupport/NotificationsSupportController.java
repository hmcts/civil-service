package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.notify.audit.NotificationAuditEntry;
import uk.gov.hmcts.reform.civil.notify.audit.NotificationAuditService;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.List;

@Tag(name = "Notifications Support Controller")
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnExpression("${testing.support.enabled:false}")
public class NotificationsSupportController {

    private final NotificationAuditService notificationAuditService;
    private final UserService userService;

    @GetMapping(
        value = "/testing-support/notifications/sent",
        produces = "application/json")
    public ResponseEntity<List<NotificationAuditEntry>> getSentNotifications(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam(value = "caseId") String caseId,
        @RequestParam(value = "templateId", required = false) String templateId,
        @RequestParam(value = "recipientEmail", required = false) String recipientEmail
    ) {
        userService.getUserInfo(authorisation);

        if (caseId == null || caseId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Querying sent notifications for caseId: {}", caseId);

        List<NotificationAuditEntry> entries = notificationAuditService.query(caseId);

        if (templateId != null && !templateId.isBlank()) {
            entries = entries.stream()
                .filter(entry -> templateId.equals(entry.templateId()))
                .toList();
        }

        if (recipientEmail != null && !recipientEmail.isBlank()) {
            entries = entries.stream()
                .filter(entry -> recipientEmail.equals(entry.recipientEmail()))
                .toList();
        }

        return ResponseEntity.ok(entries);
    }
}

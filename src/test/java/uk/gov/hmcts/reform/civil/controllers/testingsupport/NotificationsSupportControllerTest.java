package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.notify.audit.NotificationAuditEntry;
import uk.gov.hmcts.reform.civil.notify.audit.NotificationAuditService;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationsSupportControllerTest {

    private static final String CASE_ID = "001MC123";

    @Mock
    private NotificationAuditService notificationAuditService;

    private NotificationsSupportController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationsSupportController(notificationAuditService);
    }

    @Test
    void shouldReturnAllEntriesForCaseId_whenNoFiltersProvided() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));

        ResponseEntity<List<NotificationAuditEntry>> response = controller.getSentNotifications(CASE_ID, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(first, second);
    }

    @Test
    void shouldFilterByTemplateId_whenTemplateIdProvided() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(CASE_ID, "template-2", null);

        assertThat(response.getBody()).containsExactly(second);
    }

    @Test
    void shouldFilterByRecipientEmail_whenRecipientEmailProvided() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(CASE_ID, null, "defendant@email.com");

        assertThat(response.getBody()).containsExactly(second);
    }

    @Test
    void shouldApplyBothFilters_whenTemplateIdAndRecipientEmailProvided() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-1", "defendant@email.com", "received-001MC123", "notif-2");
        NotificationAuditEntry third = entry("template-2", "defendant@email.com", "received-001MC123", "notif-3");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second, third));

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(CASE_ID, "template-1", "defendant@email.com");

        assertThat(response.getBody()).containsExactly(second);
    }

    @Test
    void shouldReturnEmptyList_whenNoEntriesMatchCaseId() {
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of());

        ResponseEntity<List<NotificationAuditEntry>> response = controller.getSentNotifications(CASE_ID, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenFiltersExcludeAllEntries() {
        NotificationAuditEntry only = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(only));

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(CASE_ID, "template-99", null);

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldReturnBadRequest_whenCaseIdIsNull() {
        ResponseEntity<List<NotificationAuditEntry>> response = controller.getSentNotifications(null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(notificationAuditService, never()).query(any());
    }

    @Test
    void shouldReturnBadRequest_whenCaseIdIsEmptyString() {
        ResponseEntity<List<NotificationAuditEntry>> response = controller.getSentNotifications("", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(notificationAuditService, never()).query(any());
    }

    @Test
    void shouldReturnBadRequest_whenCaseIdIsWhitespaceOnly() {
        ResponseEntity<List<NotificationAuditEntry>> response = controller.getSentNotifications("   ", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(notificationAuditService, never()).query(any());
    }

    @Test
    void shouldIgnoreBlankTemplateIdFilter_andReturnAllEntries() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(CASE_ID, "  ", null);

        assertThat(response.getBody()).containsExactly(first, second);
    }

    @Test
    void shouldIgnoreBlankRecipientEmailFilter_andReturnAllEntries() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(CASE_ID, null, "");

        assertThat(response.getBody()).containsExactly(first, second);
    }

    private NotificationAuditEntry entry(String templateId, String recipient, String reference, String notificationId) {
        return new NotificationAuditEntry(templateId, recipient, reference, notificationId, Instant.now());
    }
}

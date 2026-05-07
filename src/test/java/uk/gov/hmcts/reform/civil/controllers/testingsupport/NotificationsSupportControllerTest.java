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
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationsSupportControllerTest {

    private static final String CASE_ID = "001MC123";
    private static final String AUTH_TOKEN = "Bearer test-token";

    @Mock
    private NotificationAuditService notificationAuditService;

    @Mock
    private UserService userService;

    private NotificationsSupportController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationsSupportController(notificationAuditService, userService);
    }

    @Test
    void shouldReturnAllEntriesForCaseId_whenNoFiltersProvided() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, CASE_ID, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(first, second);
    }

    @Test
    void shouldFilterByTemplateId_whenTemplateIdProvided() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, CASE_ID, "template-2", null);

        assertThat(response.getBody()).containsExactly(second);
    }

    @Test
    void shouldFilterByRecipientEmail_whenRecipientEmailProvided() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, CASE_ID, null, "defendant@email.com");

        assertThat(response.getBody()).containsExactly(second);
    }

    @Test
    void shouldApplyBothFilters_whenTemplateIdAndRecipientEmailProvided() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-1", "defendant@email.com", "received-001MC123", "notif-2");
        NotificationAuditEntry third = entry("template-2", "defendant@email.com", "received-001MC123", "notif-3");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second, third));
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, CASE_ID, "template-1", "defendant@email.com");

        assertThat(response.getBody()).containsExactly(second);
    }

    @Test
    void shouldReturnEmptyList_whenNoEntriesMatchCaseId() {
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of());
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, CASE_ID, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenFiltersExcludeAllEntries() {
        NotificationAuditEntry only = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(only));
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, CASE_ID, "template-99", null);

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldReturnBadRequest_whenCaseIdIsNull() {
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(notificationAuditService, never()).query(any());
    }

    @Test
    void shouldReturnBadRequest_whenCaseIdIsEmptyString() {
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, "", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(notificationAuditService, never()).query(any());
    }

    @Test
    void shouldReturnBadRequest_whenCaseIdIsWhitespaceOnly() {
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, "   ", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(notificationAuditService, never()).query(any());
    }

    @Test
    void shouldIgnoreBlankTemplateIdFilter_andReturnAllEntries() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, CASE_ID, "  ", null);

        assertThat(response.getBody()).containsExactly(first, second);
    }

    @Test
    void shouldIgnoreBlankRecipientEmailFilter_andReturnAllEntries() {
        NotificationAuditEntry first = entry("template-1", "claimant@email.com", "received-001MC123", "notif-1");
        NotificationAuditEntry second = entry("template-2", "defendant@email.com", "received-001MC123", "notif-2");
        when(notificationAuditService.query(CASE_ID)).thenReturn(List.of(first, second));
        when(userService.getUserInfo(AUTH_TOKEN)).thenReturn(UserInfo.builder().uid("user-1").build());

        ResponseEntity<List<NotificationAuditEntry>> response =
            controller.getSentNotifications(AUTH_TOKEN, CASE_ID, null, "");

        assertThat(response.getBody()).containsExactly(first, second);
    }

    @Test
    void shouldRejectRequest_whenAuthorisationTokenInvalid() {
        when(userService.getUserInfo(anyString()))
            .thenThrow(new RuntimeException("invalid token"));

        assertThatThrownBy(() ->
            controller.getSentNotifications("Bearer bad-token", CASE_ID, null, null))
            .isInstanceOf(RuntimeException.class);

        verify(notificationAuditService, never()).query(any());
    }

    private NotificationAuditEntry entry(String templateId, String recipient, String reference, String notificationId) {
        return new NotificationAuditEntry(templateId, recipient, reference, notificationId, Instant.now());
    }
}

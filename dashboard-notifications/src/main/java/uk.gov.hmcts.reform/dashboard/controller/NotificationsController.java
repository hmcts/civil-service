package uk.gov.hmcts.reform.dashboard.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dashboard.model.Notification;
import uk.gov.hmcts.reform.dashboard.service.NotificationGetterService;

import java.net.http.HttpHeaders;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationsController {

    public static final String CUI_NOTIFICATIONS_REQUEST = "/notifications/{caseReference}";
    private NotificationGetterService notificationGetterService;

    @GetMapping(path = CUI_NOTIFICATIONS_REQUEST, produces = APPLICATION_JSON)
    @Operation(summary = "Citizen UI will call this API and will get the list of notifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications requested successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Notification> getDashboardNotifications(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @PathVariable("caseReference") String caseReference) {

        return new ResponseEntity<Notification>(
            notificationGetterService.getNotifications()
        );
    }
}

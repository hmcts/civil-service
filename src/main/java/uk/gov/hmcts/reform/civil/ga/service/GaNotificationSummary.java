package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class GaNotificationSummary {

    private final NotificationService notificationService;
    private final List<String> attempted = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public void send(String email, String template, Map<String, String> properties, String reference) {
        if (StringUtils.isBlank(email)) {
            return;
        }
        attempted.add(email + " : " + reference + " : " + template);
        try {
            notificationService.sendMail(email, template, properties, reference);
        } catch (Exception exception) {
            recordFailure(email, exception);
        }
    }

    public void recordFailure(String recipient, Exception exception) {
        errors.add("Failed to send email to " + recipient + " : "
            + StringUtils.defaultIfBlank(exception.getMessage(), exception.getClass().getSimpleName()));
    }

    public String build() {
        return "Attempted: " + String.join(" | ", attempted) + " || Errors: " + String.join(" | ", errors);
    }
}

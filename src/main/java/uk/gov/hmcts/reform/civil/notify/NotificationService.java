package uk.gov.hmcts.reform.civil.notify;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.notify.audit.NotificationAuditService;
import uk.gov.hmcts.reform.civil.utils.MaskHelper;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationClient notificationClient;
    private final Optional<NotificationAuditService> notificationAuditService;
    private final ValidateEmailService validateEmailService;

    public void sendMail(
        String targetEmail,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        String trimmedTargetEmail = StringUtils.trimToNull(targetEmail);
        if (trimmedTargetEmail == null || !validateEmailService.validate(trimmedTargetEmail).isEmpty()) {
            log.warn(
                "NotificationService::sendMail::skipping invalid recipient for reference: {}, email: {}",
                reference,
                MaskHelper.maskEmail(targetEmail)
            );
            return;
        }
        try {
            log.info("NotificationService::sendMail::templateID: {}", emailTemplate);
            SendEmailResponse sendEmailResponse = notificationClient.sendEmail(
                emailTemplate,
                trimmedTargetEmail,
                parameters,
                reference
            );
            String notificationId = getNotificationId(sendEmailResponse);
            log.info("NotificationService::sendMail::successful for reference: {}, notificationID: {}",
                     reference,
                     notificationId
            );
            recordAudit(emailTemplate, trimmedTargetEmail, reference, notificationId);
        } catch (NotificationClientException e) {
            log.error("NotificationService::sendMail::error for reference: {}, message: {}", reference, e.getMessage());
            throw new NotificationException(e);
        }
    }

    public void sendLetter(String letterTemplate, Map<String, ?> personalisation, String reference) {
        try {
            notificationClient.sendLetter(letterTemplate, personalisation, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    private void recordAudit(String emailTemplate, String targetEmail, String reference, String notificationId) {
        notificationAuditService.ifPresent(audit -> {
            try {
                audit.record(emailTemplate, targetEmail, reference, notificationId);
            } catch (RuntimeException e) {
                log.warn("NotificationService::sendMail::audit failed for reference: {}, message: {}",
                         reference, e.getMessage());
            }
        });
    }

    private @NonNull String getNotificationId(SendEmailResponse sendEmailResponse) {
        return Optional.ofNullable(sendEmailResponse)
            .map(SendEmailResponse::getNotificationId)
            .map(Object::toString)
            .orElse("NOTSET");
    }
}

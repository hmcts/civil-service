package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@AllArgsConstructor
public abstract class Notifier implements NotificationData {

    protected final NotificationService notificationService;
    protected final NotificationsProperties notificationsProperties;
    protected final OrganisationService organisationService;
    protected final SimpleStateFlowEngine stateFlowEngine;
    protected final CaseTaskTrackingService caseTaskTrackingService;

    protected abstract String getTaskId();

    protected abstract Set<EmailDTO> getPartiesToNotify(final CaseData caseData);

    public void notifyParties(CaseData caseData, String eventId, String taskId) {
        final Set<EmailDTO> partiesToEmail = getPartiesToNotify(caseData);
        Map<String, String> commonProps = getCommonEmailProperties(caseData);
        final List<String> errors = sendNotification(partiesToEmail, commonProps);
        if (!errors.isEmpty()) {
            final HashMap<String, String> additionalProperties = new HashMap<>();
            additionalProperties.put("Errors", errors.toString());
            trackErrors(caseData.getCcdCaseReference(), eventId, taskId, additionalProperties);
        }
    }

    private Map<String, String> buildEmailProperties(Map<String, String> templateSpecificProps,
                                                     Map<String, String> commonEmailProperties) {
        Map<String, String> allEmailProperties = new HashMap<>(commonEmailProperties);
        allEmailProperties.putAll(templateSpecificProps);
        return allEmailProperties;
    }

    private Map<String, String> getCommonEmailProperties(CaseData caseData) {
        return Map.of(
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private List<String> sendNotification(Set<EmailDTO> recipients, Map<String, String> commonEmailProperties) {
        List<String> errorMessages = new ArrayList<>();
        for (EmailDTO recipient : recipients) {
            try {
                notificationService.sendMail(
                    recipient.getTargetEmail(), recipient.getEmailTemplate(),
                    Collections.unmodifiableMap(
                        buildEmailProperties(recipient.getParameters(), commonEmailProperties)
                    ),
                    recipient.getReference()
                );
            } catch (NotificationException e) {
                errorMessages.add(e.getMessage());
            }
        }
        return errorMessages;
    }

    private void trackErrors(final Long caseId,
                             final String eventId,
                             final String taskId,
                             final Map<String, String> additionalProperties) {
        caseTaskTrackingService.trackCaseTask(
            caseId.toString(),
            eventId,
            taskId,
            additionalProperties
        );
    }
}

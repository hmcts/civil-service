package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NOTIFY_BOTH;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
@AllArgsConstructor
public class NotifyClaimHelper {

    protected static final String NOTIFY_REFERENCE_TEMPLATE = "notify-claim-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public String getNotifyClaimEmailTemplate() {
        return notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate();
    }

    public Map<String, String> retrieveCustomProperties(CaseData caseData) {
        return Map.of(
            CLAIM_DETAILS_NOTIFICATION_DEADLINE,
            formatLocalDate(caseData
                                .getClaimDetailsNotificationDeadline()
                                .toLocalDate(), DATE)
        );
    }

    protected boolean checkIfThisDefendantToBeNotified(final CaseData caseData, String defendantName) {
        String defendantNotifyClaimInfo = Optional.ofNullable(caseData.getDefendantSolicitorNotifyClaimOptions())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse("");
        return defendantNotifyClaimInfo.contains(defendantName) || NOTIFY_BOTH.equals(defendantNotifyClaimInfo);
    }
}

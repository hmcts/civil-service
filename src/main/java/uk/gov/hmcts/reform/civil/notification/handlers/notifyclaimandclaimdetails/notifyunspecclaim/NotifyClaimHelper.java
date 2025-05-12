package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.BOTH_DEFENDANTS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@AllArgsConstructor
public class NotifyClaimHelper {

    protected static final String REFERENCE_TEMPLATE = "notify-claim-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public String getEmailTemplate() {
        return notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate();
    }

    public Map<String, String> getCustomProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIM_DETAILS_NOTIFICATION_DEADLINE,
            formatLocalDate(caseData
                                .getClaimDetailsNotificationDeadline()
                                .toLocalDate(), DATE)
        );
    }

    protected boolean checkIfThisDefendantToBeNotified(final CaseData caseData, String defendantName) {
        String defendantNotifyClaimInfo = Optional.ofNullable(caseData.getDefendantDetails())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse("");
        return defendantNotifyClaimInfo.equals(defendantName) || BOTH_DEFENDANTS.equals(defendantNotifyClaimInfo);
    }
}

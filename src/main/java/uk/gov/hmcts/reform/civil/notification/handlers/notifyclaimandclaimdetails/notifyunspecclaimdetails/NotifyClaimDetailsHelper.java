package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.BOTH_DEFENDANTS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE_PLUS_28;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
@AllArgsConstructor
public class NotifyClaimDetailsHelper {

    protected static final String REFERENCE_TEMPLATE = "notify-claim-details-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final DeadlinesCalculator deadlinesCalculator;

    public String getEmailTemplate() {
        return notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate();
    }

    public Map<String, String> getCustomProperties(CaseData caseData) {
        return Map.of(
            RESPONSE_DEADLINE, formatLocalDate(caseData
                                                   .getRespondent1ResponseDeadline()
                                                   .toLocalDate(), DATE),
            RESPONSE_DEADLINE_PLUS_28,
            formatLocalDate(deadlinesCalculator.plus14DaysDeadline(caseData.getRespondent1ResponseDeadline())
                                .toLocalDate(), DATE)
        );
    }

    protected boolean checkIfThisDefendantToBeNotified(final CaseData caseData, String defendantName) {
        String defendantNotifyClaimDetails = Optional.ofNullable(caseData.getDefendantSolicitorNotifyClaimDetailsOptions())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse("");

        return defendantNotifyClaimDetails.equals(defendantName) || BOTH_DEFENDANTS.equals(defendantNotifyClaimDetails);
    }
}

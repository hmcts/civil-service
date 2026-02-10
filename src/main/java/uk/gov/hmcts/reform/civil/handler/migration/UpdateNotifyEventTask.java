package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.NotificationCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierFactory;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;

@Component
public class UpdateNotifyEventTask extends MigrationTask<NotificationCaseReference> {

    private final NotifierFactory notifierFactory;

    public UpdateNotifyEventTask(NotifierFactory notifierFactory) {
        super(NotificationCaseReference.class);
        this.notifierFactory = notifierFactory;
    }

    @Override
    protected String getEventSummary() {
        return "Run notify event via migration task";
    }

    protected CaseData migrateCaseData(CaseData caseData, NotificationCaseReference caseReference) {
        if (caseData == null || caseReference == null) {
            throw new IllegalArgumentException("CaseData and CaseReference must not be null");
        }

        if (caseReference.getCamundaProcessIdentifier() == null)  {
            throw new IllegalArgumentException("CaseReference camundaProcessIdentifier must not be null");
        }

        final Notifier notifier = notifierFactory.getNotifier(caseReference.getCamundaProcessIdentifier());
        final String summary = notifier.notifyParties(caseData, NOTIFY_EVENT.toString(), caseReference.getCamundaProcessIdentifier());

        caseData.setNotificationSummary(summary);
        return caseData;
    }

    @Override
    protected String getEventDescription() {
        return "This task sends failed notifications on the case";
    }

    @Override
    protected String getTaskName() {
        return "UpdateNotifyEventTask";
    }
}

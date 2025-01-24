package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public abstract class NotificationHandler {

    protected abstract void sendNotification(Set<EmailDTO> recipients);

    protected abstract void notifyParties(CaseData caseData);

    protected abstract Map<String, String> addProperties(CaseData caseData);
}

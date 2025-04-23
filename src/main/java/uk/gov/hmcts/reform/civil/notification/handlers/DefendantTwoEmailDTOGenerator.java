package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

public abstract class DefendantTwoEmailDTOGenerator extends EmailDTOGenerator {

    protected DefendantTwoEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        super(notificationsProperties);
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.getRespondent2PartyEmail();
    }
}

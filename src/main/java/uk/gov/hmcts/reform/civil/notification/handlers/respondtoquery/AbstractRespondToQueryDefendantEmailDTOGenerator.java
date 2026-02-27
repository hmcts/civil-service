package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

public abstract class AbstractRespondToQueryDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    protected final NotificationsProperties notificationsProperties;
    protected final RespondToQueryHelper respondToQueryHelper;

    protected AbstractRespondToQueryDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                               RespondToQueryHelper respondToQueryHelper) {
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryHelper = respondToQueryHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getQueryLipWelshPublicResponseReceived()
            : notificationsProperties.getQueryLipPublicResponseReceived();
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return respondToQueryHelper.shouldNotifyLipDefendant(caseData);
    }
}

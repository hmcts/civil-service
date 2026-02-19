package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

public abstract class AbstractRespondToQueryClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    protected final NotificationsProperties notificationsProperties;
    protected final RespondToQueryHelper respondToQueryHelper;

    protected AbstractRespondToQueryClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                              RespondToQueryHelper respondToQueryHelper) {
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryHelper = respondToQueryHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getQueryLipWelshPublicResponseReceived()
            : notificationsProperties.getQueryLipPublicResponseReceived();
    }
}

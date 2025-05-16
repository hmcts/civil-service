package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

public abstract class AbstractNotifyManageStayClaimantHandler extends AbstractNotifyManageStayHandler {

    public AbstractNotifyManageStayClaimantHandler(NotificationService notificationService, NotificationsProperties notificationsProperties,
                                                   NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService) {
        super(notificationService, notificationsProperties, configuration, featureToggleService);
    }

    @Override
    protected String getRecipient(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return caseData.isApplicantLiP()
            ? caseData.getClaimantUserDetails().getEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    @Override
    protected boolean isLiP(CaseData caseData) {
        return caseData.isApplicantLiP();
    }

    @Override
    protected String getPartyName(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return caseData.getApplicant1().getPartyName();
    }

    @Override
    protected boolean isBilingual(CaseData caseData) {
        return caseData.isClaimantBilingual();
    }
}

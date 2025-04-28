package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

public abstract class CaseHandledOfflineApplicantSolicitorNotifier implements NotificationData {

    protected static final String REFERENCE_TEMPLATE = "defendant-response-case-handed-offline-applicant-notification-%s";
    private final NotificationService notificationService;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    protected CaseHandledOfflineApplicantSolicitorNotifier(NotificationService notificationService, OrganisationService organisationService,
                                                           NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService) {
        this.notificationService = notificationService;
        this.organisationService = organisationService;
        this.configuration = configuration;
        this.featureToggleService = featureToggleService;
    }

    protected void sendNotificationToSolicitor(CaseData caseData, String recipient, String templateID) {
        notificationService.sendMail(
            recipient,
            templateID,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return NotificationUtils.caseOfflineNotificationAddProperties(caseData,
                                                                      caseData.getApplicant1OrganisationPolicy(), organisationService,
                                                                      featureToggleService.isQueryManagementLRsEnabled(), configuration);
    }

    public abstract void notifyApplicantSolicitorForCaseHandedOffline(CaseData caseData);

    public FeatureToggleService getFeatureToggleService() {
        return featureToggleService;
    }

    public NotificationsSignatureConfiguration getNotificationsSignatureConfiguration() {
        return configuration;
    }
}

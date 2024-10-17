package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

public abstract class CaseHandledOfflineApplicantSolicitorNotifier implements NotificationData {

    protected static final String REFERENCE_TEMPLATE = "defendant-response-case-handed-offline-applicant-notification-%s";
    private final NotificationService notificationService;
    private final OrganisationService organisationService;

    protected CaseHandledOfflineApplicantSolicitorNotifier(NotificationService notificationService, OrganisationService organisationService) {
        this.notificationService = notificationService;
        this.organisationService = organisationService;
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
                                                                      caseData.getApplicant1OrganisationPolicy(), organisationService);
    }

    public abstract void notifyApplicantSolicitorForCaseHandedOffline(CaseData caseData);
}

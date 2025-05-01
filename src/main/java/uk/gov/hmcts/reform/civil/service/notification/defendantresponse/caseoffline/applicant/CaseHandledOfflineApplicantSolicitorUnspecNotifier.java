package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

@Component
public class CaseHandledOfflineApplicantSolicitorUnspecNotifier extends CaseHandledOfflineApplicantSolicitorNotifier {

    private final NotificationsProperties notificationsProperties;

    public CaseHandledOfflineApplicantSolicitorUnspecNotifier(NotificationService notificationService,
                                                              NotificationsProperties notificationsProperties,
                                                              OrganisationService organisationService) {
        super(notificationService, organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyApplicantSolicitorForCaseHandedOffline(CaseData caseData) {
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
        String templateID;

        if (is1v1Or2v1Case(caseData)) {
            templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
        } else {
            templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();

        }
        sendNotificationToSolicitor(caseData, recipient, templateID);
    }
}

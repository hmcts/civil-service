package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

@Component
public class ApplicantSolicitorUnspecCaseHandledOfflineNotifier extends CaseHandedOfflineApplicantNotifierBase {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public ApplicantSolicitorUnspecCaseHandledOfflineNotifier(NotificationService notificationService,
                                                              NotificationsProperties notificationsProperties) {
        super(notificationService);
        this.notificationService = notificationService;
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

package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class FullDefenceApplicantSolicitorOneUnspecNotifier extends FullDefenceSolicitorUnspecNotifier {

    @Autowired
    public FullDefenceApplicantSolicitorOneUnspecNotifier(NotificationService notificationService, NotificationsProperties notificationsProperties) {
        super(notificationService, notificationsProperties);
    }

    @Override
    protected String getRecipient(CaseData caseData) {
        YesOrNo applicant1Represented = caseData.getApplicant1Represented();
        return NO.equals(applicant1Represented) ? caseData.getApplicant1().getPartyEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

}

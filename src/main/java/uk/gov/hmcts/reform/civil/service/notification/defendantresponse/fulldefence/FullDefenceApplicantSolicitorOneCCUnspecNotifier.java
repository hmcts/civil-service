package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Component
public class FullDefenceApplicantSolicitorOneCCUnspecNotifier extends FullDefenceSolicitorUnspecNotifier {

    //NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC

    @Autowired
    public FullDefenceApplicantSolicitorOneCCUnspecNotifier(NotificationService notificationService,
                                                            NotificationsProperties notificationsProperties,
                                                            FeatureToggleService featureToggleService,
                                                            NotificationsSignatureConfiguration configuration) {
        super(notificationService, notificationsProperties, configuration, featureToggleService);
    }

    @Override
    protected String getRecipient(CaseData caseData) {
        return caseData.getRespondentSolicitor1EmailAddress();
    }

}

package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Optional;

@Component
public class FullDefenceRespondentSolicitorOneCCUnspecNotifier extends FullDefenceSolicitorUnspecNotifier {

    @Autowired
    public FullDefenceRespondentSolicitorOneCCUnspecNotifier(NotificationService notificationService,
                                                             NotificationsProperties notificationsProperties,
                                                             FeatureToggleService featureToggleService,
                                                             NotificationsSignatureConfiguration configuration) {
        super(notificationService, notificationsProperties, configuration, featureToggleService);
    }

    @Override
    protected String getRecipient(CaseData caseData) {

        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            var emailAddress = Optional.ofNullable(caseData.getRespondentSolicitor1EmailAddress());
            return emailAddress.orElse(null);
        } else {
            var emailAddress = Optional.ofNullable(caseData.getRespondentSolicitor2EmailAddress());
            return emailAddress.orElse(null);
        }
    }
}

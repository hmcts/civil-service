package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

/**
 * When an SDO is created it is notified to applicants and defendants.
 * This class sends the email to an unrepresented second defendant.
 */
@Component
public class CreateSDORespondent2LiPNotificationSender extends AbstractCreateSDORespondentNotificationSender {

    private static final String REFERENCE_TEMPLATE = "create-sdo-respondent-2-notification-%s";

    public CreateSDORespondent2LiPNotificationSender(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        NotificationsSignatureConfiguration configuration,
        FeatureToggleService featureToggleService) {
        super(notificationService, notificationsProperties, configuration, featureToggleService);
    }

    @Override
    protected String getDocReference(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    @Override
    protected String getRecipientEmail(CaseData caseData) {
        return caseData.getRespondent2().getPartyEmail();
    }

    @Override
    protected String getRespondentLegalName(CaseData caseData) {
        return caseData.getRespondent2().getPartyName();
    }
}

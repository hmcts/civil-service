package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

/**
 * When an SDO is created it is notified to applicants and defendants.
 * This class sends the email to a represented first defendant.
 */
@Component
public class CreateSDORespondent1LRNotificationSender extends AbstractCreateSDORespondentLRNotificationSender {

    private static final String REFERENCE_TEMPLATE = "create-sdo-respondent-1-notification-%s";

    public CreateSDORespondent1LRNotificationSender(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService,
        FeatureToggleService featureToggleService) {
        super(notificationService, notificationsProperties, organisationService, featureToggleService);
    }

    @Override
    protected String getDocReference(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    @Override
    protected String getRecipientEmail(CaseData caseData) {
        return caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected String getLROrganisationId(CaseData caseData) {
        return caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
    }
}

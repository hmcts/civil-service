package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

@Component
public abstract class AbstractCreateSDORespondentLRNotificationSender extends AbstractCreateSDORespondentNotificationSender {

    private final OrganisationService organisationService;

    public AbstractCreateSDORespondentLRNotificationSender(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService,
        FeatureToggleService featureToggleService) {
        super(notificationService, notificationsProperties, featureToggleService);
        this.organisationService = organisationService;
    }

    protected abstract String getLROrganisationId(CaseData caseData);

    @Override
    protected String getRespondentLegalName(CaseData caseData) {
        String id = getLROrganisationId(caseData);
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }
}

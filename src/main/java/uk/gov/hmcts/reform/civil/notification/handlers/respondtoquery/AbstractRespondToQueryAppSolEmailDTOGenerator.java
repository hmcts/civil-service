package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

public abstract class AbstractRespondToQueryAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected final NotificationsProperties notificationsProperties;
    protected final RespondToQueryHelper respondToQueryHelper;

    protected AbstractRespondToQueryAppSolEmailDTOGenerator(OrganisationService organisationService,
                                                           NotificationsProperties notificationsProperties,
                                                           RespondToQueryHelper respondToQueryHelper) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryHelper = respondToQueryHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getQueryLrPublicResponseReceived();
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return respondToQueryHelper.shouldNotifyApplicantSolicitor(caseData);
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.raisequery.otherpartyqueryraised;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

public class OtherPartyQueryRaisedRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    private static final String REFERENCE_TEMPLATE = "a-query-has-been-raised-notification-%s";

    public OtherPartyQueryRaisedRespSolOneEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyOtherPartyPublicQueryRaised();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}

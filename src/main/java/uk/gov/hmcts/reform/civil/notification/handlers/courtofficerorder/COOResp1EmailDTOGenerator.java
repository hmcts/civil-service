package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class COOResp1EmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String COURT_OFFICE_ORDER_REFERENCE_TEMPLATE = "generate-order-notification-%s";

    NotificationsProperties notificationsProperties;

    public COOResp1EmailDTOGenerator(OrganisationService organisationService,
                                      NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyLipUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return COURT_OFFICE_ORDER_REFERENCE_TEMPLATE;
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class GenerateOrderCOOResp2EmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    protected static final String COO_RESP_SOL_TWO_REFERENCE_TEMPLATE = "generate-order-notification-%s";

    NotificationsProperties notificationsProperties;

    public GenerateOrderCOOResp2EmailDTOGenerator(OrganisationService organisationService,
                                                  NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getGenerateOrderNotificationTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return COO_RESP_SOL_TWO_REFERENCE_TEMPLATE;
    }
}

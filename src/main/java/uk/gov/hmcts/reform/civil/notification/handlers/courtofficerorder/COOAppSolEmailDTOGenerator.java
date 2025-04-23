package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class COOAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String COURT_OFFICE_ORDER_REFERENCE_TEMPLATE = "generate-order-notification-%s";

    NotificationsProperties notificationsProperties;

    public COOAppSolEmailDTOGenerator(OrganisationService organisationService,
                                      NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            if (caseData.isClaimantBilingual()) {
                return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            }
            return notificationsProperties.getNotifyLipUpdateTemplate();
        } else {
            return notificationsProperties.getGenerateOrderNotificationTemplate();
        }
    }

    @Override
    protected String getReferenceTemplate() {
        return COURT_OFFICE_ORDER_REFERENCE_TEMPLATE;
    }
}

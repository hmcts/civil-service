package uk.gov.hmcts.reform.civil.notification.handlers.settleclaimpaidinfullnotification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Component
public class SettleClaimPaidInFullNotificationRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "defendant-settle-claim-marked-paid-in-full-%s";

    private final NotificationsProperties notificationsProperties;

    protected SettleClaimPaidInFullNotificationRespSolTwoEmailDTOGenerator(
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifySettleClaimMarkedPaidInFullDefendantTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(LEGAL_ORG_NAME,
            getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService));
        return properties;
    }
}

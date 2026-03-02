package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class StandardDirectionOrderDJAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-claimant-%s";
    private static final String LEGAL_ORG_NAME = "legalOrgName";
    private static final String CLAIM_NUMBER = "claimReferenceNumber";

    public StandardDirectionOrderDJAppSolOneEmailDTOGenerator(
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getStandardDirectionOrderDJTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_SDO_DJ;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(CLAIM_NUMBER, caseData.getCcdCaseReference().toString());
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return !caseData.isApplicant1NotRepresented();
    }
}

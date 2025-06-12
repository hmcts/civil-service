package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class AcknowledgeClaimUnspecAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected static final String APP_SOL_REF_TEMPLATE = "acknowledge-claim-applicant-notification-%s";

    NotificationsProperties notificationsProperties;
    private final AcknowledgeClaimUnspecHelper acknowledgeClaimUnspecHelper;

    public AcknowledgeClaimUnspecAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                            OrganisationService organisationService,
                                                            AcknowledgeClaimUnspecHelper acknowledgeClaimUnspecHelper) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.acknowledgeClaimUnspecHelper = acknowledgeClaimUnspecHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        //Template is common for applicant and respondent
        return notificationsProperties.getRespondentSolicitorAcknowledgeClaim();
    }

    @Override
    protected String getReferenceTemplate() {
        return APP_SOL_REF_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties = acknowledgeClaimUnspecHelper.addTemplateProperties(properties, caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return properties;
    }
}

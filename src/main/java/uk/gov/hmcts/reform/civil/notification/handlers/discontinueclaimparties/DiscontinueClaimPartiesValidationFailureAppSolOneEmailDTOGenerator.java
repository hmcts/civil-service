package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class DiscontinueClaimPartiesValidationFailureAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE = "claimant-notify-validation-failure-%s";

    public DiscontinueClaimPartiesValidationFailureAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                             OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyClaimantLrValidationDiscontinuanceFailureTemplate();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService));
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return Boolean.TRUE.equals(super.getShouldNotify(caseData))
            && !ConfirmOrderGivesPermission.YES.equals(caseData.getConfirmOrderGivesPermission());
    }
}

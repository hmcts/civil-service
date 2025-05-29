package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class NotifyClaimDetailsAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotifyClaimDetailsHelper notifyClaimDetailsHelper;

    protected NotifyClaimDetailsAppSolEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService,
                                                        NotifyClaimDetailsHelper notifyClaimDetailsHelper) {
        super(notificationsProperties, organisationService);
        this.notifyClaimDetailsHelper = notifyClaimDetailsHelper;
    }

    @Override
    protected String getReferenceTemplate() {
        return NotifyClaimDetailsHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.putAll(notifyClaimDetailsHelper.getCustomProperties(caseData));
        return properties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notifyClaimDetailsHelper.getEmailTemplate();
    }
}

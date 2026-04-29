package uk.gov.hmcts.reform.civil.notification.handlers.judgmentbyadmission;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class JudgmentByAdmissionAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_APP_SOL_ONE_TEMPLATE = "claimant-judgment-by-admission-%s";

    private final NotificationsProperties notificationsProperties;

    protected JudgmentByAdmissionAppSolOneEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyClaimantLRJudgmentByAdmissionTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_APP_SOL_ONE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(DEFENDANT_NAME,  getDefendantNameBasedOnCaseType(caseData));
        return properties;
    }
}

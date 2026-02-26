package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class GenerateSpecDJFormRequestedAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_REQUESTED = "default-judgment-applicant-requested-notification-%s";

    public GenerateSpecDJFormRequestedAppSolEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getApplicantSolicitor1DefaultJudgmentRequested();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_REQUESTED;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(LEGAL_ORG_APPLICANT1, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(CLAIM_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(DEFENDANT_NAME, getRequestedDefendantName(caseData));
        return properties;
    }

    private String getRequestedDefendantName(CaseData caseData) {
        if (caseData.getDefendantDetailsSpec() != null
            && caseData.getDefendantDetailsSpec().getValue() != null
            && caseData.getDefendantDetailsSpec().getValue().getLabel() != null) {
            return caseData.getDefendantDetailsSpec().getValue().getLabel();
        }
        return getPartyNameBasedOnType(caseData.getRespondent1());
    }
}

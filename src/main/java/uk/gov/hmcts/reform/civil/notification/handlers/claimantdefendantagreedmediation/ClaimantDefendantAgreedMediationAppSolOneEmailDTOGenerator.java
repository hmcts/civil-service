package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantDefendantAgreedMediationAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final FeatureToggleService featureToggleService;

    public ClaimantDefendantAgreedMediationAppSolOneEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties,
                                                                      FeatureToggleService featureToggleService) {
        super(notificationsProperties, organisationService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData)
            ? notificationsProperties.getNotifyApplicantLRMediationTemplate()
            : notificationsProperties.getNotifyApplicantLRMediationAgreementTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "mediation-agreement-applicant-notification-%s";
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        return properties;
    }
}

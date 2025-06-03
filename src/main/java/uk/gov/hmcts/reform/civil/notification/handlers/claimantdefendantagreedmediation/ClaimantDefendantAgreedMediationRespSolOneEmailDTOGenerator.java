package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantDefendantAgreedMediationRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final FeatureToggleService featureToggleService;

    public ClaimantDefendantAgreedMediationRespSolOneEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties,
                                                                       FeatureToggleService featureToggleService) {
        super(notificationsProperties, organisationService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData) ? notificationsProperties.getNotifyDefendantLRForMediation() :
            notificationsProperties.getNotifyRespondentLRMediationAgreementTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "mediation-agreement-respondent-notification-%s";
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData, true, organisationService));
        return properties;
    }
}

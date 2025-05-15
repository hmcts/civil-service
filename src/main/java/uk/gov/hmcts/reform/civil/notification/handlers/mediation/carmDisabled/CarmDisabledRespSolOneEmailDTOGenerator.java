package uk.gov.hmcts.reform.civil.notification.handlers.mediation.carmDisabled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class CarmDisabledRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "notification-mediation-successful-defendant-LIP-%s";

    private final NotificationsProperties notificationsProperties;

    public CarmDisabledRespSolOneEmailDTOGenerator(OrganisationService organisationService,
                                           NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyLrDefendantSuccessfulMediationForLipVLrClaim();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    //Only in case of lip v lr
    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.isLipvLROneVOne();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isResp1 = true;
        properties.putAll(Map.of(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                                                   isResp1, organisationService),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        ));
        return properties;
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.mediation.carmEnabled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isTwoVOne;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class CarmRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "notification-mediation-successful-defendant-LR-%s";
    private final NotificationsProperties notificationsProperties;

    public CarmRespSolOneEmailDTOGenerator(OrganisationService organisationService,
                                          NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (isTwoVOne(caseData)) {
            return notificationsProperties.getNotifyTwoVOneDefendantSuccessfulMediation();
        }
        return notificationsProperties.getNotifyLrDefendantSuccessfulMediation();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        if (isTwoVOne(caseData)) {
            properties.putAll(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                                 true, organisationService),
                CLAIMANT_NAME_ONE, caseData.getApplicant1().getPartyName(),
                CLAIMANT_NAME_TWO, caseData.getApplicant2().getPartyName()
            ));
        } else {
            properties.putAll(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                                 true, organisationService),
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
            ));
        }
        return properties;
    }
}

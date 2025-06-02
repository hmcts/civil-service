package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class GenerateDJFormRequestedRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_REQUEST_DEF = "interim-judgment-requested-notification-def-%s";

    public GenerateDJFormRequestedRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                              OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getInterimJudgmentRequestedDefendant();
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        //To cover 1v2 same solicitor or 1v2 lip lr journeys
        return Optional.ofNullable(caseData.getRespondentSolicitor2EmailAddress())
            .orElse(caseData.getRespondentSolicitor1EmailAddress());
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = false;
        properties.put(LEGAL_ORG_DEF, getLegalOrganizationNameForRespondent(caseData, isRespondent1, organisationService));
        properties.put(CLAIM_NUMBER_INTERIM, caseData.getCcdCaseReference().toString());
        properties.put(DEFENDANT_NAME_INTERIM, caseData.getRespondent2().getPartyName());
        return properties;
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_REQUEST_DEF;
    }
}

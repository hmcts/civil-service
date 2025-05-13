package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class GenerateDJFormApprovedRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_APPROVAL_DEF = "interim-judgment-approval-notification-def-%s";

    public GenerateDJFormApprovedRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                             OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getInterimJudgmentApprovalDefendant();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_APPROVAL_DEF;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = false;
        properties.put(LEGAL_ORG_DEF, getLegalOrganizationNameForRespondent(caseData, isRespondent1, organisationService));
        properties.put(CLAIM_NUMBER_INTERIM, caseData.getCcdCaseReference().toString());
        properties.put(DEFENDANT_NAME_INTERIM, caseData.getRespondent2().getPartyName());
        return properties;
    }
}

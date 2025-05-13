package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class GenerateDJFormRequestedAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private final GenerateDJFormHelper generateDJFormHelper;
    private static final String REFERENCE_TEMPLATE_REQUEST_CLAIMANT = "interim-judgment-requested-notification-%s";

    public GenerateDJFormRequestedAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                             OrganisationService organisationService,
                                                             GenerateDJFormHelper generateDJFormHelper) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.generateDJFormHelper = generateDJFormHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getInterimJudgmentRequestedClaimant();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_REQUEST_CLAIMANT;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(LEGAL_REP_CLAIMANT, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(CLAIM_NUMBER_INTERIM, caseData.getCcdCaseReference().toString());
        properties.put(DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName());
        if (isOneVTwoTwoLegalRep(caseData)
            && (generateDJFormHelper.checkDefendantRequested(caseData, false))) {
            properties.put(DEFENDANT_NAME_INTERIM, caseData.getRespondent2().getPartyName());
        }
        return properties;
    }
}

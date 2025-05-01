package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class GenerateDJFormAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_APPROVAL_CLAIMANT = "interim-judgment-approval-notification-%s";
    private static final String REFERENCE_TEMPLATE_REQUEST_CLAIMANT = "interim-judgment-requested-notification-%s";

    public GenerateDJFormAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                            OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        if (isOneVTwoTwoLegalRep(caseData)) {
            if (checkDefendantRequested(caseData, caseData.getRespondent1().getPartyName()) || checkDefendantRequested(caseData, caseData.getRespondent2().getPartyName())) {
                return notificationsProperties.getInterimJudgmentRequestedClaimant();
            }
        }
        return notificationsProperties.getInterimJudgmentApprovalClaimant();
    }

    @Override
    protected String getReferenceTemplate() {
        //Add rest of code from notes app here
        return REFERENCE_TEMPLATE_APPROVAL_CLAIMANT;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(LEGAL_REP_CLAIMANT, properties.get(CLAIM_LEGAL_ORG_NAME_SPEC));
        properties.put(CLAIM_NUMBER_INTERIM, caseData.getCcdCaseReference().toString());
        if (isOneVTwoTwoLegalRep(caseData)) {
            if (checkDefendantRequested(caseData, caseData.getRespondent1().getPartyName())) {
                properties.put(DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName());
            } else if (checkDefendantRequested(caseData, caseData.getRespondent2().getPartyName())) {
                properties.put(DEFENDANT_NAME_INTERIM, caseData.getRespondent2().getPartyName());
            }
        }
        return properties;
    }

    private boolean checkDefendantRequested(final CaseData caseData, String defendantName) {
        if (caseData.getDefendantDetails() != null) {
            return defendantName.equals(caseData.getDefendantDetails().getValue().getLabel());
        } else {
            return false;
        }
    }
}

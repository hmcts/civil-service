package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class GenerateDJFormRequestedRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_APPROVAL_DEF = "interim-judgment-approval-notification-def-%s";
    private static final String REFERENCE_TEMPLATE_REQUEST_DEF = "interim-judgment-requested-notification-def-%s";

    public GenerateDJFormRequestedRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                              OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        if (caseData.isRespondent1LiP() && YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            if (!caseData.isRespondent2LiP()) {
                return checkIfBothDefendants(caseData)
                    ? notificationsProperties.getInterimJudgmentApprovalDefendant()
                    : notificationsProperties.getInterimJudgmentRequestedDefendant();
            }
        }
        if (isOneVTwoTwoLegalRep(caseData)) {
            if (checkDefendantRequested(caseData, caseData.getRespondent1().getPartyName())
                || checkDefendantRequested(caseData, caseData.getRespondent2().getPartyName())) {
                return notificationsProperties.getInterimJudgmentRequestedDefendant();
            }
        }
        return notificationsProperties.getInterimJudgmentApprovalDefendant();
    }

    @Override
    protected String getReferenceTemplate(CaseData caseData) {
        if (caseData.isRespondent1LiP() && YesOrNo.YES.equals(caseData.getAddRespondent2())) {
            if (!caseData.isRespondent2LiP()) {
                return checkIfBothDefendants(caseData)
                    ? REFERENCE_TEMPLATE_APPROVAL_DEF
                    : REFERENCE_TEMPLATE_REQUEST_DEF;
            }
        }
        if (isOneVTwoTwoLegalRep(caseData)) {
            if (checkDefendantRequested(caseData, caseData.getRespondent1().getPartyName())
                || checkDefendantRequested(caseData, caseData.getRespondent2().getPartyName())) {
                return REFERENCE_TEMPLATE_REQUEST_DEF;
            }
        }
        return REFERENCE_TEMPLATE_APPROVAL_DEF;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(LEGAL_ORG_DEF, properties.get(CLAIM_LEGAL_ORG_NAME_SPEC));
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

    private Boolean checkIfBothDefendants(CaseData caseData) {
        return BOTH_DEFENDANTS.equals(caseData.getDefendantDetails().getValue().getLabel());
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class GenerateSpecDJFormRequestedRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_REQUESTED = "default-judgment-respondent-requested-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public GenerateSpecDJFormRequestedRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                                  NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitor1DefaultJudgmentRequested();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_REQUESTED;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(DEFENDANT_EMAIL, getLegalOrganizationNameForRespondent(caseData, false, organisationService));
        properties.put(CLAIM_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(DEFENDANT_NAME, getRequestedDefendantName(caseData));
        properties.put(CLAIMANT_EMAIL, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        return properties;
    }

    private String getRequestedDefendantName(CaseData caseData) {
        if (caseData.getDefendantDetailsSpec() != null
            && caseData.getDefendantDetailsSpec().getValue() != null
            && caseData.getDefendantDetailsSpec().getValue().getLabel() != null) {
            return caseData.getDefendantDetailsSpec().getValue().getLabel();
        }
        return caseData.getRespondent2() != null
            ? getPartyNameBasedOnType(caseData.getRespondent2())
            : getPartyNameBasedOnType(caseData.getRespondent1());
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class GenerateSpecDJFormReceivedDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_REQUESTED = "default-judgment-respondent-requested-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public GenerateSpecDJFormReceivedDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondent1DefaultJudgmentRequestedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_REQUESTED;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(CLAIM_NUMBER_INTERIM, caseData.getCcdCaseReference().toString());
        properties.put(DEFENDANT_NAME_INTERIM, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        return properties;
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
public class JudgmentVariedDeterminationOfMeansLipDefendantEmailDTOGenerator extends EmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "defendant-judgment-varied-determination-of-means-%s";

    private final NotificationsProperties notificationsProperties;

    public JudgmentVariedDeterminationOfMeansLipDefendantEmailDTOGenerator(
            NotificationsProperties notificationsProperties
    ) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return nonNull(caseData.getRespondent1().getPartyEmail())
                && YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getRespondent1().getPartyEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
                ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
                CLAIMANT_V_DEFENDANT,   getAllPartyNames(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                PARTY_NAME,             caseData.getRespondent1().getPartyName()
        );
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        return properties;
    }
}

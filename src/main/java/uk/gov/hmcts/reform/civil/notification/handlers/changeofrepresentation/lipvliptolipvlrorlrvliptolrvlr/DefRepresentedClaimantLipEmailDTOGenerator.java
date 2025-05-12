package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolipvlrorlrvliptolrvlr;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
@AllArgsConstructor
public class DefRepresentedClaimantLipEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE =
        "notify-claimant-lip-after-defendant-noc-approval-%s";

    private final NotificationsProperties notificationsProperties;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC()
            : notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        ));
        return properties;
    }
}

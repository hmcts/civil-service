package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class NotifyClaimantLipHelpWithFeesEmailDTOGenerator extends EmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    private static final String REFERENCE_TEMPLATE =
            "notify-claimant-lip-help-with-fees-notification-%s";

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getClaimantUserDetails().getEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
                ? notificationsProperties.getNotifyClaimantLipHelpWithFeesWelsh()
                : notificationsProperties.getNotifyClaimantLipHelpWithFees();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_NAME,        caseData.getApplicant1().getPartyName(),
                CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
        ));
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> properties, CaseData caseData
    ) {
        return properties;
    }
}

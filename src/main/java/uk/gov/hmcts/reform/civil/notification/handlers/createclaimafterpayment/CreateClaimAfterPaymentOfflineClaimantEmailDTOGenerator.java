package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
public class CreateClaimAfterPaymentOfflineClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "case-proceeds-in-caseman-applicant-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public CreateClaimAfterPaymentOfflineClaimantEmailDTOGenerator(
            NotificationsProperties notificationsProperties
    ) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
                ? notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()
                : notificationsProperties.getClaimantLipClaimUpdatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        return properties;
    }
}

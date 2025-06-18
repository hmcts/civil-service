package uk.gov.hmcts.reform.civil.notification.handlers.createlipclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ApplicantClaimSubmittedClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claim-submitted-notification-%s";

    private final PinInPostConfiguration pipInPostConfiguration;
    private final NotificationsProperties notificationsProperties;

    public ApplicantClaimSubmittedClaimantEmailDTOGenerator(
            PinInPostConfiguration pipInPostConfiguration,
            NotificationsProperties notificationsProperties
    ) {
        this.pipInPostConfiguration = pipInPostConfiguration;
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        boolean isWithHearingFee = caseData.getHelpWithFeesReferenceNumber() != null;

        if (caseData.isClaimantBilingual() && isWithHearingFee) {
            return notificationsProperties
                    .getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeBilingualTemplate();
        }
        if (caseData.isClaimantBilingual()) {
            return notificationsProperties
                    .getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeBilingualTemplate();
        }
        if (isWithHearingFee) {
            return notificationsProperties
                    .getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeTemplate();
        }
        return notificationsProperties
                .getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeTemplate();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl());
        return properties;
    }
}

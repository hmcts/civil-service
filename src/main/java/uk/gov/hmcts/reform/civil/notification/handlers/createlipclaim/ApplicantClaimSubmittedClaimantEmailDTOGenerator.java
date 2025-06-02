package uk.gov.hmcts.reform.civil.notification.handlers.createlipclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ApplicantClaimSubmittedClaimantEmailDTOGenerator extends EmailDTOGenerator {

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
    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.isLipvLipOneVOne()
                && caseData.getApplicant1Email() != null;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getApplicant1Email();
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
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl()
        );
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> properties,
            CaseData caseData
    ) {
        return properties;
    }
}

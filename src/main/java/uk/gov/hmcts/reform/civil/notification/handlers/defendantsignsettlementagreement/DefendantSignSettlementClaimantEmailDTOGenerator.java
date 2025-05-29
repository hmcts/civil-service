package uk.gov.hmcts.reform.civil.notification.handlers.defendantsignsettlementagreement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;
import java.util.Optional;

@Component
public class DefendantSignSettlementClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "notify-signed-settlement-%s";

    private final PinInPostConfiguration pipInPostConfiguration;

    protected DefendantSignSettlementClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties, PinInPostConfiguration pipInPostConfiguration) {
        super(notificationsProperties);
        this.pipInPostConfiguration = pipInPostConfiguration;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        Optional<CaseDataLiP> optionalCaseDataLiP = Optional.ofNullable(caseData.getCaseDataLiP());
        boolean isAgreed = optionalCaseDataLiP.map(CaseDataLiP::isDefendantSignedSettlementAgreement).orElse(false);

        if (caseData.isClaimantBilingual()) {
            return isAgreed ? notificationsProperties.getNotifyApplicantLipForSignedSettlementAgreementInBilingual() :
                notificationsProperties.getNotifyApplicantLipForNotAgreedSignSettlementInBilingual();
        }

        return isAgreed ? notificationsProperties.getNotifyApplicantForSignedSettlementAgreement() :
            notificationsProperties.getNotifyApplicantForNotAgreedSignSettlement();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        properties.put(RESPONDENT_NAME, caseData.getRespondent1().getPartyName());
        properties.put(FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl());
        return properties;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        Optional<CaseDataLiP> optionalCaseDataLiP = Optional.ofNullable(caseData.getCaseDataLiP());
        boolean isAgreed = optionalCaseDataLiP.map(CaseDataLiP::isDefendantSignedSettlementAgreement).orElse(false);
        boolean isNotAgreed = optionalCaseDataLiP.map(CaseDataLiP::isDefendantSignedSettlementNotAgreed).orElse(false);
        return (isAgreed || isNotAgreed) && caseData.isRespondent1LiP();
    }
}

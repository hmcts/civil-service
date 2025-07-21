package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@RequiredArgsConstructor
public class ClaimantConfirmProceedDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-respondent-notification-%s";
    public static final String NO_EMAIL_OPERATION = "No Email Operation";

    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isClaimantBilingual()
            && (!featureToggleService.isDefendantNoCOnlineForCase(caseData)
            || NO.equals(caseData.getApplicant1ProceedWithClaim())
            || caseData.isClaimantIntentionSettlePartAdmit())) {
            return NO_EMAIL_OPERATION;
        }


        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()
            : notificationsProperties.getRespondent1LipClaimUpdatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        return properties;
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmsnottoproceedlip;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantConfirmsNotToProceedLipDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    private final FeatureToggleService featureToggleService;

    protected ClaimantConfirmsNotToProceedLipDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties, FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isPartAdmitPayImmediatelyAccepted()) {
            return notificationsProperties.getNotifyRespondentLipPartAdmitPayImmediatelyAcceptedSpec();
        } else if (caseData.isClaimantDontWantToProceedWithFulLDefenceFD()) {
            return caseData.isRespondentResponseBilingual()
                ? notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()
                : notificationsProperties.getRespondent1LipClaimUpdatedTemplate();
        }

        return notificationsProperties.getClaimantSolicitorConfirmsNotToProceed();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-confirms-not-to-proceed-respondent-notification-%s";
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        if (caseData.isPartAdmitPayImmediatelyAccepted()
            || caseData.isClaimantDontWantToProceedWithFulLDefenceFD()) {
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
            properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        } else {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getRespondent1().getPartyName());
        }

        return properties;
    }
}

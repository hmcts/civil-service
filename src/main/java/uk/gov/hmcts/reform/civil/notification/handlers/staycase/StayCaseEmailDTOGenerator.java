package uk.gov.hmcts.reform.civil.notification.handlers.staycase;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@RequiredArgsConstructor
public abstract class StayCaseEmailDTOGenerator extends EmailDTOGenerator {

    protected final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        String email = getEmailAddress(caseData);
        return email != null && !email.isEmpty();
    }

    @Override
    public EmailDTO buildEmailDTO(CaseData caseData, String taskId) {
        EmailDTO email = super.buildEmailDTO(caseData, taskId);
        // The legacy stay notifications use the CCD reference, rather than the legacy case reference.
        email.setReference(String.format(getReferenceTemplate(), caseData.getCcdCaseReference()));
        return email;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (isLiP(caseData)) {
            return isBilingual(caseData) ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        }
        return notificationsProperties.getNotifyLRCaseStayed();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        Map<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, getPartyName(caseData),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        return properties;
    }

    protected abstract boolean isLiP(CaseData caseData);

    protected abstract boolean isBilingual(CaseData caseData);

    protected abstract String getPartyName(CaseData caseData);
}

package uk.gov.hmcts.reform.civil.notification.handlers.coscapplicationafterpayment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Component
@RequiredArgsConstructor
public class CoscApplicationAfterPaymentAppSolOneEmailDTOGenerator extends EmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        // The BPMN selects this notification only for a represented claimant with an unpaid judgment.
        return true;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyClaimantLRCoscApplied();
    }

    @Override
    protected String getReferenceTemplate() {
        return "cosc-application-email-notification-%s";
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        Map<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
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
}

package uk.gov.hmcts.reform.civil.notification.handlers.stayupdaterequested;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Component
@RequiredArgsConstructor
public class StayUpdateRequestedEmailHelper {

    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    public Map<String, String> addProperties(CaseData caseData, boolean lip) {
        Map<String, String> properties = new HashMap<>();
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        if (!lip) {
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        }
        addAllFooterItems(caseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    public String getTemplate(boolean lip, boolean bilingual) {
        if (lip) {
            return bilingual ? notificationsProperties.getNotifyLipBilingualStayUpdateRequested()
                : notificationsProperties.getNotifyLipStayUpdateRequested();
        }
        return notificationsProperties.getNotifyLRStayUpdateRequested();
    }

    public EmailDTO withCcdReference(EmailDTO email, String referenceTemplate, CaseData caseData) {
        email.setReference(String.format(referenceTemplate, caseData.getCcdCaseReference()));
        return email;
    }
}

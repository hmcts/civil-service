package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

@Component
@AllArgsConstructor
public class NoCLipVLRNewDefendantEmailDTOGenerator extends EmailDTOGenerator {

    private final FeatureToggleService featureToggleService;
    private final NotificationsProperties notificationsProperties;
    private final NoCHelper noCHelper;

    @Override
    public boolean getShouldNotify(CaseData caseData) {
        return featureToggleService.isDefendantNoCOnlineForCase(caseData)
            && noCHelper.isApplicantLipForRespondentSolicitorChange(caseData);
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyNewDefendantSolicitorNOC();
    }

    @Override
    protected String getReferenceTemplate() {
        return NoCHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(noCHelper.getProperties(caseData, false));
        return properties;
    }
}

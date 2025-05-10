package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlip;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
@AllArgsConstructor
public class DefendantLipEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyRespondentLipForClaimantRepresentedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return ClaimantLipNocHelper.REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(ClaimantLipNocHelper.getLipProperties(caseData));
        return properties;
    }
}

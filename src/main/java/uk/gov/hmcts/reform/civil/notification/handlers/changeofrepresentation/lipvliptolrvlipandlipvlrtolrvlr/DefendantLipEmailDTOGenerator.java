package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlipandlipvlrtolrvlr;

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
        return ClaimantLipRepresentedNocHelper.REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(ClaimantLipRepresentedNocHelper.getLipProperties(caseData));
        return properties;
    }
}

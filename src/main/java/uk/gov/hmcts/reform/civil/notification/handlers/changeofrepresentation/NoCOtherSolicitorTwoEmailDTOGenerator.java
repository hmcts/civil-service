package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
@AllArgsConstructor
public class NoCOtherSolicitorTwoEmailDTOGenerator extends EmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private final NoCHelper noCHelper;

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return !(isOneVTwoTwoLegalRep(caseData) || noCHelper.isOtherParty2Lip(caseData));
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNoticeOfChangeOtherParties();
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return noCHelper.getOtherSolicitor2Email(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return NoCHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(noCHelper.getProperties(caseData, true));
        return properties;
    }
}

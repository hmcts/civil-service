package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
@AllArgsConstructor
public class NoCClaimantLipEmailDTOGenerator extends EmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;
    private final NoCHelper noCHelper;

    @Override
    public boolean getShouldNotify(CaseData caseData) {
        return noCHelper.isApplicantLipForRespondentSolicitorChange(caseData);
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getApplicant1Email();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC()
            : notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return NoCHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(noCHelper.getClaimantLipProperties(caseData));
        return  properties;
    }
}

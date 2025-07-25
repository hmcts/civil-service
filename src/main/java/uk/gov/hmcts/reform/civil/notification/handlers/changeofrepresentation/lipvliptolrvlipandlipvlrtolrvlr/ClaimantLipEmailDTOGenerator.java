package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlipandlipvlrtolrvlr;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
@AllArgsConstructor
public class ClaimantLipEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getNotifyClaimantLipForNoLongerAccessWelshTemplate()
            : notificationsProperties.getNotifyClaimantLipForNoLongerAccessTemplate();
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

    //In case of Lip v Lip to LR v Lip or lip v lr to lr to lr - An email will be sent to claimant lip, defendant lip/lr, claimant lr
    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return Boolean.TRUE;
    }
}

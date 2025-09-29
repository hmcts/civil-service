package uk.gov.hmcts.reform.civil.notification.handlers.notifyhwfoutcomeparties;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;

import java.util.Map;

@Component
public class NotifyHwFOutcomePartiesClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "hwf-outcome-notification-%s";
    private final NotifyHwFOutcomePartiesHelper notifyClaimantHwFOutcomeHelper;

    public NotifyHwFOutcomePartiesClaimantEmailDTOGenerator(NotifyHwFOutcomePartiesHelper notifyClaimantHwFOutcomeHelper) {
        this.notifyClaimantHwFOutcomeHelper = notifyClaimantHwFOutcomeHelper;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        if (caseData.getHwFEvent() != CaseEvent.FULL_REMISSION_HWF && caseData.isApplicantLiP()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notifyClaimantHwFOutcomeHelper.getTemplateBilingual(caseData.getHwFEvent())
            : notifyClaimantHwFOutcomeHelper.getTemplate(caseData.getHwFEvent());
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(notifyClaimantHwFOutcomeHelper.getCommonProperties(caseData));
        properties.putAll(notifyClaimantHwFOutcomeHelper.getFurtherProperties(caseData));
        return properties;
    }
}

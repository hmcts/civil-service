package uk.gov.hmcts.reform.civil.notification.handlers.notifyhwfoutcomeparties;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class NotifyHwFOutcomePartiesClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "hwf-outcome-notification-%s";
    private final NotifyHwFOutcomePartiesHelper notifyClaimantHwFOutcomeHelper;

    public NotifyHwFOutcomePartiesClaimantEmailDTOGenerator(NotifyHwFOutcomePartiesHelper notifyClaimantHwFOutcomeHelper) {
        this.notifyClaimantHwFOutcomeHelper = notifyClaimantHwFOutcomeHelper;
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
        Map<String, String> commonProperties = notifyClaimantHwFOutcomeHelper.getCommonProperties(caseData);
        Map<String, String> furtherProperties = notifyClaimantHwFOutcomeHelper.getFurtherProperties(caseData);
        return Collections.unmodifiableMap(
            Stream.concat(commonProperties.entrySet().stream(), furtherProperties.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }
}

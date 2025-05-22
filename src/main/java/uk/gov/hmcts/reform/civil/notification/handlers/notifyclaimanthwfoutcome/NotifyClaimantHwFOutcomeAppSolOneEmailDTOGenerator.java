package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimanthwfoutcome;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NotifyClaimantHwFOutcomeAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "hwf-outcome-notification-%s";
    private final NotifyClaimantHwFOutcomeHelper notifyClaimantHwFOutcomeHelper;

    public NotifyClaimantHwFOutcomeAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        super(null);
        this.notifyClaimantHwFOutcomeHelper = new NotifyClaimantHwFOutcomeHelper(notificationsProperties);
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        if (caseData.isApplicantLiP()){
            return caseData.getApplicant1Email();
        }
        return caseData.getApplicantSolicitor1UserDetails().getEmail();
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

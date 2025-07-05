package uk.gov.hmcts.reform.civil.notification.handlers.extendresponsedeadline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ExtendResponseDeadlineClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_CLAIMANT_TEMPLATE = "claimant-deadline-extension-notification-%s";

    private final NotificationsProperties notificationsProperties;

    private final PinInPostConfiguration pipInPostConfiguration;

    private final FeatureToggleService featureToggleService;

    protected ExtendResponseDeadlineClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                              PinInPostConfiguration pipInPostConfiguration,
                                                              FeatureToggleService featureToggleService) {
        this.notificationsProperties = notificationsProperties;
        this.featureToggleService = featureToggleService;
        this.pipInPostConfiguration = pipInPostConfiguration;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual() && featureToggleService.isDefendantNoCOnlineForCase(caseData)
            ? notificationsProperties.getClaimantLipDeadlineExtensionWelsh()
            : notificationsProperties.getClaimantLipDeadlineExtension();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_CLAIMANT_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl());
        properties.put(RESPONSE_DEADLINE, formatLocalDate(
                caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE));
        return properties;
    }
}

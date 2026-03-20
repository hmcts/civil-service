package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondatespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class InformAgreedExtensionDateSpecClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "agreed-extension-date-applicant-notification-spec-%s";

    private final NotificationsProperties notificationsProperties;
    private final PinInPostConfiguration pinInPostConfiguration;

    protected InformAgreedExtensionDateSpecClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                                     PinInPostConfiguration pinInPostConfiguration) {
        this.notificationsProperties = notificationsProperties;
        this.pinInPostConfiguration = pinInPostConfiguration;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getClaimantLipDeadlineExtensionWelsh()
            : notificationsProperties.getClaimantLipDeadlineExtension();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(FRONTEND_URL, pinInPostConfiguration.getCuiFrontEndUrl());
        properties.put(RESPONSE_DEADLINE, formatLocalDate(
            caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE
        ));
        return properties;
    }
}

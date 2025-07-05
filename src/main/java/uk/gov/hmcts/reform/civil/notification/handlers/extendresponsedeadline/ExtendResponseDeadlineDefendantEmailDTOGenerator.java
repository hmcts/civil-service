package uk.gov.hmcts.reform.civil.notification.handlers.extendresponsedeadline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ExtendResponseDeadlineDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_DEFENDANT_TEMPLATE = "defendant-deadline-extension-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected ExtendResponseDeadlineDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getRespondentDeadlineExtensionWelsh()
            : notificationsProperties.getRespondentDeadlineExtension();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_DEFENDANT_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(AGREED_EXTENSION_DATE, formatLocalDate(
                caseData.getRespondentSolicitor1AgreedDeadlineExtension(), DATE));
        return properties;
    }
}

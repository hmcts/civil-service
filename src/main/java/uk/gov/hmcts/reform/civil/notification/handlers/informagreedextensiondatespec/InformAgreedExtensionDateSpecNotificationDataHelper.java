package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondatespec;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.fetchDefendantName;

final class InformAgreedExtensionDateSpecNotificationDataHelper {

    private InformAgreedExtensionDateSpecNotificationDataHelper() {
        // Utility class
    }

    static Map<String, String> addApplicantSolicitorProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(AGREED_EXTENSION_DATE,
            formatLocalDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension(), DATE));
        properties.put(DEFENDANT_NAME, fetchDefendantName(caseData));
        return properties;
    }

    static Map<String, String> addRespondentSolicitorProperties(Map<String, String> properties, CaseData caseData) {
        LocalDate extensionDate = resolveRespondentExtensionDate(caseData);
        properties.put(AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE));
        return properties;
    }

    private static LocalDate resolveRespondentExtensionDate(CaseData caseData) {
        LocalDate extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();

        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        if (scenario == ONE_V_TWO_TWO_LEGAL_REP || scenario == ONE_V_TWO_ONE_LEGAL_REP) {
            if (caseData.getRespondentSolicitor1AgreedDeadlineExtension() == null
                && caseData.getRespondentSolicitor2AgreedDeadlineExtension() != null) {
                extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
            } else if (caseData.getRespondentSolicitor1AgreedDeadlineExtension() != null
                && caseData.getRespondentSolicitor2AgreedDeadlineExtension() != null
                && caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                    .isAfter(caseData.getRespondentSolicitor1AgreedDeadlineExtension())) {
                extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
            }
        }
        return extensionDate;
    }
}

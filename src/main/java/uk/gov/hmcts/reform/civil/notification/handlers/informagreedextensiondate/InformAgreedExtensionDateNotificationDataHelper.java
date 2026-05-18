package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondate;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.fetchDefendantName;

final class InformAgreedExtensionDateNotificationDataHelper {

    private InformAgreedExtensionDateNotificationDataHelper() {
        // Utility class
    }

    static Map<String, String> addCommonProperties(Map<String, String> properties, CaseData caseData) {
        LocalDateTime extensionDate = resolveExtensionDate(caseData);
        properties.put(AGREED_EXTENSION_DATE, formatLocalDate(extensionDate.toLocalDate(), DATE));
        properties.put(DEFENDANT_NAME, fetchDefendantName(caseData));
        return properties;
    }

    private static LocalDateTime resolveExtensionDate(CaseData caseData) {
        LocalDateTime extensionDate = caseData.getRespondent1ResponseDeadline();

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP || multiPartyScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            if (caseData.getRespondent1ResponseDeadline() == null
                && caseData.getRespondent2ResponseDeadline() != null) {
                extensionDate = caseData.getRespondent2ResponseDeadline();
            } else if (caseData.getRespondent1ResponseDeadline() != null
                && caseData.getRespondent2ResponseDeadline() == null) {
                extensionDate = caseData.getRespondent1ResponseDeadline();
            } else if (caseData.getRespondent1ResponseDeadline() != null
                && caseData.getRespondent2ResponseDeadline() != null) {
                if (caseData.getRespondent2ResponseDeadline()
                    .isAfter(caseData.getRespondent1ResponseDeadline())) {
                    extensionDate = caseData.getRespondent2ResponseDeadline();
                }
            }
        }

        return extensionDate;
    }
}

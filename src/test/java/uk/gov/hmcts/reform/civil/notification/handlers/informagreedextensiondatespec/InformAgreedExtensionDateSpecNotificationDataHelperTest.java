package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondatespec;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

class InformAgreedExtensionDateSpecNotificationDataHelperTest {

    @Test
    void shouldAddApplicantSolicitorProperties() {
        LocalDate extensionDate = LocalDate.of(2025, 12, 25);
        CaseData caseData = CaseData.builder()
            .respondentSolicitor1AgreedDeadlineExtension(extensionDate)
            .build();

        try (MockedStatic<PartyUtils> partyUtils = Mockito.mockStatic(PartyUtils.class);
             MockedStatic<DateFormatHelper> dateFormatHelper = Mockito.mockStatic(DateFormatHelper.class)) {

            partyUtils.when(() -> PartyUtils.fetchDefendantName(caseData)).thenReturn("Defendant");
            dateFormatHelper.when(() -> DateFormatHelper.formatLocalDate(extensionDate, DATE))
                .thenReturn("25 December 2025");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updated = InformAgreedExtensionDateSpecNotificationDataHelper
                .addApplicantSolicitorProperties(properties, caseData);

            assertThat(updated)
                .containsEntry(AGREED_EXTENSION_DATE, "25 December 2025")
                .containsEntry(DEFENDANT_NAME, "Defendant");
        }
    }

    @Test
    void shouldUseRespondentTwoExtensionWhenLater() {
        LocalDate respondent1Extension = LocalDate.of(2025, 1, 1);
        LocalDate respondent2Extension = LocalDate.of(2025, 2, 1);
        CaseData caseData = CaseData.builder()
            .respondentSolicitor1AgreedDeadlineExtension(respondent1Extension)
            .respondentSolicitor2AgreedDeadlineExtension(respondent2Extension)
            .build();

        try (MockedStatic<MultiPartyScenario> multiPartyScenario = Mockito.mockStatic(MultiPartyScenario.class);
             MockedStatic<DateFormatHelper> dateFormatHelper = Mockito.mockStatic(DateFormatHelper.class)) {

            multiPartyScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP);
            dateFormatHelper.when(() -> DateFormatHelper.formatLocalDate(respondent2Extension, DATE))
                .thenReturn("01 February 2025");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updated = InformAgreedExtensionDateSpecNotificationDataHelper
                .addRespondentSolicitorProperties(properties, caseData);

            assertThat(updated)
                .containsEntry(AGREED_EXTENSION_DATE, "01 February 2025");
        }
    }
}

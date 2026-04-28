package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondate;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

class InformAgreedExtensionDateNotificationDataHelperTest {

    @Test
    void shouldUseRespondentOneDeadlineWhenSingleDefendant() {
        LocalDateTime respondent1Deadline = LocalDateTime.of(2025, 4, 1, 12, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1ResponseDeadline(respondent1Deadline)
            .build();

        try (MockedStatic<PartyUtils> partyUtils = Mockito.mockStatic(PartyUtils.class);
             MockedStatic<DateFormatHelper> dateFormatHelper = Mockito.mockStatic(DateFormatHelper.class);
             MockedStatic<MultiPartyScenario> multiPartyScenario = Mockito.mockStatic(MultiPartyScenario.class)) {

            partyUtils.when(() -> PartyUtils.fetchDefendantName(caseData)).thenReturn("Defendant");
            dateFormatHelper.when(() -> DateFormatHelper.formatLocalDate(respondent1Deadline.toLocalDate(), DATE))
                .thenReturn("01 April 2025");
            multiPartyScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_ONE);

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updated = InformAgreedExtensionDateNotificationDataHelper.addCommonProperties(properties, caseData);

            assertThat(updated)
                .containsEntry(AGREED_EXTENSION_DATE, "01 April 2025")
                .containsEntry(DEFENDANT_NAME, "Defendant");
        }
    }

    @Test
    void shouldUseRespondentTwoDeadlineWhenLater() {
        LocalDateTime respondent1Deadline = LocalDateTime.of(2025, 2, 1, 12, 0);
        LocalDateTime respondent2Deadline = LocalDateTime.of(2025, 5, 1, 12, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1ResponseDeadline(respondent1Deadline)
            .respondent2ResponseDeadline(respondent2Deadline)
            .build();

        try (MockedStatic<PartyUtils> partyUtils = Mockito.mockStatic(PartyUtils.class);
             MockedStatic<DateFormatHelper> dateFormatHelper = Mockito.mockStatic(DateFormatHelper.class);
             MockedStatic<MultiPartyScenario> multiPartyScenario = Mockito.mockStatic(MultiPartyScenario.class)) {

            partyUtils.when(() -> PartyUtils.fetchDefendantName(caseData)).thenReturn("Second Defendant");
            dateFormatHelper.when(() -> DateFormatHelper.formatLocalDate(respondent2Deadline.toLocalDate(), DATE))
                .thenReturn("01 May 2025");
            multiPartyScenario.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP);

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updated = InformAgreedExtensionDateNotificationDataHelper.addCommonProperties(properties, caseData);

            assertThat(updated)
                .containsEntry(AGREED_EXTENSION_DATE, "01 May 2025")
                .containsEntry(DEFENDANT_NAME, "Second Defendant");
        }
    }
}

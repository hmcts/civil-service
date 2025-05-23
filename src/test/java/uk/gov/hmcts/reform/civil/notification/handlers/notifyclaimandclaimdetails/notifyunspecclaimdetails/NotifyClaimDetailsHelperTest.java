package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifyClaimDetailsHelperTest {

    private NotificationsProperties notificationsProperties;
    private DeadlinesCalculator deadlinesCalculator;
    private NotifyClaimDetailsHelper notifyClaimDetailsHelper;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        deadlinesCalculator = mock(DeadlinesCalculator.class);
        notifyClaimDetailsHelper = new NotifyClaimDetailsHelper(notificationsProperties, deadlinesCalculator);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        // Given
        String expectedTemplateId = "template-id-abc";
        when(notificationsProperties.getRespondentSolicitorClaimDetailsEmailTemplate())
            .thenReturn(expectedTemplateId);

        // When
        String actualTemplateId = notifyClaimDetailsHelper.getEmailTemplate();

        assertEquals(expectedTemplateId, actualTemplateId);
    }

    @Test
    void shouldReturnCorrectCustomProperties() {
        LocalDateTime responseDeadline = LocalDateTime.of(2025, 6, 1, 0, 0);
        LocalDateTime plus28DaysDeadline = LocalDateTime.of(2025, 6, 29, 0, 0);

        CaseData caseData = CaseData.builder()
            .respondent1ResponseDeadline(responseDeadline)
            .build();

        when(deadlinesCalculator.plus14DaysDeadline(responseDeadline)).thenReturn(plus28DaysDeadline);

        Map<String, String> props = notifyClaimDetailsHelper.getCustomProperties(caseData);

        assertEquals(2, props.size());
        assertEquals("1 June 2025", props.get("responseDeadline")); // formatted date
        assertEquals("29 June 2025", props.get("responseDeadlinePlus28")); // formatted date
    }

    @Test
    void shouldReturnTrueWhenDefendantNameMatches() {
        String targetDefendant = "Defendant One";

        DynamicListElement selected = DynamicListElement.builder().label(targetDefendant).build();
        DynamicList list = DynamicList.builder().value(selected).build();

        CaseData caseData = CaseData.builder()
            .defendantSolicitorNotifyClaimDetailsOptions(list)
            .build();

        boolean result = notifyClaimDetailsHelper.checkDefendantToBeNotifiedWithClaimDetails(caseData, targetDefendant);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenBothDefendantsSelected() {
        DynamicListElement selected = DynamicListElement.builder().label("Both Defendants").build();
        DynamicList list = DynamicList.builder().value(selected).build();

        CaseData caseData = CaseData.builder()
            .defendantSolicitorNotifyClaimDetailsOptions(list)
            .build();

        boolean result = notifyClaimDetailsHelper.checkDefendantToBeNotifiedWithClaimDetails(caseData, "Any Name");

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenDefendantNameDoesNotMatch() {
        // Given
        String targetDefendant = "Defendant One";

        DynamicListElement selected = DynamicListElement.builder().label("Defendant Two").build();
        DynamicList list = DynamicList.builder().value(selected).build();

        CaseData caseData = CaseData.builder()
            .defendantSolicitorNotifyClaimDetailsOptions(list)
            .build();

        boolean result = notifyClaimDetailsHelper.checkDefendantToBeNotifiedWithClaimDetails(caseData, targetDefendant);

        assertFalse(result);
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifyClaimHelperTest {

    private NotificationsProperties notificationsProperties;
    private NotifyClaimHelper notifyClaimHelper;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        notifyClaimHelper = new NotifyClaimHelper(notificationsProperties);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        String expectedTemplateId = "template-id-xyz";
        when(notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate())
            .thenReturn(expectedTemplateId);

        String actual = notifyClaimHelper.getNotifyClaimEmailTemplate();

        assertEquals(expectedTemplateId, actual);
    }

    @Test
    void shouldReturnCorrectCustomProperties() {
        LocalDateTime deadline = LocalDateTime.of(2025, 6, 1, 0, 0);

        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder()
                             .individualTitle("Mr")
                             .individualFirstName("John").individualLastName("Doe")
                             .type(Party.Type.INDIVIDUAL).build())
            .claimDetailsNotificationDeadline(deadline)
            .build();

        Map<String, String> props = notifyClaimHelper.retrieveCustomProperties(caseData);

        assertEquals(2, props.size());
        assertEquals("Mr John Doe", props.get("defendantName"));
        assertEquals("1 June 2025", props.get("claimDetailsNotificationDeadline")); // depends on DATE pattern
    }

    @Test
    void shouldReturnTrueWhenDefendantNameMatches() {
        String targetDefendant = "Defendant One";

        DynamicListElement selected = DynamicListElement.builder().label(targetDefendant).build();
        DynamicList list = DynamicList.builder().value(selected).build();

        CaseData caseData = CaseData.builder()
            .defendantDetails(list)
            .build();

        boolean result = notifyClaimHelper.checkIfThisDefendantToBeNotified(caseData, targetDefendant);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenBothDefendantsSelected() {
        DynamicListElement selected = DynamicListElement.builder().label("Both Defendants").build();
        DynamicList list = DynamicList.builder().value(selected).build();

        CaseData caseData = CaseData.builder()
            .defendantDetails(list)
            .build();

        boolean result = notifyClaimHelper.checkIfThisDefendantToBeNotified(caseData, "Any Name");

        assertTrue(result);
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;

@ExtendWith(MockitoExtension.class)
public class HearingProcessClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private HearingProcessClaimantEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenBilingual() {
        CaseData caseData = CaseData.builder().claimantBilingualLanguagePreference(BOTH.toString()).build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenNotBilingual() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getHearingNotificationLipDefendantTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("notification-of-hearing-lip-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Party party = Party.builder().companyName("Party Name").type(Party.Type.COMPANY).build();
        CaseData caseData = CaseData.builder()
            .hearingDate(LocalDate.parse("2025-07-01"))
            .hearingTimeHourMinute("10:30")
            .applicant1(party)
            .build();

        try (MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class)) {
            notificationUtilsMockedStatic.when(() -> NotificationUtils.getFormattedHearingDate(LocalDate.parse("2025-07-01")))
                .thenReturn("1 July 2025");
            notificationUtilsMockedStatic.when(() -> NotificationUtils.getFormattedHearingTime("10:30"))
                .thenReturn("10:30 AM");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

            assertThat(updatedProperties)
                .containsEntry(HEARING_DATE, "1 July 2025")
                .containsEntry(HEARING_TIME, "10:30 AM")
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Party Name")
                .hasSize(3);
        }
    }

}

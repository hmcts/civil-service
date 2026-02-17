package uk.gov.hmcts.reform.civil.notification.handlers.notifylipresetpin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PIN;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPOND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class NotifyLipResetPinDefendantEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String REFERENCE_TEMPLATE = "claim-reset-pin-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @InjectMocks
    private NotifyLipResetPinDefendantEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getRespondentDefendantResponseForSpec()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddClaimAndDefendantSpecificProperties() {
        LocalDate issueDate = LocalDate.of(2024, 1, 1);
        LocalDateTime responseDeadline = LocalDateTime.of(2024, 1, 20, 10, 0);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Jane")
                .individualLastName("Roe")
                .partyName("Jane Roe")
                .build())
            .respondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("John")
                .individualLastName("Doe")
                .partyName("John Doe")
                .build())
            .issueDate(issueDate)
            .ccdCaseReference(1234567890123456L)
            .respondent1PinToPostLRspec(new DefendantPinToPostLRspec().setAccessCode("PINCODE"))
            .respondent1ResponseDeadline(responseDeadline)
            .build();
        Map<String, String> properties = new HashMap<>();

        when(pinInPostConfiguration.getRespondToClaimUrl()).thenReturn("respond-url");
        when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("frontend-url");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsAllEntriesOf(Map.of(
            RESPONDENT_NAME, "John Doe",
            CLAIMANT_NAME, "Jane Roe",
            ISSUED_ON, formatLocalDate(issueDate, DATE),
            RESPOND_URL, "respond-url",
            CLAIM_16_DIGIT_NUMBER, "1234567890123456",
            PIN, "PINCODE",
            RESPONSE_DEADLINE, formatLocalDate(responseDeadline.toLocalDate(), DATE),
            FRONTEND_URL, "frontend-url"
        ));
    }
}

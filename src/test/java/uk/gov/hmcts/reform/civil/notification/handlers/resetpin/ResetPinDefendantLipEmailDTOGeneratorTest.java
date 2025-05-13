package uk.gov.hmcts.reform.civil.notification.handlers.resetpin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PIN;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPOND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

class ResetPinDefendantLipEmailDTOGeneratorTest {

    @Mock
    private PinInPostConfiguration pipInPostConfiguration;

    @Mock
    private NotificationsProperties notificationsProperties;

    private ResetPinDefendantLipEmailDTOGenerator emailDTOGenerator;

    private final DefendantPinToPostLRspec pin =
        DefendantPinToPostLRspec.builder()
            .expiryDate(LocalDate.now())
            .citizenCaseRole("citizen")
            .respondentCaseRole("citizen")
            .accessCode("TEST1234").build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailDTOGenerator = new ResetPinDefendantLipEmailDTOGenerator(pipInPostConfiguration, notificationsProperties);
    }

    @Test
    void shouldNotify_whenRespondentIsLiPAndHasEmail() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder().partyEmail("respondent@example.com").build()).respondent1Represented(YesOrNo.NO)
            .respondent1PinToPostLRspec(pin)
            .build();

        boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldNotNotify_whenRespondentIsNotLiP() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder().partyEmail("respondent@example.com").build())
            .respondent1PinToPostLRspec(pin)
            .build();

        boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldAddPropertiesCorrectly() {

        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder().type(Party.Type.ORGANISATION).organisationName("Mr. Sole Trader").build())
            .applicant1(Party.builder().type(Party.Type.ORGANISATION).organisationName("Claimant Name").build())
            .issueDate(LocalDate.of(2023, 1, 1))
            .legacyCaseReference(LEGACY_CASE_REFERENCE)
            .ccdCaseReference(1234567890123456L)
            .respondent1PinToPostLRspec(pin)
            .respondent1ResponseDeadline(LocalDate.of(2023, 1, 15).atStartOfDay())
            .build();

        when(notificationsProperties.getRespondentDefendantResponseForSpec())
            .thenReturn("template-id");
        when(pipInPostConfiguration.getRespondToClaimUrl()).thenReturn("dummy_respond_to_claim_url");
        when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");

        Map<String, String> properties = emailDTOGenerator.addProperties(caseData);

        assertThat(properties).containsEntry(RESPONDENT_NAME, "Mr. Sole Trader");
        assertThat(properties).containsEntry(CLAIMANT_NAME, "Claimant Name");
        assertThat(properties).containsEntry(ISSUED_ON, "1 January 2023");
        assertThat(properties).containsEntry(RESPOND_URL, "dummy_respond_to_claim_url");
        assertThat(properties).containsEntry(CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE);
        assertThat(properties).containsEntry("claim16DigitNumber", "1234567890123456");
        assertThat(properties).containsEntry(PIN, "TEST1234");
        assertThat(properties).containsEntry(RESPONSE_DEADLINE, formatLocalDate(
            caseData.getRespondent1ResponseDeadline()
                .toLocalDate(), DATE));
        assertThat(properties).containsEntry(FRONTEND_URL, "dummy_cui_front_end_url");
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder().type(Party.Type.ORGANISATION).partyEmail("respondent@example.com").build())
            .build();

        String emailAddress = emailDTOGenerator.getEmailAddress(caseData);

        assertThat(emailAddress).isEqualTo("respondent@example.com");
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getRespondentDefendantResponseForSpec()).thenReturn("template-id");

        String templateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("template-id");
    }
}


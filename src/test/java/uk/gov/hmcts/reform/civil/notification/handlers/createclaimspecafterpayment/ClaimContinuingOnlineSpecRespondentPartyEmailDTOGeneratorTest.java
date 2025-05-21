package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPOND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecRespondentPartyEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String EMAIL = "respondent@example.com";
    public static final String RESPONDENT_NAME = "Respondent Name";
    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final String CLAIMANT_NAME = "Claimant Name";
    public static final String RESPOND_TO_CLAIM_URL = "dummy_respond_to_claim_url";
    public static final String CUI_FRONT_END_URL = "dummy_cui_front_end_url";
    public static final long CCD_CASE_REFERENCE = 1234567890123456L;
    public static final String PIN = "TEST1234";
    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";
    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @InjectMocks
    private ClaimContinuingOnlineSpecRespondentPartyEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        when(notificationsProperties.getRespondentDefendantResponseForSpec()).thenReturn(TEMPLATE_ID);
        CaseData caseData = CaseData.builder().build();

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().partyEmail(EMAIL).build())
                .build();

        String emailAddress = generator.getEmailAddress(caseData);

        assertThat(emailAddress).isEqualTo(EMAIL);
    }

    @Test
    void shouldReturnSamePropertiesInAddCustomProperties() {
        Map<String, String> properties = Map.of("key1", "value1", "key2", "value2");
        CaseData caseData = CaseData.builder().build();

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).isEqualTo(properties);
    }

    @Test
    void shouldReturnTrueWhenRespondentHasEmail() {
        CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().partyEmail(EMAIL).build())
                .build();

        Boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldReturnFalseWhenRespondentIsNull() {
        CaseData caseData = CaseData.builder().respondent1(null).build();

        Boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldReturnFalseWhenRespondentHasNoEmail() {
        CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().partyEmail(null).build())
                .build();

        Boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(CLAIM_CONTINUING_ONLINE_NOTIFICATION);
    }

    @Test
    void shouldReturnCorrectNotificationProperties() {
        when(pinInPostConfiguration.getRespondToClaimUrl()).thenReturn(RESPOND_TO_CLAIM_URL);
        when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn(CUI_FRONT_END_URL);
        CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().companyName(RESPONDENT_NAME).type(Party.Type.COMPANY).build())
                .applicant1(Party.builder().companyName(CLAIMANT_NAME).type(Party.Type.COMPANY).build())
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .ccdCaseReference(CCD_CASE_REFERENCE)
                .respondent1PinToPostLRspec(DefendantPinToPostLRspec.builder().accessCode(PIN).build())
                .respondent1ResponseDeadline(LocalDate.now().plusDays(14).atStartOfDay())
                .issueDate(LocalDate.now())
                .build();

        Map<String, String> properties = generator.addProperties(caseData);

        assertThat(properties).containsEntry(NotificationData.RESPONDENT_NAME, RESPONDENT_NAME);
        assertThat(properties).containsEntry(NotificationData.CLAIMANT_NAME, CLAIMANT_NAME);
        assertThat(properties).containsEntry(ISSUED_ON, DateFormatHelper.formatLocalDate(LocalDate.now(), DateFormatHelper.DATE));
        assertThat(properties).containsEntry(RESPOND_URL, RESPOND_TO_CLAIM_URL);
        assertThat(properties).containsEntry(CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE);
        assertThat(properties).containsEntry(CLAIM_16_DIGIT_NUMBER, "1234567890123456");
        assertThat(properties).containsEntry(NotificationData.PIN, PIN);
        assertThat(properties).containsEntry(RESPONSE_DEADLINE, DateFormatHelper.formatLocalDate(LocalDate.now().plusDays(14), DateFormatHelper.DATE));
        assertThat(properties).containsEntry(FRONTEND_URL, CUI_FRONT_END_URL);
    }
}
package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

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
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PIN;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPOND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
public class ClaimContinuingOnlineSpecDefendantEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final String RESPOND_TO_CLAIM_URL = "dummy_respond_to_claim_url";
    public static final String CUI_FRONT_END_URL = "dummy_cui_front_end_url";
    public static final long CCD_CASE_REFERENCE = 1234567890123456L;
    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @InjectMocks
    private ClaimContinuingOnlineSpecDefendantEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        when(notificationsProperties.getRespondentDefendantResponseForSpec()).thenReturn(TEMPLATE_ID);
        CaseData caseData = CaseData.builder().build();

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
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
                .respondent1PinToPostLRspec(DefendantPinToPostLRspec.builder().accessCode("12346").build())
                .respondent1ResponseDeadline(LocalDate.now().plusDays(14).atStartOfDay())
                .issueDate(LocalDate.now())
                .build();

        Map<String, String> initial = generator.addCustomProperties(new HashMap<>(), caseData);

        Map<String, String> properties = generator.addCustomProperties(initial, caseData);

        assertThat(properties).containsAllEntriesOf(Map.of(
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
                RESPOND_URL, pinInPostConfiguration.getRespondToClaimUrl(),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
                PIN, caseData.getRespondent1PinToPostLRspec().getAccessCode(),
                RESPONSE_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE),
                FRONTEND_URL, pinInPostConfiguration.getCuiFrontEndUrl()
        ));
    }
}

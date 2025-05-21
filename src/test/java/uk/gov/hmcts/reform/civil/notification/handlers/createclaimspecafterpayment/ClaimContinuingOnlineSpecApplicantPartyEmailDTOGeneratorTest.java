package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecApplicantPartyEmailDTOGeneratorTest {

    private static final String DEFAULT_TEMPLATE = "default-template";
    private static final String BILINGUAL_TEMPLATE = "bilingual-template";
    public static final String APP_NAME = "App Name";
    public static final String RESP_NAME = "Resp Name";
    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ClaimContinuingOnlineSpecApplicantPartyEmailDTOGenerator generator;

    @Test
    void shouldReturnDefaultTemplate() {
        when(notificationsProperties.getClaimantClaimContinuingOnlineForSpec())
                .thenReturn(DEFAULT_TEMPLATE);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build();
        String template = generator.getEmailTemplateId(caseData);

        assertThat(template).isEqualTo(DEFAULT_TEMPLATE);
    }

    @Test
    void shouldReturnBilingualTemplateWhenBilingualAndToggleEnabled() {
        when(notificationsProperties.getBilingualClaimantClaimContinuingOnlineForSpec())
                .thenReturn(BILINGUAL_TEMPLATE);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.isClaimantBilingual()).thenReturn(true);

        String template = generator.getEmailTemplateId(caseData);

        assertThat(template).isEqualTo(BILINGUAL_TEMPLATE);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String refTpl = generator.getReferenceTemplate();
        assertThat(refTpl).isEqualTo(CLAIM_CONTINUING_ONLINE_NOTIFICATION);
    }

    @Test
    void shouldAlwaysNotify() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build();
        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldAddCustomProperties() {
        LocalDate issueDate = LocalDate.of(2022, 1, 1);
        LocalDateTime deadline = LocalDateTime.of(2022, 1, 15, 0, 0);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build().toBuilder()
                .respondent1(Party.builder().companyName(RESP_NAME).type(Party.Type.COMPANY).build())
                .applicant1(Party.builder().companyName(APP_NAME).type(Party.Type.COMPANY).build())
                .issueDate(issueDate)
                .respondent1ResponseDeadline(deadline)
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .build();

        Map<String, String> props = generator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(props).containsEntry(RESPONDENT_NAME, RESP_NAME);
        assertThat(props).containsEntry(CLAIMANT_NAME, APP_NAME);
        assertThat(props).containsEntry(ISSUED_ON, formatLocalDate(issueDate, DATE));
        assertThat(props).containsEntry(CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE);
        assertThat(props).containsEntry(RESPONSE_DEADLINE, formatLocalDate(deadline.toLocalDate(), DATE));
    }
}
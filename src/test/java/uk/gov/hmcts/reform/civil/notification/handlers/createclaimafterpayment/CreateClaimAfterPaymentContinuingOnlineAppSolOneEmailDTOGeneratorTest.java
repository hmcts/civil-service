package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentContinuingOnlineAppSolOneEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";
    public static final String ISSUED_ON = "issuedOn";
    public static final String NOTIFICATION_DEADLINE = "notificationDeadline";
    public static final String CLAIMANT_NAME = "claimantName";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CreateClaimAfterPaymentContinuingOnlineAppSolOneEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
                .issueDate(LocalDate.of(2023, 10, 1))
                .claimNotificationDeadline(LocalDate.of(2023, 10, 15).atStartOfDay())
                .applicant1(Party.builder()
                        .individualFirstName("John")
                        .individualLastName("Doe")
                        .type(Party.Type.INDIVIDUAL)
                        .build())
                .build();
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineCos()).thenReturn(TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(CLAIM_CONTINUING_ONLINE_NOTIFICATION);
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = generator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(properties).containsEntry(ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE));
        assertThat(properties).containsEntry(NOTIFICATION_DEADLINE, formatLocalDate(caseData.getClaimNotificationDeadline().toLocalDate(), DATE));
        assertThat(properties).containsEntry(CLAIMANT_NAME, "John Doe");
    }
}
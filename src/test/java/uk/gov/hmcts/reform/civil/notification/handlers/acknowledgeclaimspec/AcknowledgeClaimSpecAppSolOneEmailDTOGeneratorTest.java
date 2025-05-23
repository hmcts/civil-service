package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimSpecAppSolOneEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String ACKNOWLEDGE_CLAIM_APPLICANT_NOTIFICATION = "acknowledge-claim-applicant-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private AcknowledgeClaimSpecAppSolOneEmailDTOGenerator emailGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getApplicantSolicitorAcknowledgeClaimForSpec())
                .thenReturn(TEMPLATE_ID);

        String actual = emailGenerator.getEmailTemplateId(caseData);
        assertThat(actual).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>(Map.of("existingKey", "existingValue"));
        CaseData caseData = CaseData.builder()
                .issueDate(LocalDate.of(2025, 1, 1))
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().build())
                .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder()
                        .name("John Doe")
                        .build())
                .build();

        Map<String, String> updatedProps = emailGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProps)
                .containsEntry("existingKey", "existingValue")
                .containsEntry("issuedOn", "1 January 2025");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String ref = emailGenerator.getReferenceTemplate();
        assertThat(ref).isEqualTo(ACKNOWLEDGE_CLAIM_APPLICANT_NOTIFICATION);
    }
}

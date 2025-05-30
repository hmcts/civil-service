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
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimSpecAppSolOneEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String ACKNOWLEDGE_CLAIM_APPLICANT_NOTIFICATION = "acknowledge-claim-applicant-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

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
        uk.gov.hmcts.reform.ccd.model.Organisation organisation = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("Org123")
                .build();

        Map<String, String> properties = new HashMap<>(Map.of("existingKey", "existingValue"));
        LocalDateTime deadline = LocalDateTime.of(2025, 5, 30, 12, 39, 18);
        CaseData caseData = CaseData.builder()
                .issueDate(LocalDate.of(2025, 1, 1))
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder()
                        .name("John Doe")
                        .build())
                .respondent1ResponseDeadline(deadline)
                .build();

        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        Map<String, String> updatedProps = emailGenerator.addCustomProperties(properties, caseData);

        System.out.println("Parameters: " + updatedProps);

        assertThat(updatedProps)
                .containsEntry("existingKey", "existingValue")
                .containsEntry("responseDeadline", "30 May 2025");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String ref = emailGenerator.getReferenceTemplate();
        assertThat(ref).isEqualTo(ACKNOWLEDGE_CLAIM_APPLICANT_NOTIFICATION);
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.TemplateCommonPropertiesHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimSpecRespSolOneEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String ACKNOWLEDGE_CLAIM_RESPONDENT_NOTIFICATION = "acknowledge-claim-respondent-notification-%s";
    public static final String TASK_ID = "reference";
    public final LocalDateTime deadline = LocalDateTime.of(2025, 5, 8, 0, 0);

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private TemplateCommonPropertiesHelper templateCommonPropertiesHelper;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private AcknowledgeClaimSpecRespSolOneEmailDTOGenerator emailGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaimForSpec())
                .thenReturn(TEMPLATE_ID);

        String actual = emailGenerator.getEmailTemplateId(caseData);
        assertThat(actual).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldIncludeRespondent1NameAndDeadlineInParameters() {
        Party respondent1 = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("John")
                .individualLastName("Doe")
                .build();

        Organisation organisation = Organisation.builder()
                .organisationID("Org123")
                .build();

        CaseData caseData = CaseData.builder()
                .ccdCaseReference(1L)
                .legacyCaseReference("ref1")
                .respondent1(respondent1)
                .respondent1ResponseDeadline(deadline)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build())
                .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder().build())
                .build();

        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().name("org name").build()));

        EmailDTO dto = emailGenerator.buildEmailDTO(caseData, TASK_ID);
        Map<String, String> params = dto.getParameters();

        System.out.println("Parameters: " + params);

        assertThat(params)
                .containsEntry("legalOrgName", "org name")
                .containsEntry(RESPONSE_DEADLINE, formatLocalDate(deadline.toLocalDate(), DATE));
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String ref = emailGenerator.getReferenceTemplate();
        assertThat(ref).isEqualTo(ACKNOWLEDGE_CLAIM_RESPONDENT_NOTIFICATION);
    }

    @Test
    void shouldAlwaysReturnTrueForGetShouldNotify() {
        CaseData caseData = CaseData.builder().build();
        Boolean result = emailGenerator.getShouldNotify(caseData);
        assertThat(result).isTrue();
    }
}

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
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimSpecRespSolOneEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String ACKNOWLEDGE_CLAIM_RESPONDENT_NOTIFICATION = "acknowledge-claim-respondent-notification-%s";
    public final LocalDateTime deadline = LocalDateTime.of(2025, 5, 8, 0, 0);

    @Mock
    private NotificationsProperties notificationsProperties;

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
                .build();

        CaseData caseData = CaseData.builder()
                .ccdCaseReference(1L)
                .legacyCaseReference("ref1")
                .respondent1(respondent1)
                .respondent1ResponseDeadline(deadline)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(organisation).build()
                )
                .build();

        EmailDTO dto = emailGenerator.buildEmailDTO(caseData);
        Map<String, String> params = dto.getParameters();

        assertThat(params)
                .containsEntry(RESPONDENT_NAME, "John Doe")
                .containsEntry(RESPONSE_DEADLINE, formatLocalDate(deadline.toLocalDate(), DATE));
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String ref = emailGenerator.getReferenceTemplate();
        assertThat(ref).isEqualTo(ACKNOWLEDGE_CLAIM_RESPONDENT_NOTIFICATION);
    }
}

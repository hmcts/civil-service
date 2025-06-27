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
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private OrganisationService organisationService;

    @InjectMocks
    private AcknowledgeClaimSpecRespSolOneEmailDTOGenerator emailGenerator;

    private static final String ORG_ID = "ORG123";
    private static final String ORG_NAME = "Test Organisation";
    private static final String FALLBACK_NAME = "Fallback Solicitor Name";
    private static final LocalDateTime DEADLINE = LocalDateTime.of(2025, 6, 1, 23, 59);

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getRespondentSolicitorAcknowledgeClaimForSpec())
                .thenReturn(TEMPLATE_ID);

        String actual = emailGenerator.getEmailTemplateId(caseData);
        assertThat(actual).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldAddOrgNameAndDeadline_WhenOrganisationFound() {
        // Given
        uk.gov.hmcts.reform.ccd.model.Organisation ccdOrg =
            uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID(ORG_ID).build();

        OrganisationPolicy policy = OrganisationPolicy.builder().organisation(ccdOrg).build();
        StatementOfTruth statement = StatementOfTruth.builder().name(FALLBACK_NAME).build();

        CaseData caseData = CaseData.builder()
            .respondent1OrganisationPolicy(policy)
            .applicantSolicitor1ClaimStatementOfTruth(statement)
            .respondent1ResponseDeadline(DEADLINE)
            .build();

        when(organisationService.findOrganisationById(ORG_ID))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                                        .name(ORG_NAME).build()));

        Map<String, String> properties = new HashMap<>();

        // When
        Map<String, String> result = emailGenerator.addCustomProperties(properties, caseData);

        // Then
        assertThat(result)
            .containsEntry("legalOrgName", ORG_NAME)
            .containsEntry(RESPONSE_DEADLINE, formatLocalDate(DEADLINE.toLocalDate(), DATE));
    }

    @Test
    void shouldUseFallbackName_WhenOrganisationNotFound() {
        // Given
        uk.gov.hmcts.reform.ccd.model.Organisation ccdOrg =
            uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID(ORG_ID).build();

        OrganisationPolicy policy = OrganisationPolicy.builder().organisation(ccdOrg).build();
        StatementOfTruth statement = StatementOfTruth.builder().name(FALLBACK_NAME).build();

        CaseData caseData = CaseData.builder()
            .respondent1OrganisationPolicy(policy)
            .applicantSolicitor1ClaimStatementOfTruth(statement)
            .respondent1ResponseDeadline(DEADLINE)
            .build();

        when(organisationService.findOrganisationById(ORG_ID)).thenReturn(Optional.empty());

        Map<String, String> properties = new HashMap<>();

        // When
        Map<String, String> result = emailGenerator.addCustomProperties(properties, caseData);

        // Then
        assertThat(result)
            .containsEntry("legalOrgName", FALLBACK_NAME)
            .containsEntry(RESPONSE_DEADLINE, formatLocalDate(DEADLINE.toLocalDate(), DATE));
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

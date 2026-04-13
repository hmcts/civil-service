package uk.gov.hmcts.reform.civil.notification.handlers.judgmentbyadmission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;

@ExtendWith(MockitoExtension.class)
class JudgmentByAdmissionAppSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "judgment-by-admission-template-id";
    private static final String APPLICANT_SOL_EMAIL = "appsol@example.com";
    private static final String APPLICANT_LEGAL_ORG_NAME = "Applicant Legal Org";
    private static final String DEFENDANT = "Defendant Name";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    private JudgmentByAdmissionAppSolOneEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new JudgmentByAdmissionAppSolOneEmailDTOGenerator(organisationService, notificationsProperties);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("claimant-judgment-by-admission-%s");
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(notificationsProperties.getNotifyClaimantLRJudgmentByAdmissionTemplate()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail(APPLICANT_SOL_EMAIL))
            .build();

        assertThat(generator.getEmailAddress(caseData)).isEqualTo(APPLICANT_SOL_EMAIL);
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseDataBuilder.builder().build();
        Map<String, String> properties = new HashMap<>();

        try (MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class)) {
            notificationUtilsMockedStatic
                .when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
                .thenReturn(APPLICANT_LEGAL_ORG_NAME);
            notificationUtilsMockedStatic
                .when(() -> NotificationUtils.getDefendantNameBasedOnCaseType(caseData))
                .thenReturn(DEFENDANT);

            Map<String, String> updated = generator.addCustomProperties(properties, caseData);

            assertThat(updated).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, APPLICANT_LEGAL_ORG_NAME);
            assertThat(updated).containsEntry(LEGAL_ORG_NAME, APPLICANT_LEGAL_ORG_NAME);
            assertThat(updated).containsEntry(DEFENDANT_NAME, DEFENDANT);
        }
    }
}


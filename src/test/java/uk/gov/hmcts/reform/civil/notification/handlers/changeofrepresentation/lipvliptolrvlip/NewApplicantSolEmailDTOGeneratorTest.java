package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_APPLICANT1;

@ExtendWith(MockitoExtension.class)
class NewApplicantSolEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NewApplicantSolEmailDTOGenerator generator;

    private static final String TEMPLATE_ID = "template-notice-of-change-applicant-lip-solicitor";

    @Test
    void shouldReturnTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getNoticeOfChangeApplicantLipSolicitorTemplate()).thenReturn(TEMPLATE_ID);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo(ClaimantLipNocHelper.REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1234567890123456L)
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr")
                            .individualLastName("Doe")
                            .individualFirstName("John")
                            .partyName("John Doe").build())

            .respondent1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mrs")
                            .individualLastName("Dan")
                            .individualFirstName("Jane")
                            .partyName("Jane Dan").build())
            .build();

        Map<String, String> initialProps = new HashMap<>();

        try (
            MockedStatic<NotificationUtils> notificationUtilsMockedStatic = mockStatic(NotificationUtils.class)
        ) {
            notificationUtilsMockedStatic
                .when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
                .thenReturn("Org Ltd");

            Map<String, String> result = generator.addCustomProperties(initialProps, caseData);

            assertThat(result).containsOnlyKeys(
                CLAIM_NUMBER, CLAIMANT_V_DEFENDANT, LEGAL_ORG_APPLICANT1, CLAIMANT_NAME
            );
            assertThat(result.get(CLAIM_NUMBER)).isEqualTo("1234567890123456");
            assertThat(result.get(CLAIMANT_V_DEFENDANT)).isEqualTo("Mr John Doe V Mrs Jane Dan");
            assertThat(result.get(LEGAL_ORG_APPLICANT1)).isEqualTo("Org Ltd");
            assertThat(result.get(CLAIMANT_NAME)).isEqualTo("Mr John Doe");

            notificationUtilsMockedStatic.verify(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService));
        }
    }
}

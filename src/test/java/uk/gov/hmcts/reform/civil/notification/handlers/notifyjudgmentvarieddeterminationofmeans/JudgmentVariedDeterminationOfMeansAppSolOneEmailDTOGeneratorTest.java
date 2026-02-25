package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;

@ExtendWith(MockitoExtension.class)
class JudgmentVariedDeterminationOfMeansAppSolOneEmailDTOGeneratorTest {

    private static final String APPLICANT_EMAIL = "solicitor@example.com";
    private static final String TEMPLATE_ID = "claimant-judgment-varied-template-id";
    public static final long CCD_CASE_REFERENCE = 12345L;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private JudgmentVariedDeterminationOfMeansAppSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("claimant-judgment-varied-determination-of-means-%s");
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        CaseData caseData = CaseData.builder()
                .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("solicitor@example.com"))
                .build();

        assertThat(generator.getEmailAddress(caseData)).isEqualTo(APPLICANT_EMAIL);
    }

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getNotifyClaimantJudgmentVariedDeterminationOfMeansTemplate()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldAddCustomPropertiesCorrectly() {
        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CCD_CASE_REFERENCE)
                .applicant1OrganisationPolicy(new OrganisationPolicy())
                .applicantSolicitor1ClaimStatementOfTruth(new StatementOfTruth().setName("Legal Org Name"))
                .applicant1Represented(YesOrNo.YES)
                .applicant1(Party.builder()
                        .companyName("Applicant 1")
                        .type(Party.Type.COMPANY)
                        .build())
                .respondent1(Party.builder()
                        .companyName("Respondent 1")
                        .type(Party.Type.COMPANY)
                        .build())
                .build();

        Map<String, String> properties = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        Map<String, String> expectedProps = Map.of(
                DEFENDANT_NAME, "Respondent 1",
                LEGAL_ORG_NAME, "Legal Org Name"
        );

        assertThat(result).containsAllEntriesOf(expectedProps);
    }
}
package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@ExtendWith(MockitoExtension.class)
class JudgmentVariedDeterminationOfMeansClaimantEmailDTOGeneratorTest {

    private static final String APPLICANT_LIP_EMAIL = "applicantLip@example.com";
    private static final String SOLICITOR_EMAIL = "solicitor@example.com";
    private static final String LIP_TEMPLATE_ID = "lip-template-id";
    private static final String BIL_LIP_TEMPLATE = "bilingual-lip-template-id";
    private static final String SOLICITOR_TEMPLATE = "sol-template-id";
    private static final String LEGACY_REF = "000DC001";
    private static final long CCD_REF = 12345L;
    public static final String CLAIMANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS = "claimant-judgment-varied-determination-of-means-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private JudgmentVariedDeterminationOfMeansClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(CLAIMANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS);
    }

    @Test
    void shouldNotifyWhenLiPEmailPresent() {
        CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder().partyEmail(APPLICANT_LIP_EMAIL).build())
                .build();

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotifyWhenSolicitorEmailPresent() {
        CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.YES)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(SOLICITOR_EMAIL).build())
                .build();

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldReturnCorrectAddress() {
        CaseData lip = CaseData.builder()
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder().partyEmail(APPLICANT_LIP_EMAIL).build())
                .build();
        assertThat(generator.getEmailAddress(lip)).isEqualTo(APPLICANT_LIP_EMAIL);

        CaseData sol = CaseData.builder()
                .applicant1Represented(YesOrNo.YES)
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(SOLICITOR_EMAIL).build())
                .build();
        assertThat(generator.getEmailAddress(sol)).isEqualTo(SOLICITOR_EMAIL);
    }

    @Test
    void shouldUseLipTemplate() {
        CaseData caseData = CaseData.builder().applicant1Represented(YesOrNo.NO).build();
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(LIP_TEMPLATE_ID);
        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(LIP_TEMPLATE_ID);
    }

    @Test
    void shouldUseBilingualLip() {
        CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(BIL_LIP_TEMPLATE);
        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(BIL_LIP_TEMPLATE);
    }

    @Test
    void shouldUseSolicitorTemplate() {
        when(notificationsProperties.getNotifyClaimantJudgmentVariedDeterminationOfMeansTemplate())
                .thenReturn(SOLICITOR_TEMPLATE);
        assertThat(generator.getEmailTemplateId(CaseData.builder().build()))
                .isEqualTo(SOLICITOR_TEMPLATE);
    }

    @Test
    void shouldAddLiPProperties() {
        CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().companyName("Applicant").partyEmail(APPLICANT_LIP_EMAIL).type(Party.Type.COMPANY).build())
                .respondent1(Party.builder().companyName("Respondent").partyEmail("respondent@example.com").type(Party.Type.COMPANY).build())
                .applicant1Represented(YesOrNo.NO)
                .legacyCaseReference(LEGACY_REF)
                .build();

        Map<String, String> props = generator.addProperties(caseData);
        assertThat(props).containsEntry(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        assertThat(props).containsEntry(CLAIM_REFERENCE_NUMBER, LEGACY_REF);
        assertThat(props).containsEntry(PARTY_NAME, "Applicant");
    }

    @Test
    void shouldAddSolicitorProperties() {
        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CCD_REF)
                .applicant1(Party.builder().partyEmail(APPLICANT_LIP_EMAIL).type(Party.Type.INDIVIDUAL).build())
                .respondent1(Party.builder().partyEmail("r@example.com").type(Party.Type.INDIVIDUAL).build())
                .applicant1Represented(YesOrNo.YES)
                .legacyCaseReference(LEGACY_REF)
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().build())
                .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder().build())
                .build();

        when(configuration.getOpeningHours()).thenReturn("HOURS");
        when(configuration.getHmctsSignature()).thenReturn("SIGNATURE");
        when(configuration.getPhoneContact()).thenReturn("PHONE");
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);

        Map<String, String> props = generator.addProperties(caseData);
        assertThat(props).containsEntry(CLAIM_REFERENCE_NUMBER, String.valueOf(CCD_REF));
        assertThat(props).containsEntry(CASEMAN_REF, LEGACY_REF);
        assertThat(props).containsEntry(PHONE_CONTACT, "PHONE");
    }

    @Test
    void shouldNotModifyCustomProperties() {
        Map<String, String> p = Map.of("k", "v");
        assertThat(generator.addCustomProperties(p, CaseData.builder().build())).isEqualTo(p);
    }
}

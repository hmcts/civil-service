package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGeneratorTest {

    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final long CCD_CASE_REFERENCE = 1234L;
    public static final String KEY_1 = "key1";
    public static final String VALUE_1 = "value1";
    public static final String SOLICITOR_NAME = "Solicitor Name";
    public static final String CASE_PROCEEDS_IN_CASEMAN_APPLICANT_NOTIFICATION = "case-proceeds-in-caseman-applicant-notification-%s";
    public static final String LIP_TEMPLATE = "lip-template";
    public static final String LIP_BI_TEMPLATE = "lip-bi-template";
    public static final String SOL_OFFLINE_TEMPLATE = "sol-offline-template";
    public static final String APP_SOL_1_EMAIL = "lip@example.com";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGenerator generator;

    @Test
    void getEmailAddress_returnsApplicant1Email_whenLipvLROneVOne() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLROneVOne()).thenReturn(true);
        when(caseData.getApplicant1Email()).thenReturn(APP_SOL_1_EMAIL);

        String email = generator.getEmailAddress(caseData);

        assertThat(email).isEqualTo(APP_SOL_1_EMAIL);
    }

    @Test
    void getEmailAddress_returnsSolicitorEmail_whenNotLipvLROneVOne() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLROneVOne()).thenReturn(false);
        when(caseData.getApplicantSolicitor1UserDetailsEmail()).thenReturn(APP_SOL_1_EMAIL);

        String email = generator.getEmailAddress(caseData);

        assertThat(email).isEqualTo(APP_SOL_1_EMAIL);
    }

    @Test
    void getEmailTemplateId_returnsLipTemplate_whenLipvLROneVOne_nonBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLROneVOne()).thenReturn(true);
        when(caseData.isClaimantBilingual()).thenReturn(false);
        when(notificationsProperties.getClaimantLipClaimUpdatedTemplate()).thenReturn(LIP_TEMPLATE);

        String template = generator.getEmailTemplateId(caseData);

        assertThat(template).isEqualTo(LIP_TEMPLATE);
    }

    @Test
    void getEmailTemplateId_returnsLipBilingualTemplate_whenLipvLROneVOne_bilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLROneVOne()).thenReturn(true);
        when(caseData.isClaimantBilingual()).thenReturn(true);
        when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate())
                .thenReturn(LIP_BI_TEMPLATE);

        String template = generator.getEmailTemplateId(caseData);

        assertThat(template).isEqualTo(LIP_BI_TEMPLATE);
    }

    @Test
    void getEmailTemplateId_returnsSolicitorTemplate_whenNotLipvLROneVOne() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLROneVOne()).thenReturn(false);
        when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn(SOL_OFFLINE_TEMPLATE);

        String template = generator.getEmailTemplateId(caseData);

        assertThat(template).isEqualTo(SOL_OFFLINE_TEMPLATE);
    }

    @Test
    void getReferenceTemplate_isCorrect() {
        String refTpl = generator.getReferenceTemplate();

        assertThat(refTpl)
                .isEqualTo(CASE_PROCEEDS_IN_CASEMAN_APPLICANT_NOTIFICATION);
    }

    @Test
    void addProperties_returnsOnlyLipProps_whenLipvLROneVOne() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLROneVOne()).thenReturn(true);
        when(caseData.getCcdCaseReference()).thenReturn(CCD_CASE_REFERENCE);

        Party p = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("John")
                .individualLastName("Doe")
                .build();
        when(caseData.getApplicant1()).thenReturn(p);

        Map<String, String> props = generator.addProperties(caseData);

        assertThat(props).containsExactlyInAnyOrderEntriesOf(Map.of(
                NotificationData.CLAIM_REFERENCE_NUMBER, "1234",
                NotificationData.CLAIMANT_NAME, "John Doe"
        ));
    }

    @Test
    void addCustomProperties_preservesExistingEntries_forNonLipvLROneVOne() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLROneVOne()).thenReturn(false);

        OrganisationPolicy organisationPolicy = mock(OrganisationPolicy.class);
        Organisation organisation = mock(Organisation.class);
        when(caseData.getApplicant1OrganisationPolicy()).thenReturn(organisationPolicy);
        when(organisationPolicy.getOrganisation()).thenReturn(organisation);

        StatementOfTruth statementOfTruth = mock(StatementOfTruth.class);
        when(caseData.getApplicantSolicitor1ClaimStatementOfTruth()).thenReturn(statementOfTruth);
        when(statementOfTruth.getName()).thenReturn(SOLICITOR_NAME);

        Map<String, String> base = Map.of(KEY_1, VALUE_1);
        Map<String, String> props = generator.addCustomProperties(new java.util.HashMap<>(base), caseData);

        assertThat(props).containsEntry(KEY_1, VALUE_1);
    }

    @Test
    void addCustomProperties_returnsPropertiesUnchanged_whenLipvLROneVOne() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isLipvLROneVOne()).thenReturn(true);

        Map<String, String> baseProperties = Map.of(KEY_1, VALUE_1);
        Map<String, String> result = generator.addCustomProperties(new HashMap<>(baseProperties), caseData);

        assertThat(result).isEqualTo(baseProperties);
    }

    @Test
    void addProperties_returnsSuperProperties_whenCaseIsNotLipvLROneVOne() {
        Party applicant = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Jane")
                .individualLastName("Doe")
                .build();

        CaseData cd = CaseData.builder()
                .ccdCaseReference(CCD_CASE_REFERENCE)
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .applicant1(applicant)
                .build();

        Map<String, String> props = generator.addProperties(cd);

        assertThat(props)
                .hasSize(3)
                .containsEntry(NotificationData.CLAIM_REFERENCE_NUMBER, "1234")
                .containsEntry(NotificationData.CASEMAN_REF,          LEGACY_CASE_REFERENCE)
                .containsKey(NotificationData.PARTY_REFERENCES);
    }
}

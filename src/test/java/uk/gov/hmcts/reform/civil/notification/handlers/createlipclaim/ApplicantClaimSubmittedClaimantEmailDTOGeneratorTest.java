package uk.gov.hmcts.reform.civil.notification.handlers.createlipclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantClaimSubmittedClaimantEmailDTOGeneratorTest {

    public static final String APPLICANT1_EMAIL = "test@example.com";
    public static final String CLAIM_SUBMITTED_NOTIFICATION = "claim-submitted-notification-%s";
    public static final String TEMPLATE_HWF = "template-hwf";
    public static final String TEMPLATE_BILINGUAL = "template-bilingual";
    public static final String CLAIMANT_NAME = "claimantName";
    public static final String DEFENDANT_NAME = "DefendantName";
    public static final String BILINGUAL_HWF_TEMPLATE = "bilingual-hwf-template";
    public static final String HELP_WITH_FEES_REFERENCE_NUMBER = "1111";
    public static final String PAY_CLAIM_FEE_TEMPLATE = "pay-claim-fee-template";
    public static final String URL = "http://frontend.url";
    public static final String FRONTEND_BASE_URL = "frontendBaseUrl";

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ApplicantClaimSubmittedClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnApplicant1Email_whenGetEmailAddressIsCalled() {
        CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().partyEmail(APPLICANT1_EMAIL).build())
                .build();

        String emailAddress = generator.getEmailAddress(caseData);

        assertThat(emailAddress).isEqualTo(APPLICANT1_EMAIL);
    }

    @Test
    void shouldReturnReferenceTemplate_whenGetReferenceTemplateIsCalled() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(CLAIM_SUBMITTED_NOTIFICATION);
    }

    @Test
    void shouldReturnTrue_whenAllConditionsForNotificationAreMet() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.getApplicant1Email()).thenReturn(APPLICANT1_EMAIL);

        boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldReturnFalse_whenLipvLipOneVOneIsFalseAndEmailIsNull() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.isLipvLipOneVOne()).thenReturn(false);

        boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldReturnFalse_whenEmailIsNullAndLipvLipOneVOneIsTrue() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.getApplicant1Email()).thenReturn(null);

        boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldReturnFalse_whenEmailIsNullAndToggleIsDisabled() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.isLipvLipOneVOne()).thenReturn(true);

        boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenHWFReferenceNumberIsPresent() {
        CaseData caseData = CaseData.builder()
                .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber(HELP_WITH_FEES_REFERENCE_NUMBER).build()).build())
                .build();

        when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeTemplate())
                .thenReturn(TEMPLATE_HWF);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_HWF);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantIsBilingual() {
        CaseData caseData = CaseData.builder()
                .claimantBilingualLanguagePreference(Language.WELSH.name())
                .build();

        when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeBilingualTemplate())
                .thenReturn(TEMPLATE_BILINGUAL);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_BILINGUAL);
    }

    @Test
    void shouldAddPropertiesCorrectly() {
        CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("John").individualLastName("Doe").build())
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("Jane").individualLastName("Smith").build())
                .build();

        when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn(URL);

        Map<String, String> properties = generator.addProperties(caseData);

        assertThat(properties).containsExactlyInAnyOrderEntriesOf(Map.of(
                CLAIMANT_NAME, "John Doe",
                DEFENDANT_NAME, "Jane Smith",
                FRONTEND_BASE_URL, URL
        ));
    }

    @Test
    void shouldReturnSamePropertiesMap() {
        Map<String, String> properties = Map.of("key1", "value1", "key2", "value2");
        CaseData caseData = CaseData.builder().build();

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).isSameAs(properties);
    }

    @Test
    void shouldReturnBilingualTemplateId_whenClaimantIsBilingualAndHWFReferenceNumberIsPresent() {
        CaseData caseData = CaseData.builder()
                .claimantBilingualLanguagePreference(Language.WELSH.name())
                .caseDataLiP(CaseDataLiP.builder()
                        .helpWithFees(HelpWithFees.builder()
                                .helpWithFeesReferenceNumber(HELP_WITH_FEES_REFERENCE_NUMBER)
                                .build())
                        .build())
                .build();

        when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeBilingualTemplate())
                .thenReturn(BILINGUAL_HWF_TEMPLATE);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(BILINGUAL_HWF_TEMPLATE);
    }

    @Test
    void shouldReturnPayClaimFeeTemplateId_whenClaimantIsNotBilingualAndNoHWFReferenceNumber() {
        CaseData caseData = CaseData.builder()
                .claimantBilingualLanguagePreference(null)
                .caseDataLiP(CaseDataLiP.builder()
                        .helpWithFees(HelpWithFees.builder()
                                .helpWithFeesReferenceNumber(null)
                                .build())
                        .build())
                .build();

        when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeTemplate())
                .thenReturn(PAY_CLAIM_FEE_TEMPLATE);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(PAY_CLAIM_FEE_TEMPLATE);
    }
}
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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotifyApplicantClaimSubmittedEmailDTOGeneratorTest {

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private NotifyApplicantClaimSubmittedEmailDTOGenerator generator;

    @Test
    void shouldReturnApplicant1Email_whenGetEmailAddressIsCalled() {
        CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().partyEmail("test@example.com").build())
                .build();

        String emailAddress = generator.getEmailAddress(caseData);

        assertThat(emailAddress).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnReferenceTemplate_whenGetReferenceTemplateIsCalled() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("claim-submitted-notification-%s");
    }

    @Test
    void shouldReturnTrue_whenAllConditionsForNotificationAreMet() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.getApplicant1Email()).thenReturn("test@example.com");
        when(toggleService.isLipVLipEnabled()).thenReturn(true);

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
        when(toggleService.isLipVLipEnabled()).thenReturn(true);

        boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldReturnFalse_whenEmailIsNullAndToggleIsDisabled() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(toggleService.isLipVLipEnabled()).thenReturn(false);

        boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenHWFReferenceNumberIsPresent() {
        CaseData caseData = CaseData.builder()
                .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("1111").build()).build())
                .build();

        when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeTemplate())
                .thenReturn("template-hwf");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("template-hwf");
    }

    @Test
    void shouldReturnCorrectEmailTemplateId_whenClaimantIsBilingual() {
        CaseData caseData = CaseData.builder()
                .claimantBilingualLanguagePreference(Language.WELSH.name())
                .build();

        when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndPayClaimFeeBilingualTemplate())
                .thenReturn("template-bilingual");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("template-bilingual");
    }

    @Test
    void shouldAddCustomPropertiesCorrectly() {
        CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().companyName("Claimant Name").type(Party.Type.COMPANY).build())
                .respondent1(Party.builder().companyName("Defendant Name").type(Party.Type.COMPANY).build())
                .build();

        when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("http://frontend.url");

        Map<String, String> initialProperties = new HashMap<>();

        Map<String, String> properties = generator.addCustomProperties(initialProperties, caseData);

        assertThat(properties).containsEntry("claimantName", "Claimant Name");
        assertThat(properties).containsEntry("DefendantName", "Defendant Name");
        assertThat(properties).containsEntry("frontendBaseUrl", "http://frontend.url");
    }

    @Test
    void shouldReturnBilingualTemplateId_whenClaimantIsBilingualAndHWFReferenceNumberIsPresent() {
        CaseData caseData = CaseData.builder()
                .claimantBilingualLanguagePreference(Language.WELSH.name())
                .caseDataLiP(CaseDataLiP.builder()
                        .helpWithFees(HelpWithFees.builder()
                                .helpWithFeesReferenceNumber("1111")
                                .build())
                        .build())
                .build();

        when(notificationsProperties.getNotifyLiPClaimantClaimSubmittedAndHelpWithFeeBilingualTemplate())
                .thenReturn("bilingual-hwf-template");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("bilingual-hwf-template");
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
                .thenReturn("pay-claim-fee-template");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("pay-claim-fee-template");
    }
}
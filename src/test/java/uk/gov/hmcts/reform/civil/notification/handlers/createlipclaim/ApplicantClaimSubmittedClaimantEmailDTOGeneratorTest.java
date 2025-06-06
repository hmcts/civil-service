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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;

@ExtendWith(MockitoExtension.class)
class ApplicantClaimSubmittedClaimantEmailDTOGeneratorTest {

    public static final String CLAIM_SUBMITTED_NOTIFICATION = "claim-submitted-notification-%s";
    public static final String TEMPLATE_HWF = "template-hwf";
    public static final String TEMPLATE_BILINGUAL = "template-bilingual";
    public static final String CLAIMANT_NAME = "claimantName";
    public static final String DEFENDANT_NAME = "DefendantName";
    public static final String BILINGUAL_HWF_TEMPLATE = "bilingual-hwf-template";
    public static final String HELP_WITH_FEES_REFERENCE_NUMBER = "1111";
    public static final String PAY_CLAIM_FEE_TEMPLATE = "pay-claim-fee-template";

    @Mock
    private PinInPostConfiguration pinInPostConfiguration;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ApplicantClaimSubmittedClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnReferenceTemplate_whenGetReferenceTemplateIsCalled() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(CLAIM_SUBMITTED_NOTIFICATION);
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

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();

        CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().companyName("Claimant Name").type(Party.Type.COMPANY).build())
                .respondent1(Party.builder().companyName("Defendant Name").type(Party.Type.COMPANY).build())
                .build();

        when(pinInPostConfiguration.getCuiFrontEndUrl()).thenReturn("http://frontend.url");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsEntry(CLAIMANT_NAME, "Claimant Name");
        assertThat(result).containsEntry(DEFENDANT_NAME, "Defendant Name");
        assertThat(result).containsEntry(FRONTEND_URL, "http://frontend.url");
    }
}
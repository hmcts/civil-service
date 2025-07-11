package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@ExtendWith(MockitoExtension.class)
class JudgmentVariedDeterminationOfMeansClaimantEmailDTOGeneratorTest {

    private static final String APPLICANT_LIP_EMAIL = "applicantLip@example.com";
    private static final String LIP_TEMPLATE_ID = "lip-template-id";
    private static final String BIL_LIP_TEMPLATE = "bilingual-lip-template-id";
    private static final String LEGACY_REF = "000DC001";
    public static final String CLAIMANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS = "claimant-judgment-varied-determination-of-means-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private JudgmentVariedDeterminationOfMeansClaimantEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(CLAIMANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS);
    }

    @Test
    void shouldReturnCorrectAddress() {
        CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.NO)
                .applicant1(Party.builder().partyEmail(APPLICANT_LIP_EMAIL).build())
                .build();
        assertThat(generator.getEmailAddress(caseData)).isEqualTo(APPLICANT_LIP_EMAIL);
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
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder()
                .applicant1(Party.builder().companyName("Applicant").partyEmail(APPLICANT_LIP_EMAIL).type(Party.Type.COMPANY).build())
                .respondent1(Party.builder().companyName("Respondent").partyEmail("respondent@example.com").type(Party.Type.COMPANY).build())
                .applicant1Represented(YesOrNo.NO)
                .legacyCaseReference(LEGACY_REF)
                .build();

        Map<String, String> properties = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        Map<String, String> expectedProps = Map.of(
                CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData),
                CLAIM_REFERENCE_NUMBER, LEGACY_REF,
                PARTY_NAME, "Applicant"
        );

        assertThat(result).containsAllEntriesOf(expectedProps);
    }
}

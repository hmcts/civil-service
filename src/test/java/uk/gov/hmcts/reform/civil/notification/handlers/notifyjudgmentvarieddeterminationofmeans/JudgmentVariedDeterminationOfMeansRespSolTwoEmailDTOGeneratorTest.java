package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;

@ExtendWith(MockitoExtension.class)
class JudgmentVariedDeterminationOfMeansRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final long CLAIM_REF  = 12345L;
    private static final String LEGACY_REF = "000DC001";
    public static final String DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS = "defendant-judgment-varied-determination-of-means-%s";
    public static final String RESPONDENT_SOLICITOR2_EMAIL = "solicitor2@example.com";
    public static final String SIGNATURE = "SIG";
    public static final String PHONE = "PHONE";
    public static final String HOURS = "HOURS";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private NotificationsSignatureConfiguration signatureConfiguration;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private JudgmentVariedDeterminationOfMeansRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnTemplateIdAndReferenceTemplate() {
        when(notificationsProperties.getNotifyDefendantJudgmentVariedDeterminationOfMeansTemplate())
                .thenReturn(TEMPLATE_ID);

        CaseData cd = CaseData.builder().build();
        assertThat(generator.getEmailTemplateId(cd)).isEqualTo(TEMPLATE_ID);
        assertThat(generator.getReferenceTemplate()).isEqualTo(DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS);
    }

    @Test
    void shouldReturnTrueWhenAllConditionsAreMet() {
        CaseData caseData = CaseData.builder()
                .respondentSolicitor2EmailAddress(RESPONDENT_SOLICITOR2_EMAIL)
                .respondent1Represented(YesOrNo.YES)
                .respondent2Represented(YesOrNo.YES)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenRespondentSolicitor2EmailIsNull() {
        CaseData caseData = CaseData.builder()
                .respondentSolicitor2EmailAddress(null)
                .respondent1Represented(YesOrNo.YES)
                .respondent2Represented(YesOrNo.YES)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenRespondent1IsNotRepresented() {
        CaseData caseData = CaseData.builder()
                .respondentSolicitor2EmailAddress(RESPONDENT_SOLICITOR2_EMAIL)
                .respondent1Represented(YesOrNo.NO)
                .respondent2Represented(YesOrNo.YES)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenRespondent2IsNotRepresented() {
        CaseData caseData = CaseData.builder()
                .respondentSolicitor2EmailAddress(RESPONDENT_SOLICITOR2_EMAIL)
                .respondent1Represented(YesOrNo.YES)
                .respondent2Represented(YesOrNo.NO)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldAddCustomPropertiesCorrectly() {
        when(signatureConfiguration.getHmctsSignature()).thenReturn(SIGNATURE);
        when(signatureConfiguration.getPhoneContact()).thenReturn(PHONE);
        when(signatureConfiguration.getOpeningHours()).thenReturn(HOURS);
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);

        CaseData cd = CaseData.builder()
                .ccdCaseReference(CLAIM_REF)
                .legacyCaseReference(LEGACY_REF)
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().build())
                .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> result = generator.addCustomProperties(properties, cd);

        assertThat(result).containsEntry(CLAIM_REFERENCE_NUMBER, String.valueOf(CLAIM_REF));
        assertThat(result).containsEntry(CASEMAN_REF, LEGACY_REF);
        assertThat(result).containsEntry(HMCTS_SIGNATURE, SIGNATURE);
        assertThat(result).containsEntry(PHONE_CONTACT, PHONE);
        assertThat(result).containsEntry(OPENING_HOURS, HOURS);
        assertThat(result).containsKey(SPEC_UNSPEC_CONTACT);
    }
}

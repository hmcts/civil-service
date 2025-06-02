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
class JudgmentVariedDeterminationOfMeansRespSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final long CLAIM_REF  = 12345L;
    private static final String LEGACY_REF = "000DC001";
    public static final String DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS = "defendant-judgment-varied-determination-of-means-%s";
    public static final String RESPONDENT_SOLICITOR_EMAIL = "solicitor@example.com";
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
    private JudgmentVariedDeterminationOfMeansRespSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnTemplateIdAndReferenceTemplate() {
        when(notificationsProperties.getNotifyDefendantJudgmentVariedDeterminationOfMeansTemplate())
                .thenReturn(TEMPLATE_ID);

        CaseData caseData = CaseData.builder().build();
        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
        assertThat(generator.getReferenceTemplate()).isEqualTo(DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS);
    }

    @Test
    void shouldReturnTrueWhenRespondentSolicitorEmailExistsAndRespondentIsRepresented() {
        CaseData caseData = CaseData.builder()
                .respondentSolicitor1EmailAddress(RESPONDENT_SOLICITOR_EMAIL)
                .respondent1Represented(YesOrNo.YES)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenRespondentSolicitorEmailIsNull() {
        CaseData caseData = CaseData.builder()
                .respondentSolicitor1EmailAddress(null)
                .respondent1Represented(YesOrNo.YES)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenRespondentIsNotRepresented() {
        CaseData caseData = CaseData.builder()
                .respondentSolicitor1EmailAddress(RESPONDENT_SOLICITOR_EMAIL)
                .respondent1Represented(YesOrNo.NO)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldAddCustomPropertiesCorrectly() {
        when(signatureConfiguration.getHmctsSignature()).thenReturn(SIGNATURE);
        when(signatureConfiguration.getPhoneContact()).thenReturn(PHONE);
        when(signatureConfiguration.getOpeningHours()).thenReturn(HOURS);
        when(featureToggleService.isQueryManagementLRsEnabled()).thenReturn(true);

        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CLAIM_REF)
                .legacyCaseReference(LEGACY_REF)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        Map<String, String> expectedProps = Map.of(
                CLAIM_REFERENCE_NUMBER, String.valueOf(CLAIM_REF),
                CASEMAN_REF, LEGACY_REF,
                HMCTS_SIGNATURE, SIGNATURE,
                PHONE_CONTACT, PHONE,
                OPENING_HOURS, HOURS
        );

        assertThat(result).containsAllEntriesOf(expectedProps);
        assertThat(result).containsKey(SPEC_UNSPEC_CONTACT);
    }
}

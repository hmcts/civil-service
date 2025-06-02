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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@ExtendWith(MockitoExtension.class)
class JudgmentVariedDeterminationOfMeansLipDefendantEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String LEGACY_REF = "000DC001";
    public static final String DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS = "defendant-judgment-varied-determination-of-means-%s";
    public static final String RESPONDENT_EMAIL = "respondent@example.com";
    public static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";
    public static final String APPLICANT_EMAIL = "applicant@example.com";
    public static final String APPLICANT_NAME = "Applicant";
    public static final String RESPONDENT_NAME = "Respondent";
    public static final String CLAIMANTVDEFENDANT = "claimantvdefendant";
    public static final String APPLICANT_V_RESPONDENT = "Applicant V Respondent";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private JudgmentVariedDeterminationOfMeansLipDefendantEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS);
    }

    @Test
    void shouldReturnTrueWhenRespondentIsLiPAndEmailIsPresent() {
        CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().partyEmail(RESPONDENT_EMAIL).build())
                .respondent1Represented(YesOrNo.NO)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenRespondentEmailIsNull() {
        CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().partyEmail(null).build())
                .respondent1Represented(YesOrNo.NO)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldReturnLipTemplateIdWhenClaimantIsBilingual() {
        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
        caseData = caseData.toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                        .respondent1LiPResponse(RespondentLiPResponse.builder()
                                .respondent1ResponseLanguage(Language.BOTH.toString()).build())
                        .build())
                .build();

        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(BILINGUAL_TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(BILINGUAL_TEMPLATE_ID);
    }

    @Test
    void shouldReturnFalseWhenRespondentIsRepresented() {
        CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().partyEmail(RESPONDENT_EMAIL).build())
                .respondent1Represented(YesOrNo.YES)
                .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldReturnRespondent1EmailAddress() {
        CaseData caseData = CaseData.builder()
                .respondent1(Party.builder().partyEmail(RESPONDENT_EMAIL).build())
                .build();

        assertThat(generator.getEmailAddress(caseData)).isEqualTo(RESPONDENT_EMAIL);
    }

    @Test
    void shouldNotifyOnlyWhenLiPAndEmailPresent() {
        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
        caseData = caseData.toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .respondent1(Party.builder().partyEmail(RESPONDENT_EMAIL).build())
                .legacyCaseReference(LEGACY_REF)
                .build();
        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldPickCorrectTemplateAndProperties() {
        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithDeterminationMeans();
        caseData = caseData.toBuilder()
                .applicant1(Party.builder().partyEmail(APPLICANT_EMAIL).companyName(APPLICANT_NAME).type(Party.Type.COMPANY).build())
                .respondent1Represented(YesOrNo.NO)
                .respondent1(Party.builder().partyEmail(RESPONDENT_EMAIL).companyName(RESPONDENT_NAME).type(Party.Type.COMPANY).build())
                .legacyCaseReference(LEGACY_REF)
                .build();
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);

        Map<String, String> expectedProps = Map.of(
                CLAIMANTVDEFENDANT, APPLICANT_V_RESPONDENT,
                CLAIM_REFERENCE_NUMBER, LEGACY_REF,
                PARTY_NAME, RESPONDENT_NAME
        );

        Map<String, String> props = generator.addProperties(caseData);
        assertThat(props).containsExactlyInAnyOrderEntriesOf(expectedProps);
    }

    @Test
    void shouldNotModifyCustomProperties() {
        Map<String, String> properties = Map.of("a", "b");
        assertThat(generator.addCustomProperties(properties, CaseData.builder().build())).isEqualTo(properties);
    }
}

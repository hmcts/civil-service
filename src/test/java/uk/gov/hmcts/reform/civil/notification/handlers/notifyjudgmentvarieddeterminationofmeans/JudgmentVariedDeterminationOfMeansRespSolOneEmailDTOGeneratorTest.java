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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
class JudgmentVariedDeterminationOfMeansRespSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final long CLAIM_REF  = 12345L;
    public static final String DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS = "defendant-judgment-varied-determination-of-means-%s";
    public static final String RESPONDENT_SOLICITOR_EMAIL = "solicitor@example.com";

    @Mock
    private NotificationsProperties notificationsProperties;

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
        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CLAIM_REF)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent1Represented(YesOrNo.YES)
                .build();

        Map<String, String> properties = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        Map<String, String> expectedProps = Map.of(
                CLAIM_REFERENCE_NUMBER, String.valueOf(CLAIM_REF)
        );

        assertThat(result).containsAllEntriesOf(expectedProps);
    }
}

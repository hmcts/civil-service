package uk.gov.hmcts.reform.civil.notification.handlers.djnondivergent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;

@ExtendWith(MockitoExtension.class)
class DjNonDivergentDefendant1LipEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private DjNonDivergentDefendant1LipEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenBilingual() {
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                new CaseDataLiP()
                    .setRespondent1LiPResponse(
                        new RespondentLiPResponse()
                            .setRespondent1ResponseLanguage(BOTH.toString())
                    )
            ).build();
        String expectedTemplateId = "notify-lip-update-template-bilingual";
        when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenNotBilingual() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "notify-lip-update-template";
        when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("dj-non-divergent-defendant-notification-lip-%s");
    }

    @Test
    void shouldReturnFalseForShouldNotify() {
        CaseData caseData = CaseData.builder().build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

}

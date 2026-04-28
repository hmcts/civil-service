package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestJudgementByAdmissionLipRespondentEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private RequestJudgementByAdmissionHelper helper;

    @InjectMocks
    private RequestJudgementByAdmissionLipRespondentEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("request-judgement-by-admission-respondent-notification-%s");
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyRespondentLipRequestJudgementByAdmissionNotificationTemplate())
            .thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldNotifyWhenLipvLipOneVOne() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyWhenApplicantIsRepresented() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.NO)
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenRespondentIsRepresented() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.YES)
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldDelegateAddCustomPropertiesToHelper() {
        CaseData caseData = CaseData.builder().build();
        Map<String, String> properties = new HashMap<>();

        emailDTOGenerator.addCustomProperties(properties, caseData);

        verify(helper).addLipProperties(eq(properties), eq(caseData));
    }
}

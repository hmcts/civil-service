package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FRONTEND_URL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@ExtendWith(MockitoExtension.class)
public class RequestJudgementByAdmissionLipRespondentEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private PinInPostConfiguration pipInPostConfiguration;

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
    void shouldAddCustomProperties() {
        when(pipInPostConfiguration.getCuiFrontEndUrl()).thenReturn("dummy_cui_front_end_url");

        Party applicant = new Party()
            .setType(Party.Type.COMPANY)
            .setCompanyName("Applicant Company");
        Party respondent = new Party()
            .setType(Party.Type.COMPANY)
            .setCompanyName("Respondent Company");

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .respondent1(respondent)
            .legacyCaseReference("000DC001")
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties).containsEntry(CLAIMANT_NAME, "Applicant Company");
        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, "Respondent Company");
        assertThat(updatedProperties).containsEntry(CLAIM_REFERENCE_NUMBER, "000DC001");
        assertThat(updatedProperties).containsEntry(FRONTEND_URL, "dummy_cui_front_end_url");
    }
}

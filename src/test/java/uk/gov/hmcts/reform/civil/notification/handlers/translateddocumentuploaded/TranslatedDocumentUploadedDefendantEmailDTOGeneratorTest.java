package uk.gov.hmcts.reform.civil.notification.handlers.translateddocumentuploaded;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@ExtendWith(MockitoExtension.class)
public class TranslatedDocumentUploadedDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private TranslatedDocumentUploadedDefendantEmailDTOGenerator emailDTOGenerator;

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
        String expectedTemplateId = "template-id-bilingual";

        when(notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenNotBilingual() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id-english";

        when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("translated-document-uploaded-defendant-notification-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        Party applicant = Party.builder()
            .type(Party.Type.COMPANY)
            .companyName("Applicant Company")
            .build();
        Party respondent = Party.builder()
            .type(Party.Type.COMPANY)
            .companyName("Respondent Company")
            .build();
        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .respondent1(respondent)
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, "Respondent Company");
    }
}

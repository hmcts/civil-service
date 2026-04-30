package uk.gov.hmcts.reform.civil.notification.handlers.translateddocumentuploaded;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

@ExtendWith(MockitoExtension.class)
public class TranslatedDocumentUploadedClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private TranslatedDocumentUploadedClaimantEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("translated-document-uploaded-claimant-notification-%s");
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenBilingual() {
        CaseData caseData = CaseData.builder().claimantBilingualLanguagePreference(BOTH.toString()).build();
        String expectedTemplateId = "template-id-bilingual";

        when(notificationsProperties.getNotifyLiPClaimantDefendantRespondedWelshLip()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenNotBilingual() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id-english";

        when(notificationsProperties.getNotifyLiPClaimantDefendantResponded()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldAddCustomProperties() {
        Party applicant = new Party()
            .setType(Party.Type.COMPANY)
            .setCompanyName("Applicant Company");
        Party respondent = new Party()
            .setType(Party.Type.COMPANY)
            .setCompanyName("Respondent Company");
        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .respondent1(respondent)
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties).containsEntry(CLAIMANT_NAME, "Applicant Company");
        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, "Respondent Company");
    }
}

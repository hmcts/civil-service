package uk.gov.hmcts.reform.civil.notification.handlers.translatedordernoticeuploaded;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@ExtendWith(MockitoExtension.class)
public class TranslatedOrderNoticeUploadedClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private TranslatedOrderNoticeUploadedClaimantEmailDTOGenerator emailDTOGenerator;

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("translated-order-notice-uploaded-claimant-notification-%s");
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyLiPOrderTranslatedTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldNotifyWhenClaimantIsLipAndBilingual() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference("BOTH")
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyWhenClaimantIsRepresented() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.YES)
            .claimantBilingualLanguagePreference("BOTH")
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyWhenClaimantIsNotBilingual() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference("ENGLISH")
            .build();

        assertThat(emailDTOGenerator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldAddCustomProperties() {
        Party applicant = new Party()
            .setType(Party.Type.COMPANY)
            .setCompanyName("Applicant Company");
        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties).containsEntry(PARTY_NAME, "Applicant Company");
    }
}

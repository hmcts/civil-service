package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimantLipEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantLipEmailDTOGenerator claimantLipEmailDTOGenerator;

    private static final String TEMPLATE_ENGLISH = "template-id-english";
    private static final String TEMPLATE_WELSH = "template-id-welsh";

    @Test
    void shouldReturnEnglishTemplateIdWhenNotBilingual() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference(Language.ENGLISH.getDisplayedValue())
            .build();

        when(notificationsProperties.getNotifyClaimantLipForNoLongerAccessTemplate()).thenReturn(TEMPLATE_ENGLISH);

        String templateId = claimantLipEmailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ENGLISH);
    }

    @Test
    void shouldReturnWelshTemplateIdWhenBilingual() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference(Language.WELSH.getDisplayedValue())
            .build();

        when(notificationsProperties.getNotifyClaimantLipForNoLongerAccessWelshTemplate()).thenReturn(TEMPLATE_WELSH);

        String templateId = claimantLipEmailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_WELSH);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String reference = claimantLipEmailDTOGenerator.getReferenceTemplate();
        assertThat(reference).isEqualTo(ClaimantLipNocHelper.REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder().build();
        Map<String, String> initialProps = new HashMap<>();
        Map<String, String> lipProps = Map.of("key1", "value1");

        try (MockedStatic<ClaimantLipNocHelper> mockedStatic = mockStatic(ClaimantLipNocHelper.class)) {
            mockedStatic.when(() -> ClaimantLipNocHelper.getLipProperties(caseData)).thenReturn(lipProps);

            Map<String, String> result = claimantLipEmailDTOGenerator.addCustomProperties(initialProps, caseData);

            assertThat(result)
                .containsEntry("key1", "value1")
                .hasSize(1);

            mockedStatic.verify(() -> ClaimantLipNocHelper.getLipProperties(caseData), times(1));
        }
    }
}

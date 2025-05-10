package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantLipEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private DefendantLipEmailDTOGenerator defendantLipEmailDTOGenerator;

    private static final String TEMPLATE_ID = "defendant-lip-notify-template";

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(notificationsProperties.getNotifyRespondentLipForClaimantRepresentedTemplate()).thenReturn(TEMPLATE_ID);

        String result = defendantLipEmailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String reference = defendantLipEmailDTOGenerator.getReferenceTemplate();

        assertThat(reference).isEqualTo(ClaimantLipNocHelper.REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomPropertiesUsingHelper() {
        CaseData caseData = CaseData.builder().build();
        Map<String, String> baseProperties = new HashMap<>();
        Map<String, String> helperProperties = Map.of("respondentName", "Jane Doe");

        try (MockedStatic<ClaimantLipNocHelper> mockedStatic = mockStatic(ClaimantLipNocHelper.class)) {
            mockedStatic.when(() -> ClaimantLipNocHelper.getLipProperties(caseData)).thenReturn(helperProperties);

            Map<String, String> result = defendantLipEmailDTOGenerator.addCustomProperties(baseProperties, caseData);

            assertThat(result).containsExactlyEntriesOf(helperProperties);
            mockedStatic.verify(() -> ClaimantLipNocHelper.getLipProperties(caseData), times(1));
        }
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lrvlrlrandlipvlr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoCLipVLRNewDefendantEmailDTOGeneratorTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private NoCHelper noCHelper;

    @InjectMocks
    private NoCLipVLRNewDefendantEmailDTOGenerator generator;

    private final CaseData caseData = mock(CaseData.class);

    @Test
    void shouldNotify_WhenFeatureEnabledAndApplicantIsLip() {
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);
        when(noCHelper.isApplicantLipForRespondentSolicitorChange(caseData)).thenReturn(true);

        assertTrue(generator.getShouldNotify(caseData));
    }

    @Test
    void shouldNotNotify_WhenFeatureDisabled() {
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(false);
        assertFalse(generator.getShouldNotify(caseData));
    }

    @Test
    void shouldNotNotify_WhenApplicantIsNotLip() {
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);
        when(noCHelper.isApplicantLipForRespondentSolicitorChange(caseData)).thenReturn(false);

        assertFalse(generator.getShouldNotify(caseData));
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        when(caseData.getRespondentSolicitor1EmailAddress()).thenReturn("solicitor@example.com");

        assertEquals("solicitor@example.com", generator.getEmailAddress(caseData));
    }

    @Test
    void shouldReturnCorrectTemplateId() {
        when(notificationsProperties.getNotifyNewDefendantSolicitorNOC()).thenReturn("template-id");

        assertEquals("template-id", generator.getEmailTemplateId(caseData));
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertEquals(NoCHelper.REFERENCE_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> baseProps = new HashMap<>();
        Map<String, String> additionalProps = Map.of("key", "value");

        when(noCHelper.getProperties(caseData, false)).thenReturn(additionalProps);

        Map<String, String> result = generator.addCustomProperties(baseProps, caseData);

        assertEquals("value", result.get("key"));
    }
}

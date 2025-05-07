package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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

    @Test
    void shouldNotify_WhenConditionsMet() {
        CaseData caseData = mock(CaseData.class);

        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);
        try (MockedStatic<NocNotificationUtils> mockedUtils = mockStatic(NocNotificationUtils.class)) {
            mockedUtils.when(() -> NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData))
                .thenReturn(true);

            assertTrue(generator.getShouldNotify(caseData));
        }
    }

    @Test
    void shouldNotNotify_WhenConditionsNotMet() {
        CaseData caseData = mock(CaseData.class);

        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(false);
        try (MockedStatic<NocNotificationUtils> mockedUtils = mockStatic(NocNotificationUtils.class)) {
            mockedUtils.when(() -> NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData))
                .thenReturn(false);

            assertFalse(generator.getShouldNotify(caseData));
        }
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getRespondentSolicitor1EmailAddress()).thenReturn("solicitor@example.com");

        assertEquals("solicitor@example.com", generator.getEmailAddress(caseData));
    }

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNotifyNewDefendantSolicitorNOC()).thenReturn("template-id");

        assertEquals("template-id", generator.getEmailTemplateId(caseData));
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertEquals(NoCHelper.REFERENCE_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = mock(CaseData.class);
        Map<String, String> initialProps = new HashMap<>();
        Map<String, String> customProps = Map.of("key", "value");

        when(noCHelper.getProperties(caseData, false)).thenReturn(customProps);

        Map<String, String> result = generator.addCustomProperties(initialProps, caseData);

        assertEquals("value", result.get("key"));
    }
}

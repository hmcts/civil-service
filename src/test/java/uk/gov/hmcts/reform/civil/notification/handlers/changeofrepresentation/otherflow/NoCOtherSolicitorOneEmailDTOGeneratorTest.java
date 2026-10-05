package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.common.NotificationHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;

@ExtendWith(MockitoExtension.class)
class NoCOtherSolicitorOneEmailDTOGeneratorTest {

    private static final String PARTY_REFERENCES_VALUE =
        "Claimant reference: ClaimantRef - Defendant reference: DefendantRef";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private NoCHelper noCHelper;

    @InjectMocks
    private NoCOtherSolicitorOneEmailDTOGenerator generator;

    @Test
    void shouldNotify_WhenOtherPartyIsNotLip() {
        CaseData caseData = mock(CaseData.class);
        try (MockedStatic<NotificationHelper> mockedStatic = mockStatic(NotificationHelper.class)) {
            mockedStatic.when(() -> NotificationHelper.isOtherParty1Lip(caseData))
                    .thenReturn(false);

            assertTrue(generator.getShouldNotify(caseData));
        }
    }

    @Test
    void shouldNotNotify_WhenOtherPartyIsLip() {
        CaseData caseData = mock(CaseData.class);
        try (MockedStatic<NotificationHelper> mockedStatic = mockStatic(NotificationHelper.class)) {
            mockedStatic.when(() -> NotificationHelper.isOtherParty1Lip(caseData))
                    .thenReturn(true);

            assertFalse(generator.getShouldNotify(caseData));
        }
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn("template-id");

        assertEquals("template-id", generator.getEmailTemplateId(caseData));
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        CaseData caseData = mock(CaseData.class);
        try (MockedStatic<NotificationHelper> mockedStatic = mockStatic(NotificationHelper.class)) {
            mockedStatic.when(() -> NotificationHelper.getOtherSolicitor1Email(caseData))
                    .thenReturn("other.solicitor@example.com");

            assertEquals("other.solicitor@example.com", generator.getEmailAddress(caseData));
        }
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertEquals(NoCHelper.REFERENCE_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = mock(CaseData.class);
        Map<String, String> initialProps = new HashMap<>();
        Map<String, String> additionalProps = Map.of("key", "value");

        when(noCHelper.getProperties(caseData, false)).thenReturn(additionalProps);
        when(noCHelper.getNoCPartyReferences(caseData)).thenReturn(PARTY_REFERENCES_VALUE);

        Map<String, String> result = generator.addCustomProperties(initialProps, caseData);

        assertEquals("value", result.get("key"));
    }

    @Test
    void shouldAddPartyReferencesIncludingTheOutgoingSolicitorReference() {
        CaseData caseData = mock(CaseData.class);
        when(noCHelper.getProperties(caseData, false)).thenReturn(Map.of("key", "value"));
        when(noCHelper.getNoCPartyReferences(caseData)).thenReturn(PARTY_REFERENCES_VALUE);

        Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

        assertEquals(PARTY_REFERENCES_VALUE, result.get(PARTY_REFERENCES));
    }
}

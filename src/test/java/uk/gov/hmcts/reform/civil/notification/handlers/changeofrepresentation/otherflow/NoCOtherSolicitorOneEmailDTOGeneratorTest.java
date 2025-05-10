package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoCOtherSolicitorOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private NoCHelper noCHelper;

    @InjectMocks
    private NoCOtherSolicitorOneEmailDTOGenerator generator;

    @Test
    void shouldNotify_WhenOtherPartyIsNotLip() {
        CaseData caseData = mock(CaseData.class);
        when(noCHelper.isOtherParty1Lip(caseData)).thenReturn(false);

        assertTrue(generator.getShouldNotify(caseData));
    }

    @Test
    void shouldNotNotify_WhenOtherPartyIsLip() {
        CaseData caseData = mock(CaseData.class);
        when(noCHelper.isOtherParty1Lip(caseData)).thenReturn(true);

        assertFalse(generator.getShouldNotify(caseData));
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
        when(noCHelper.getOtherSolicitor1Email(caseData)).thenReturn("other.solicitor@example.com");

        assertEquals("other.solicitor@example.com", generator.getEmailAddress(caseData));
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

        Map<String, String> result = generator.addCustomProperties(initialProps, caseData);

        assertEquals("value", result.get("key"));
    }
}

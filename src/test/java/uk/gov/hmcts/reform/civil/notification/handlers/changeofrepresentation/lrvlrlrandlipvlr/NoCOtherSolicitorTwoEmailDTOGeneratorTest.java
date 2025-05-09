package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lrvlrlrandlipvlr;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoCOtherSolicitorTwoEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private NoCHelper noCHelper;

    @InjectMocks
    private NoCOtherSolicitorTwoEmailDTOGenerator generator;

    private MockedStatic<MultiPartyScenario> multiPartyScenarioMock;

    @BeforeEach
    void setupStaticMocks() {
        multiPartyScenarioMock = mockStatic(MultiPartyScenario.class);
    }

    @AfterEach
    void closeStaticMocks() {
        multiPartyScenarioMock.close();
    }

    @Test
    void shouldNotify_whenBothConditionsAreFalse() {
        CaseData caseData = mock(CaseData.class);
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);
        when(noCHelper.isOtherParty2Lip(caseData)).thenReturn(false);

        assertTrue(generator.getShouldNotify(caseData));
    }

    @Test
    void shouldNotNotify_whenIsOneVTwoTwoLegalRepTrue() {
        CaseData caseData = mock(CaseData.class);
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);
        assertFalse(generator.getShouldNotify(caseData));
    }

    @Test
    void shouldNotNotify_whenOtherParty2LipIsTrue() {
        CaseData caseData = mock(CaseData.class);
        multiPartyScenarioMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);
        when(noCHelper.isOtherParty2Lip(caseData)).thenReturn(true);

        assertFalse(generator.getShouldNotify(caseData));
    }

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn("template-id");

        assertEquals("template-id", generator.getEmailTemplateId(caseData));
    }

    @Test
    void shouldReturnCorrectEmailAddress() {
        CaseData caseData = mock(CaseData.class);
        when(noCHelper.getOtherSolicitor2Email(caseData)).thenReturn("sol2@example.com");

        assertEquals("sol2@example.com", generator.getEmailAddress(caseData));
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertEquals(NoCHelper.REFERENCE_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomPropertiesCorrectly() {
        CaseData caseData = mock(CaseData.class);
        Map<String, String> existing = new HashMap<>();
        existing.put("existing", "value");

        Map<String, String> custom = Map.of("customKey", "customValue");
        when(noCHelper.getProperties(caseData, true)).thenReturn(custom);

        Map<String, String> result = generator.addCustomProperties(existing, caseData);

        assertEquals(2, result.size());
        assertEquals("value", result.get("existing"));
        assertEquals("customValue", result.get("customKey"));
    }
}

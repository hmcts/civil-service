package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyClaimRespOneSolEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotifyClaimHelper notifyClaimHelper;

    @InjectMocks
    private NotifyClaimRespOneSolEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        String templateId = "some-template-id";
        when(notifyClaimHelper.getNotifyClaimEmailTemplate()).thenReturn(templateId);

        String result = generator.getEmailTemplateId(caseData);

        assertEquals(templateId, result);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertEquals("notify-claim-notification-%s", generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomPropertiesFromHelper() {
        Map<String, String> baseProps = new HashMap<>();
        Map<String, String> customProps = Map.of("key", "value");

        when(notifyClaimHelper.retrieveCustomProperties(caseData)).thenReturn(customProps);

        Map<String, String> result = generator.addCustomProperties(baseProps, caseData);

        assertEquals(1, result.size());
        assertEquals("value", result.get("key"));
    }

    @Test
    void shouldReturnFalseWhenSolicitorIsNotRegistered() {
        when(caseData.isRespondentSolicitorRegistered()).thenReturn(false);

        boolean result = generator.getShouldNotify(caseData);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenMultipartyWithTwoLegalRepsAndCorrectRespondent() {
        Party respondent = mock(Party.class);
        when(respondent.getPartyName()).thenReturn("respondent name");
        when(caseData.getRespondent1()).thenReturn(respondent);
        when(caseData.isRespondentSolicitorRegistered()).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> staticMock = mockStatic(MultiPartyScenario.class)) {
            staticMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);
            when(notifyClaimHelper.checkIfThisDefendantToBeNotified(caseData, "respondent name")).thenReturn(true);

            boolean result = generator.getShouldNotify(caseData);

            assertTrue(result);
        }
    }

    @Test
    void shouldReturnTrueWhenNotMultipartyScenario() {
        Party respondent = mock(Party.class);
        when(respondent.getPartyName()).thenReturn("respondent name");
        when(caseData.getRespondent1()).thenReturn(respondent);
        when(caseData.isRespondentSolicitorRegistered()).thenReturn(true);

        try (MockedStatic<MultiPartyScenario> staticMock = mockStatic(MultiPartyScenario.class)) {
            staticMock.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

            boolean result = generator.getShouldNotify(caseData);

            assertTrue(result);
        }
    }
}

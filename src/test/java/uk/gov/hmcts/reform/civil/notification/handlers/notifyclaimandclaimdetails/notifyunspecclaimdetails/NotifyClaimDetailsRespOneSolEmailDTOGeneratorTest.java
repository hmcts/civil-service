package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

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
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@ExtendWith(MockitoExtension.class)
class NotifyClaimDetailsRespOneSolEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotifyClaimDetailsHelper notifyClaimDetailsHelper;

    @InjectMocks
    private NotifyClaimDetailsRespOneSolEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        String templateId = "some-template-id";
        when(notifyClaimDetailsHelper.getEmailTemplate()).thenReturn(templateId);

        String result = generator.getEmailTemplateId(caseData);

        assertEquals(templateId, result);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertEquals(NotifyClaimDetailsHelper.REFERENCE_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomPropertiesFromHelper() {
        Map<String, String> baseProps = new HashMap<>();
        Map<String, String> customProps = Map.of("key", "value");

        when(notifyClaimDetailsHelper.getCustomProperties(caseData)).thenReturn(customProps);

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
            staticMock.when(() -> isOneVTwoTwoLegalRep(caseData)).thenReturn(true);
            when(notifyClaimDetailsHelper.checkDefendantToBeNotifiedWithClaimDetails(caseData, "respondent name")).thenReturn(true);

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
            staticMock.when(() -> isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

            boolean result = generator.getShouldNotify(caseData);

            assertTrue(result);
        }
    }
}

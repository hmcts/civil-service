package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class NotifyClaimRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotifyClaimHelper notifyClaimHelper;

    @InjectMocks
    private NotifyClaimRespSolTwoEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        when(notifyClaimHelper.getNotifyClaimEmailTemplate()).thenReturn("email-template-123");

        String result = generator.getEmailTemplateId(caseData);

        assertEquals("email-template-123", result);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertEquals("notify-claim-notification-%s", generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomPropertiesIncludingRespondent2Name() {
        Map<String, String> baseProps = new HashMap<>();
        Map<String, String> customProps = Map.of("key", "value");

        Party respondent2 = mock(Party.class);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(notifyClaimHelper.retrieveCustomProperties(caseData)).thenReturn(customProps);

        try (MockedStatic<PartyUtils> partyUtils = mockStatic(PartyUtils.class)) {
            partyUtils.when(() -> PartyUtils.getPartyNameBasedOnType(respondent2))
                .thenReturn("Jane Doe");

            Map<String, String> result = generator.addCustomProperties(baseProps, caseData);

            assertEquals(2, result.size());
            assertEquals("value", result.get("key"));
            assertEquals("Jane Doe", result.get(RESPONDENT_NAME));
        }
    }

    @Test
    void shouldReturnTrueWhenMultipartyAndSecondSolicitorRegisteredAndRespondentNotified() {
        Party respondent2 = mock(Party.class);
        when(respondent2.getPartyName()).thenReturn("respondent 2 name");
        when(caseData.isRespondentTwoSolicitorRegistered()).thenReturn(true);
        when(caseData.getRespondent2()).thenReturn(respondent2);

        try (MockedStatic<MultiPartyScenario> scenario = mockStatic(MultiPartyScenario.class)) {
            scenario.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);
            when(notifyClaimHelper.checkIfThisDefendantToBeNotified(caseData, "respondent 2 name")).thenReturn(true);

            boolean result = generator.getShouldNotify(caseData);
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnTrueWhenMultipartyAndFirstDefendantIsLIP() {
        Party respondent2 = mock(Party.class);
        when(respondent2.getPartyName()).thenReturn("respondent 2 name");
        when(caseData.isRespondentTwoSolicitorRegistered()).thenReturn(true);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(caseData.getDefendant1LIPAtClaimIssued()).thenReturn(YesOrNo.YES);

        try (MockedStatic<MultiPartyScenario> scenario = mockStatic(MultiPartyScenario.class)) {
            scenario.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

            boolean result = generator.getShouldNotify(caseData);
            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenNotMultiparty() {
        try (MockedStatic<MultiPartyScenario> scenario = mockStatic(MultiPartyScenario.class)) {
            scenario.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

            boolean result = generator.getShouldNotify(caseData);
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnFalseWhenSecondSolicitorNotRegistered() {
        when(caseData.isRespondentTwoSolicitorRegistered()).thenReturn(false);

        try (MockedStatic<MultiPartyScenario> scenario = mockStatic(MultiPartyScenario.class)) {
            scenario.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

            boolean result = generator.getShouldNotify(caseData);
            assertFalse(result);
        }
    }

    @Test
    void shouldReturnFalseWhenRespondent2IsNullAndHelperReturnsFalse() {
        when(caseData.isRespondentTwoSolicitorRegistered()).thenReturn(true);
        when(caseData.getRespondent2()).thenReturn(null);
        when(notifyClaimHelper.checkIfThisDefendantToBeNotified(caseData, null)).thenReturn(false);

        try (MockedStatic<MultiPartyScenario> scenario = mockStatic(MultiPartyScenario.class)) {
            scenario.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

            boolean result = generator.getShouldNotify(caseData);
            assertFalse(result);
        }
    }
}

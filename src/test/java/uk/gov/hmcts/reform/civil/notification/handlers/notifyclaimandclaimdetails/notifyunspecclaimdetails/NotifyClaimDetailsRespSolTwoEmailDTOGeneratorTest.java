package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class NotifyClaimDetailsRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotifyClaimDetailsHelper notifyClaimDetailsHelper;

    @InjectMocks
    private NotifyClaimDetailsRespSolTwoEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        String templateId = "email-template-123";
        when(notifyClaimDetailsHelper.getEmailTemplate()).thenReturn(templateId);

        String result = generator.getEmailTemplateId(caseData);

        assertEquals(templateId, result);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertEquals(NotifyClaimDetailsHelper.REFERENCE_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomPropertiesFromHelper() {
        Map<String, String> base = new HashMap<>();
        Map<String, String> custom = Map.of("key", "value");

        when(notifyClaimDetailsHelper.getCustomProperties(caseData)).thenReturn(custom);

        String respOrgName = "respondent2-legal-org-name";
        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, Boolean.FALSE, organisationService))
            .thenReturn(respOrgName);

        Map<String, String> result = generator.addCustomProperties(base, caseData);

        assertEquals(2, result.size());
        assertThat(result).containsEntry("key", "value");
        assertThat(result).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, respOrgName);
        notificationUtilsMockedStatic.close();
    }

    @Test
    void shouldReturnTrueWhenMultipartyWithSecondSolicitorRegisteredAndRespondentMatches() {
        Party respondent2 = mock(Party.class);
        when(respondent2.getPartyName()).thenReturn("respondent 2 name");

        when(caseData.isRespondentTwoSolicitorRegistered()).thenReturn(true);
        when(caseData.getRespondent2()).thenReturn(respondent2);

        try (MockedStatic<MultiPartyScenario> scenario = mockStatic(MultiPartyScenario.class)) {
            scenario.when(() -> isOneVTwoTwoLegalRep(caseData)).thenReturn(true);
            when(notifyClaimDetailsHelper.checkDefendantToBeNotifiedWithClaimDetails(caseData, "respondent 2 name")).thenReturn(true);

            boolean result = generator.getShouldNotify(caseData);

            assertTrue(result);
        }
    }

    @Test
    void shouldReturnFalseWhenNotMultiparty() {
        try (MockedStatic<MultiPartyScenario> scenario = mockStatic(MultiPartyScenario.class)) {
            scenario.when(() -> isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

            boolean result = generator.getShouldNotify(caseData);

            assertFalse(result);
        }
    }

    @Test
    void shouldReturnFalseWhenRespondentTwoSolicitorNotRegistered() {
        when(caseData.isRespondentTwoSolicitorRegistered()).thenReturn(false);

        try (MockedStatic<MultiPartyScenario> scenario = mockStatic(MultiPartyScenario.class)) {
            scenario.when(() -> isOneVTwoTwoLegalRep(caseData)).thenReturn(true);

            boolean result = generator.getShouldNotify(caseData);

            assertFalse(result);
        }
    }

    @Test
    void shouldReturnFalseWhenRespondentTwoNameIsNull() {
        when(caseData.isRespondentTwoSolicitorRegistered()).thenReturn(true);
        when(caseData.getRespondent2()).thenReturn(null);

        try (MockedStatic<MultiPartyScenario> scenario = mockStatic(MultiPartyScenario.class)) {
            scenario.when(() -> isOneVTwoTwoLegalRep(caseData)).thenReturn(true);
            when(notifyClaimDetailsHelper.checkDefendantToBeNotifiedWithClaimDetails(caseData, null)).thenReturn(false);

            boolean result = generator.getShouldNotify(caseData);

            assertFalse(result);
        }
    }
}

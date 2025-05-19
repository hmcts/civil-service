package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled;

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
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.MediationUtils;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@ExtendWith(MockitoExtension.class)
class CarmAppSolOneEmailDTOGeneratorTest {

    private static final String LEGAL_ORG_NAME = "Legal Org Ltd";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmAppSolOneEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .ccdCaseReference(12345L)
            .applicant1(Party.builder().partyName("Applicant One").build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr")
                             .individualFirstName("James")
                             .individualLastName("John")
                             .build())
            .respondent2(Party.builder().partyName("Respondent Two").build())
            .build();
    }

    @Test
    void shouldReturnSuccessfulTemplate_whenTaskIdMatches_andMultiPartyScenarioIsOneVTwo() {
        try (MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = mockStatic(MultiPartyScenario.class)) {
            multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(true);
            multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

            when(notificationsProperties.getNotifyOneVTwoClaimantSuccessfulMediation()).thenReturn("one-v-two-template");

            String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

            assertEquals("one-v-two-template", templateId);
        }
    }

    @Test
    void shouldReturnSuccessfulTemplate_whenTaskIdMatches_andMultiPartyScenarioIsNotOneVTwo() {
        try (MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = mockStatic(MultiPartyScenario.class)) {
            multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(false);
            multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

            when(notificationsProperties.getNotifyLrClaimantSuccessfulMediation()).thenReturn("lr-success-template");

            String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

            assertEquals("lr-success-template", templateId);
        }
    }

    @Test
    void shouldReturnUnsuccessfulNoAttendanceTemplate_whenUnsuccessfulReasonIsNotContactable() {
        try (MockedStatic<MediationUtils> mediationUtilsMockedStatic = mockStatic(MediationUtils.class)) {

            mediationUtilsMockedStatic.when(() -> MediationUtils.findMediationUnsuccessfulReason(
                eq(caseData),
                anyList()  // Properly typed to avoid raw warnings
            )).thenReturn(true);

            when(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).thenReturn("no-attendance-template");

            String templateId = generator.getEmailTemplateId(caseData, "someOtherTaskId");

            assertEquals("no-attendance-template", templateId);
        }
    }

    @Test
    void shouldReturnUnsuccessfulTemplate_whenNoUnsuccessfulReasonMatches() {
        try (MockedStatic<uk.gov.hmcts.reform.civil.utils.MediationUtils> mediationUtilsMockedStatic = mockStatic(uk.gov.hmcts.reform.civil.utils.MediationUtils.class)) {
            mediationUtilsMockedStatic.when(() -> uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason(
                eq(caseData),
                anyList())
            ).thenReturn(false);

            when(notificationsProperties.getMediationUnsuccessfulLRTemplate()).thenReturn("unsuccessful-template");

            String templateId = generator.getEmailTemplateId(caseData, "someOtherTaskId");

            assertEquals("unsuccessful-template", templateId);
        }
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertEquals("mediation-update-applicant-notification-%s", generator.getReferenceTemplate());
    }

    @Test
    void shouldAddCustomProperties_whenOneVTwoScenario() {
        try (MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = mockStatic(MultiPartyScenario.class);
             MockedStatic<NotificationUtils> notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
             MockedStatic<PartyUtils> partyUtilsMockedStatic = mockStatic(PartyUtils.class)) {

            multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(true);
            multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

            notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
                .thenReturn(LEGAL_ORG_NAME);

            partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(caseData.getRespondent1()))
                .thenReturn("James John");

            partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(caseData.getRespondent2()))
                .thenReturn("Respondent Two");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> result = generator.addCustomProperties(properties, caseData);

            assertEquals("your claim against James John and Respondent Two", result.get(PARTY_NAME));
            assertEquals(LEGAL_ORG_NAME, result.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            assertEquals("James John", result.get(DEFENDANT_NAME_ONE));
            assertEquals("Respondent Two", result.get(DEFENDANT_NAME_TWO));
        }
    }

    @Test
    void shouldAddCustomProperties_whenNotOneVTwoScenario() {
        try (MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic = mockStatic(MultiPartyScenario.class);
             MockedStatic<NotificationUtils> notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
             MockedStatic<PartyUtils> partyUtilsMockedStatic = mockStatic(PartyUtils.class)) {

            multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(false);
            multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData)).thenReturn(false);

            notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
                .thenReturn(LEGAL_ORG_NAME);

            partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(caseData.getRespondent1()))
                .thenReturn("James John");

            Map<String, String> properties = new HashMap<>();
            Map<String, String> result = generator.addCustomProperties(properties, caseData);

            assertEquals("your claim against James John", result.get(PARTY_NAME));
            assertEquals(LEGAL_ORG_NAME, result.get(CLAIM_LEGAL_ORG_NAME_SPEC));
            assertEquals("12345", result.get(CLAIM_REFERENCE_NUMBER));
            assertEquals("James John", result.get(DEFENDANT_NAME));
        }
    }
}

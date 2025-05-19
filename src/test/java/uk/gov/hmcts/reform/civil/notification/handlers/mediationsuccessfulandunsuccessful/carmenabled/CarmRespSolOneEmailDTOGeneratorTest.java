package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled;

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
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.MediationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME_TWO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;

@ExtendWith(MockitoExtension.class)
class CarmRespSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_NOTIFY_TWO_V_ONE = "notify-two-v-one";
    private static final String TEMPLATE_NOTIFY_LR_SUCCESS = "notify-lr-success";
    private static final String TEMPLATE_MED_UNSUCCESSFUL_NO_ATTENDANCE = "med-unsuccessful-no-attendance";
    private static final String TEMPLATE_MED_UNSUCCESSFUL = "med-unsuccessful";
    private static final String CLAIM_LEGAL_ORG_NAME = "Legal Org Name";
    private static final String APPLICANT1_NAME = "Applicant One";
    private static final String APPLICANT2_NAME = "Applicant Two";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmRespSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnTwoVOneSuccessTemplate_whenTaskIsMediationSuccessAndIsTwoVOne() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNotifyTwoVOneDefendantSuccessfulMediation()).thenReturn(TEMPLATE_NOTIFY_TWO_V_ONE);
        try (MockedStatic<MultiPartyScenario> mockStatic = mockStatic(MultiPartyScenario.class)) {
            mockStatic.when(() -> MultiPartyScenario.isTwoVOne(caseData)).thenReturn(true);
            String template = generator.getEmailTemplateId(caseData, "mediationSuccessfulNotifyParties");
            assertThat(template).isEqualTo(TEMPLATE_NOTIFY_TWO_V_ONE);
        }
    }

    @Test
    void shouldReturnLrSuccessTemplate_whenTaskIsMediationSuccessAndIsNotTwoVOne() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNotifyLrDefendantSuccessfulMediation()).thenReturn(TEMPLATE_NOTIFY_LR_SUCCESS);
        try (MockedStatic<MultiPartyScenario> mockStatic = mockStatic(MultiPartyScenario.class)) {
            mockStatic.when(() -> MultiPartyScenario.isTwoVOne(caseData)).thenReturn(false);
            String template = generator.getEmailTemplateId(caseData, "MediationSuccessfulNotifyParties");
            assertThat(template).isEqualTo(TEMPLATE_NOTIFY_LR_SUCCESS);
        }
    }

    @Test
    void shouldReturnNoAttendanceTemplate_whenUnsuccessfulAndReasonMatches() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate())
            .thenReturn(TEMPLATE_MED_UNSUCCESSFUL_NO_ATTENDANCE);

        try (
            MockedStatic<MediationUtils> mediationUtilsMock = mockStatic(MediationUtils.class);
            MockedStatic<MultiPartyScenario> multiPartyMock = mockStatic(MultiPartyScenario.class)
        ) {
            mediationUtilsMock.when(() ->
                                        MediationUtils.findMediationUnsuccessfulReason(eq(caseData), any())
            ).thenReturn(true);
            multiPartyMock.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(false);

            String template = generator.getEmailTemplateId(caseData, "someOtherTask");
            assertThat(template).isEqualTo(TEMPLATE_MED_UNSUCCESSFUL_NO_ATTENDANCE);
        }
    }

    @Test
    void shouldReturnNoAttendanceTemplate_whenUnsuccessfulAndIsOneVTwoAndReasonMatchesForDefendantTwo() {
        CaseData caseData = mock(CaseData.class);
        try (
            MockedStatic<MediationUtils> mediationUtilsMock = mockStatic(MediationUtils.class);
            MockedStatic<MultiPartyScenario> multiPartyMock = mockStatic(MultiPartyScenario.class)
        ) {
            mediationUtilsMock.when(() ->
                                        MediationUtils.findMediationUnsuccessfulReason(eq(caseData), any())
            ).thenReturn(false).thenReturn(true);
            multiPartyMock.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(true);

            String template = generator.getEmailTemplateId(caseData, "someOtherTask");
            assertThat(template).isEqualTo(TEMPLATE_MED_UNSUCCESSFUL_NO_ATTENDANCE);
        }
    }

    @Test
    void shouldReturnUnsuccessfulTemplate_whenUnsuccessfulAndNoMatchingReason() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getMediationUnsuccessfulLRTemplate()).thenReturn(TEMPLATE_MED_UNSUCCESSFUL);
        try (
            MockedStatic<MediationUtils> mediationUtilsMock = mockStatic(MediationUtils.class);
            MockedStatic<MultiPartyScenario> multiPartyMock = mockStatic(MultiPartyScenario.class)
        ) {
            mediationUtilsMock.when(() ->
                                        MediationUtils.findMediationUnsuccessfulReason(eq(caseData), any())
            ).thenReturn(false);
            multiPartyMock.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(false);

            String template = generator.getEmailTemplateId(caseData, "someOtherTask");
            assertThat(template).isEqualTo(TEMPLATE_MED_UNSUCCESSFUL);
        }
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();
        assertThat(referenceTemplate).isEqualTo("mediation-update-defendant-notification-LR-%s");
    }

    @Test
    void shouldAddCustomPropertiesForTwoVOneScenario() {
        CaseData caseData = mock(CaseData.class);
        Party applicant1 = Party.builder().partyName(APPLICANT1_NAME).build();
        Party applicant2 = Party.builder().partyName(APPLICANT2_NAME).build();
        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(caseData.getApplicant2()).thenReturn(applicant2);

        Map<String, String> props = new HashMap<>();

        try (
            MockedStatic<MultiPartyScenario> multiPartyMock = mockStatic(MultiPartyScenario.class);
            MockedStatic<NotificationUtils> notifUtilsMock = mockStatic(NotificationUtils.class)
        ) {
            multiPartyMock.when(() -> MultiPartyScenario.isTwoVOne(caseData)).thenReturn(true);
            notifUtilsMock.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, true, organisationService))
                .thenReturn(CLAIM_LEGAL_ORG_NAME);

            Map<String, String> result = generator.addCustomProperties(props, caseData);
            assertThat(result).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, CLAIM_LEGAL_ORG_NAME);
            assertThat(result).containsEntry(CLAIMANT_NAME_ONE, APPLICANT1_NAME);
            assertThat(result).containsEntry(CLAIMANT_NAME_TWO, APPLICANT2_NAME);
            assertThat(result).containsEntry(PARTY_NAME, APPLICANT1_NAME + " and " + APPLICANT2_NAME + "'s claim against you");
        }
    }

    @Test
    void shouldAddCustomPropertiesForNonTwoVOneScenario() {
        CaseData caseData = mock(CaseData.class);
        Party applicant1 = Party.builder().partyName(APPLICANT1_NAME).build();
        when(caseData.getApplicant1()).thenReturn(applicant1);

        Map<String, String> props = new HashMap<>();

        try (
            MockedStatic<MultiPartyScenario> multiPartyMock = mockStatic(MultiPartyScenario.class);
            MockedStatic<NotificationUtils> notifUtilsMock = mockStatic(NotificationUtils.class)
        ) {
            multiPartyMock.when(() -> MultiPartyScenario.isTwoVOne(caseData)).thenReturn(false);
            notifUtilsMock.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, true, organisationService))
                .thenReturn(CLAIM_LEGAL_ORG_NAME);

            Map<String, String> result = generator.addCustomProperties(props, caseData);
            assertThat(result).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, CLAIM_LEGAL_ORG_NAME);
            assertThat(result).containsEntry(CLAIMANT_NAME, APPLICANT1_NAME);
            assertThat(result).containsEntry(PARTY_NAME, APPLICANT1_NAME + "'s claim against you");
        }
    }
}

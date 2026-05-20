package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled;

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
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.MediationUtils;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME_TWO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@ExtendWith(MockitoExtension.class)
class CarmDisabledRespSolOneRepresentedEmailDTOGeneratorTest {

    private static final String SUCCESS_TEMPLATE = "success-template";
    private static final String TWO_V_ONE_SUCCESS_TEMPLATE = "two-v-one-success-template";
    private static final String UNSUCCESSFUL_TEMPLATE = "unsuccessful-template";
    private static final String NO_ATTENDANCE_TEMPLATE = "no-attendance-template";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmDisabledRespSolOneRepresentedEmailDTOGenerator generator;

    @Test
    void shouldReturnSuccessfulTemplateForRepresentedDefendant() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNotifyLrDefendantSuccessfulMediation()).thenReturn(SUCCESS_TEMPLATE);

        String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo(SUCCESS_TEMPLATE);
    }

    @Test
    void shouldReturnTwoVOneSuccessfulTemplateForRepresentedDefendant() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNotifyTwoVOneDefendantSuccessfulMediation()).thenReturn(TWO_V_ONE_SUCCESS_TEMPLATE);

        try (MockedStatic<MultiPartyScenario> multiPartyScenario = mockStatic(MultiPartyScenario.class)) {
            multiPartyScenario.when(() -> MultiPartyScenario.isTwoVOne(caseData)).thenReturn(true);

            String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

            assertThat(templateId).isEqualTo(TWO_V_ONE_SUCCESS_TEMPLATE);
        }
    }

    @Test
    void shouldReturnNoAttendanceTemplateWhenDefendantOneNotContactable() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).thenReturn(NO_ATTENDANCE_TEMPLATE);

        try (
            MockedStatic<MediationUtils> mediationUtils = mockStatic(MediationUtils.class);
            MockedStatic<MultiPartyScenario> multiPartyScenario = mockStatic(MultiPartyScenario.class)
        ) {
            mediationUtils.when(() -> MediationUtils.findMediationUnsuccessfulReason(eq(caseData), eq(List.of(NOT_CONTACTABLE_DEFENDANT_ONE))))
                .thenReturn(true);
            multiPartyScenario.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(false);

            String templateId = generator.getEmailTemplateId(caseData, "other-task");

            assertThat(templateId).isEqualTo(NO_ATTENDANCE_TEMPLATE);
        }
    }

    @Test
    void shouldReturnNoAttendanceTemplateWhenOneVTwoLegalRepAndDefendantTwoNotContactable() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).thenReturn(NO_ATTENDANCE_TEMPLATE);

        try (
            MockedStatic<MediationUtils> mediationUtils = mockStatic(MediationUtils.class);
            MockedStatic<MultiPartyScenario> multiPartyScenario = mockStatic(MultiPartyScenario.class)
        ) {
            mediationUtils.when(() -> MediationUtils.findMediationUnsuccessfulReason(eq(caseData), any()))
                .thenReturn(false)
                .thenReturn(true);
            multiPartyScenario.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(true);

            String templateId = generator.getEmailTemplateId(caseData, "other-task");

            assertThat(templateId).isEqualTo(NO_ATTENDANCE_TEMPLATE);
        }
    }

    @Test
    void shouldReturnUnsuccessfulTemplateWhenNoNoAttendanceReasonMatches() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getMediationUnsuccessfulLRTemplate()).thenReturn(UNSUCCESSFUL_TEMPLATE);

        try (
            MockedStatic<MediationUtils> mediationUtils = mockStatic(MediationUtils.class);
            MockedStatic<MultiPartyScenario> multiPartyScenario = mockStatic(MultiPartyScenario.class)
        ) {
            mediationUtils.when(() -> MediationUtils.findMediationUnsuccessfulReason(eq(caseData), any()))
                .thenReturn(false);
            multiPartyScenario.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData)).thenReturn(false);

            String templateId = generator.getEmailTemplateId(caseData);

            assertThat(templateId).isEqualTo(UNSUCCESSFUL_TEMPLATE);
        }
    }

    @Test
    void shouldNotifyForLrVsLrOneVOne() {
        CaseData caseData = new CaseDataBuilder()
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .build();

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyForLipVsLrOneVOne() {
        CaseData caseData = new CaseDataBuilder()
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.YES)
            .build();

        assertThat(generator.getShouldNotify(caseData)).isFalse();
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = mock(CaseData.class);
        Party applicant1 = new Party()
            .setType(Party.Type.INDIVIDUAL)
            .setIndividualFirstName("Alice")
            .setIndividualLastName("Smith");
        when(caseData.getApplicant1()).thenReturn(applicant1);

        try (MockedStatic<NotificationUtils> notificationUtils = mockStatic(NotificationUtils.class)) {
            notificationUtils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, true, organisationService))
                .thenReturn("Respondent Legal Org");

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertThat(result)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Respondent Legal Org")
                .containsEntry(CLAIMANT_NAME, "Alice Smith")
                .containsEntry(PARTY_NAME, "Alice Smith's claim against you");
        }
    }

    @Test
    void shouldAddCustomPropertiesForTwoVOneCase() {
        Party applicant1 = new Party()
            .setType(Party.Type.INDIVIDUAL)
            .setIndividualFirstName("Alice")
            .setIndividualLastName("Smith");
        Party applicant2 = new Party()
            .setType(Party.Type.INDIVIDUAL)
            .setIndividualFirstName("Bob")
            .setIndividualLastName("Jones");
        CaseData caseData = new CaseDataBuilder()
            .applicant1(applicant1)
            .applicant2(applicant2)
            .addApplicant2(YesOrNo.YES)
            .build();

        try (MockedStatic<NotificationUtils> notificationUtils = mockStatic(NotificationUtils.class)) {
            notificationUtils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, true, organisationService))
                .thenReturn("Respondent Legal Org");

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertThat(result)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Respondent Legal Org")
                .containsEntry(CLAIMANT_NAME_ONE, "Alice Smith")
                .containsEntry(CLAIMANT_NAME_TWO, "Bob Jones")
                .containsEntry(PARTY_NAME, "Alice Smith and Bob Jones's claim against you");
        }
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("mediation-update-defendant-notification-LR-%s");
    }
}

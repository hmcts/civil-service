package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.MediationUtils;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_TWO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@ExtendWith(MockitoExtension.class)
class CarmRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_SUCCESS = "notify-lr-success";
    private static final String TEMPLATE_UNSUCCESSFUL_NO_ATTENDANCE = "med-unsuccessful-no-attendance";
    private static final String TEMPLATE_UNSUCCESSFUL = "med-unsuccessful";
    private static final String ORG_NAME = "Some Legal Org";
    private static final String CLAIMANT_PARTY_NAME = "John Smith";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnSuccessfulMediationTemplate_whenTaskIsSuccessful() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNotifyLrDefendantSuccessfulMediation()).thenReturn(TEMPLATE_SUCCESS);
        String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());
        assertThat(templateId).isEqualTo(TEMPLATE_SUCCESS);
    }

    @Test
    void shouldReturnNoAttendanceTemplate_whenUnsuccessfulReasonMatches() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getMediationUnsuccessfulNoAttendanceLRTemplate()).thenReturn(TEMPLATE_UNSUCCESSFUL_NO_ATTENDANCE);
        try (MockedStatic<MediationUtils> mediationUtils = mockStatic(MediationUtils.class)) {
            mediationUtils.when(() -> MediationUtils.findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_TWO)))
                .thenReturn(true);

            String templateId = generator.getEmailTemplateId(caseData, "someOtherTask");
            assertThat(templateId).isEqualTo(TEMPLATE_UNSUCCESSFUL_NO_ATTENDANCE);
        }
    }

    @Test
    void shouldReturnUnsuccessfulTemplate_whenUnsuccessfulReasonDoesNotMatch() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getMediationUnsuccessfulLRTemplate()).thenReturn(TEMPLATE_UNSUCCESSFUL);
        try (MockedStatic<MediationUtils> mediationUtils = mockStatic(MediationUtils.class)) {
            mediationUtils.when(() -> MediationUtils.findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_TWO)))
                .thenReturn(false);

            String templateId = generator.getEmailTemplateId(caseData, "someOtherTask");
            assertThat(templateId).isEqualTo(TEMPLATE_UNSUCCESSFUL);
        }
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("mediation-update-defendant-notification-LR-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = mock(CaseData.class);
        Party applicant1 = Party.builder().partyName(CLAIMANT_PARTY_NAME).build();
        when(caseData.getApplicant1()).thenReturn(applicant1);

        try (MockedStatic<NotificationUtils> notificationUtils = mockStatic(NotificationUtils.class)) {
            notificationUtils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, false, organisationService))
                .thenReturn(ORG_NAME);

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertThat(result).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME);
            assertThat(result).containsEntry(CLAIMANT_NAME, CLAIMANT_PARTY_NAME);
            assertThat(result).containsEntry(PARTY_NAME, CLAIMANT_PARTY_NAME + "'s claim against you");
        }
    }
}

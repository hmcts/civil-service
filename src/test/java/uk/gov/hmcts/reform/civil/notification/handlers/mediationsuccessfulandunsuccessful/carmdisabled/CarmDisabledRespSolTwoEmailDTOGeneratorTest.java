package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled;

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
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@ExtendWith(MockitoExtension.class)
class CarmDisabledRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CarmDisabledRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnSuccessfulTemplateForSecondRepresentedDefendant() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getNotifyLrDefendantSuccessfulMediation()).thenReturn("success-template");

        String templateId = generator.getEmailTemplateId(caseData, MediationSuccessfulNotifyParties.toString());

        assertThat(templateId).isEqualTo("success-template");
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
            notificationUtils.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, false, organisationService))
                .thenReturn("Respondent 2 Legal Org");

            Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

            assertThat(result)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Respondent 2 Legal Org")
                .containsEntry(CLAIMANT_NAME, "Alice Smith")
                .containsEntry(PARTY_NAME, "Alice Smith's claim against you");
        }
    }
}

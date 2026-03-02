package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_EMAIL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;

@ExtendWith(MockitoExtension.class)
class GenerateSpecDJFormReceivedRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String REFERENCE_TEMPLATE = "default-judgment-respondent-received-notification-%s";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    private GenerateSpecDJFormReceivedRespSolTwoEmailDTOGenerator generator;
    private MockedStatic<NotificationUtils> notificationUtilsMockedStatic;
    private MockedStatic<PartyUtils> partyUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        generator = new GenerateSpecDJFormReceivedRespSolTwoEmailDTOGenerator(organisationService, notificationsProperties);
        notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
        partyUtilsMockedStatic = mockStatic(PartyUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (notificationUtilsMockedStatic != null) {
            notificationUtilsMockedStatic.close();
        }
        if (partyUtilsMockedStatic != null) {
            partyUtilsMockedStatic.close();
        }
    }

    @Test
    void shouldReturnTemplateId() {
        CaseData caseData = mock(CaseData.class);
        when(notificationsProperties.getRespondentSolicitor1DefaultJudgmentReceived()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();
        CaseData caseData = mock(CaseData.class);
        Party respondent2 = mock(Party.class);
        Party applicant1 = mock(Party.class);

        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, false, organisationService))
            .thenReturn("Respondent Two Org");
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(respondent2)).thenReturn("Defendant 2");
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(applicant1)).thenReturn("Applicant 1");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsEntry(DEFENDANT_EMAIL, "Respondent Two Org");
        assertThat(result).containsEntry(CLAIM_NUMBER, "1234567890123456");
        assertThat(result).containsEntry(DEFENDANT_NAME, "Defendant 2");
        assertThat(result).containsEntry(CLAIMANT_NAME, "Applicant 1");
    }
}


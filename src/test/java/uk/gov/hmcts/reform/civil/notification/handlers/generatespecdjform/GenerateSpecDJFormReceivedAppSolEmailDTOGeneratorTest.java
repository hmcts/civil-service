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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_SPECIFIED;

@ExtendWith(MockitoExtension.class)
class GenerateSpecDJFormReceivedAppSolEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String REFERENCE_TEMPLATE_RECEIVED = "default-judgment-applicant-received-notification-%s";
    private static final String APPLICANT_ORG = "Applicant Org";
    private static final String DEFENDANT = "Defendant One";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    private GenerateSpecDJFormReceivedAppSolEmailDTOGenerator generator;
    private MockedStatic<NotificationUtils> notificationUtilsMockedStatic;
    private MockedStatic<PartyUtils> partyUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        generator = new GenerateSpecDJFormReceivedAppSolEmailDTOGenerator(organisationService, notificationsProperties);
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
        when(notificationsProperties.getApplicantSolicitor1DefaultJudgmentReceived()).thenReturn(TEMPLATE_ID);

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE_RECEIVED);
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(APPLICANT_ORG);
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(respondent1)).thenReturn(DEFENDANT);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result).containsEntry(LEGAL_ORG_SPECIFIED, APPLICANT_ORG);
        assertThat(result).containsEntry(CLAIM_NUMBER, "1234567890123456");
        assertThat(result).containsEntry(DEFENDANT_NAME, DEFENDANT);
    }
}


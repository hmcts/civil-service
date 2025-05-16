package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_CLAIMANT;

class GenerateDJFormApprovedAppSolOneEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    protected static final String APPLICANT_LEGAL_ORG_NAME = "Test Legal Org";

    private GenerateDJFormApprovedAppSolOneEmailDTOGenerator generator;
    private NotificationsProperties notificationsProperties;
    private OrganisationService organisationService;
    private GenerateDJFormHelper generateDJFormHelper;
    private static final String REFERENCE_TEMPLATE_APPROVAL_CLAIMANT = "interim-judgment-approval-notification-%s";

    private MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic;
    private MockedStatic<NotificationUtils> notificationUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        organisationService = Mockito.mock(OrganisationService.class);
        generateDJFormHelper = mock(GenerateDJFormHelper.class);
        generator = new GenerateDJFormApprovedAppSolOneEmailDTOGenerator(
            notificationsProperties,
            organisationService,
            generateDJFormHelper
        );
        multiPartyScenarioMockedStatic = mockStatic(MultiPartyScenario.class);
        notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (multiPartyScenarioMockedStatic != null) {
            multiPartyScenarioMockedStatic.close();
        }
        if (notificationUtilsMockedStatic != null) {
            notificationUtilsMockedStatic.close();
        }
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = mock(CaseData.class);

        when(notificationsProperties.getInterimJudgmentApprovalClaimant()).thenReturn(TEMPLATE_ID);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo(REFERENCE_TEMPLATE_APPROVAL_CLAIMANT);
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();

        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        Party respondent2 = mock(Party.class);

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(APPLICANT_LEGAL_ORG_NAME);

        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(respondent1.getPartyName()).thenReturn("Respondent 1");
        when(respondent2.getPartyName()).thenReturn("Respondent 2");
        when(generateDJFormHelper.checkDefendantRequested(caseData, false)).thenReturn(true);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(LEGAL_REP_CLAIMANT)).isEqualTo(APPLICANT_LEGAL_ORG_NAME);
        assertThat(result.get(CLAIM_NUMBER_INTERIM)).isEqualTo("1234567890123456");
        assertThat(result.get(DEFENDANT_NAME_INTERIM)).isEqualTo("Respondent 2");
    }

    @Test
    void shouldAddCustomPropertiesWhenIf_isOneVTwoTwoLegalRep_ConditionIsFalse() {
        Map<String, String> properties = new HashMap<>();

        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        Party respondent2 = mock(Party.class);

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(APPLICANT_LEGAL_ORG_NAME);

        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(respondent1.getPartyName()).thenReturn("Respondent 1");
        when(respondent2.getPartyName()).thenReturn("Respondent 2");
        when(generateDJFormHelper.checkDefendantRequested(caseData, false)).thenReturn(true);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(false);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(LEGAL_REP_CLAIMANT)).isEqualTo(APPLICANT_LEGAL_ORG_NAME);
        assertThat(result.get(CLAIM_NUMBER_INTERIM)).isEqualTo("1234567890123456");
        assertThat(result.get(DEFENDANT_NAME_INTERIM)).isEqualTo("Respondent 1");
    }

    @Test
    void shouldAddCustomPropertiesWhenIf_checkDefendantRequested_ConditionIsFalse() {
        Map<String, String> properties = new HashMap<>();

        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        Party respondent2 = mock(Party.class);

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getApplicantLegalOrganizationName(caseData, organisationService))
            .thenReturn(APPLICANT_LEGAL_ORG_NAME);
        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(respondent1.getPartyName()).thenReturn("Respondent 1");
        when(respondent2.getPartyName()).thenReturn("Respondent 2");
        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(false);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(LEGAL_REP_CLAIMANT)).isEqualTo(APPLICANT_LEGAL_ORG_NAME);
        assertThat(result.get(CLAIM_NUMBER_INTERIM)).isEqualTo("1234567890123456");
        assertThat(result.get(DEFENDANT_NAME_INTERIM)).isEqualTo("Respondent 1");
    }
}

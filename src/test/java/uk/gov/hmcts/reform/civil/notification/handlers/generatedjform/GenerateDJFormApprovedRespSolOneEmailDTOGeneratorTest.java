package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_DEF;

class GenerateDJFormApprovedRespSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String DEFENDANT_LEGAL_ORG_NAME = "Test Legal Org";

    private GenerateDJFormApprovedRespSolOneEmailDTOGenerator generator;
    private NotificationsProperties notificationsProperties;
    private OrganisationService organisationService;
    private static final String REFERENCE_TEMPLATE_APPROVAL_DEF = "interim-judgment-approval-notification-def-%s";

    private MockedStatic<NotificationUtils> notificationUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        organisationService = mock(OrganisationService.class);
        generator = new GenerateDJFormApprovedRespSolOneEmailDTOGenerator(
            notificationsProperties,
            organisationService
        );
        notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (notificationUtilsMockedStatic != null) {
            notificationUtilsMockedStatic.close();
        }
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = mock(CaseData.class);

        when(notificationsProperties.getInterimJudgmentApprovalDefendant()).thenReturn(TEMPLATE_ID);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo(REFERENCE_TEMPLATE_APPROVAL_DEF);
    }

    @Test
    void shouldAddCustomPropertiesForRespondent1() {
        Map<String, String> properties = new HashMap<>();

        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);

        boolean isRespondent1 = true;
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, isRespondent1, organisationService))
            .thenReturn(DEFENDANT_LEGAL_ORG_NAME);

        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(respondent1.getPartyName()).thenReturn("Respondent 1");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(LEGAL_ORG_DEF)).isEqualTo(DEFENDANT_LEGAL_ORG_NAME);
        assertThat(result.get(CLAIM_NUMBER_INTERIM)).isEqualTo("1234567890123456");
        assertThat(result.get(DEFENDANT_NAME_INTERIM)).isEqualTo("Respondent 1");
    }
}

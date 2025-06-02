package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_DEF;

class GenerateDJFormApprovedRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private static final String DEFENDANT_LEGAL_ORG_NAME = "Test Legal Org";

    private GenerateDJFormApprovedRespSolTwoEmailDTOGenerator generator;
    private NotificationsProperties notificationsProperties;
    private OrganisationService organisationService;
    private static final String REFERENCE_TEMPLATE_APPROVAL_DEF = "interim-judgment-approval-notification-def-%s";

    private MockedStatic<NotificationUtils> notificationUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        organisationService = mock(OrganisationService.class);
        generator = new GenerateDJFormApprovedRespSolTwoEmailDTOGenerator(
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
    void shouldAddCustomPropertiesForRespondent2() {
        Map<String, String> properties = new HashMap<>();

        CaseData caseData = mock(CaseData.class);
        Party respondent2 = mock(Party.class);

        boolean isRespondent1 = false;
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, isRespondent1, organisationService))
            .thenReturn(DEFENDANT_LEGAL_ORG_NAME);

        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(respondent2.getPartyName()).thenReturn("Respondent 2");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(LEGAL_ORG_DEF)).isEqualTo(DEFENDANT_LEGAL_ORG_NAME);
        assertThat(result.get(CLAIM_NUMBER_INTERIM)).isEqualTo("1234567890123456");
        assertThat(result.get(DEFENDANT_NAME_INTERIM)).isEqualTo("Respondent 2");
    }

    @Test
    void shouldReturnTrue_whenIsOneVTwoLegalRepIsTrue() {
        Party respondent2Party = Party.builder()
            .build();
        CaseData caseData = CaseData.builder().respondent2(respondent2Party)
            .respondent2Copy(respondent2Party)
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .build();

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrue_whenIsOneVTwoTwoLegalRepIsTrue() {
        Party respondent2Party = Party.builder()
            .build();
        CaseData caseData = CaseData.builder().respondent2(respondent2Party)
            .respondent2Copy(respondent2Party)
            .build();

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenBothConditionsAreFalse() {
        CaseData caseData = mock(CaseData.class);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }
}

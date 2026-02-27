package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class StandardDirectionOrderDJRespSolTwoEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    protected static final String RESPONDENT_LEGAL_ORG_NAME = "Test Respondent 2 Legal Org";
    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-defendant-%s";
    private static final String LEGAL_ORG_NAME = "legalOrgName";
    private static final String CLAIM_NUMBER = "claimReferenceNumber";

    private StandardDirectionOrderDJRespSolTwoEmailDTOGenerator generator;
    private NotificationsProperties notificationsProperties;
    private OrganisationService organisationService;

    private MockedStatic<NotificationUtils> notificationUtilsMockedStatic;
    private MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        organisationService = Mockito.mock(OrganisationService.class);
        generator = new StandardDirectionOrderDJRespSolTwoEmailDTOGenerator(
            notificationsProperties,
            organisationService
        );
        notificationUtilsMockedStatic = mockStatic(NotificationUtils.class);
        multiPartyScenarioMockedStatic = mockStatic(MultiPartyScenario.class);
    }

    @AfterEach
    void tearDown() {
        if (notificationUtilsMockedStatic != null) {
            notificationUtilsMockedStatic.close();
        }
        if (multiPartyScenarioMockedStatic != null) {
            multiPartyScenarioMockedStatic.close();
        }
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        CaseData caseData = mock(CaseData.class);

        when(notificationsProperties.getStandardDirectionOrderDJTemplate()).thenReturn(TEMPLATE_ID);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo(REFERENCE_TEMPLATE_SDO_DJ);
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();

        CaseData caseData = mock(CaseData.class);

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, false, organisationService))
            .thenReturn(RESPONDENT_LEGAL_ORG_NAME);

        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(LEGAL_ORG_NAME)).isEqualTo(RESPONDENT_LEGAL_ORG_NAME);
        assertThat(result.get(CLAIM_NUMBER)).isEqualTo("1234567890123456");
    }

    @Test
    void shouldReturnTrueWhenAllConditionsAreMet() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getAddRespondent2()).thenReturn(YesOrNo.YES);
        when(caseData.getRespondent2Represented()).thenReturn(YesOrNo.NO);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAddRespondent2IsNo() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getAddRespondent2()).thenReturn(YesOrNo.NO);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNotOneVTwoTwoLegalRep() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getAddRespondent2()).thenReturn(YesOrNo.YES);
        when(caseData.getRespondent2Represented()).thenReturn(YesOrNo.NO);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(false);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenRespondent2IsRepresented() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getAddRespondent2()).thenReturn(YesOrNo.YES);
        when(caseData.getRespondent2Represented()).thenReturn(YesOrNo.YES);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }
}

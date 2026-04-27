package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
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

class StandardDirectionOrderDJRespSolOneEmailDTOGeneratorTest
    extends StandardDirectionOrderDJEmailGeneratorBaseTest<StandardDirectionOrderDJRespSolOneEmailDTOGenerator> {

    private static final String RESPONDENT_LEGAL_ORG_NAME = "Test Respondent Legal Org";
    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-defendant-%s";

    private StandardDirectionOrderDJNotificationHelper notificationHelper;
    private MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic;

    @Override
    protected StandardDirectionOrderDJRespSolOneEmailDTOGenerator createGenerator(
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService) {
        StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper =
            new StandardDirectionOrderDJEmailDTOGeneratorBase(notificationsProperties);
        notificationHelper = mock(StandardDirectionOrderDJNotificationHelper.class);
        return new StandardDirectionOrderDJRespSolOneEmailDTOGenerator(
            organisationService, templateHelper, notificationHelper);
    }

    @Override
    protected String getExpectedReferenceTemplate() {
        return REFERENCE_TEMPLATE_SDO_DJ;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return generator.getEmailTemplateId(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return generator.getReferenceTemplate();
    }

    @Override
    protected void setupAdditionalMocks() {
        multiPartyScenarioMockedStatic = mockStatic(MultiPartyScenario.class);
    }

    @Override
    protected void tearDownAdditionalMocks() {
        if (multiPartyScenarioMockedStatic != null) {
            multiPartyScenarioMockedStatic.close();
        }
    }

    @Test
    void shouldAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();

        CaseData caseData = mock(CaseData.class);

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, true, organisationService))
            .thenReturn(RESPONDENT_LEGAL_ORG_NAME);

        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result)
            .containsEntry(LEGAL_ORG_NAME, RESPONDENT_LEGAL_ORG_NAME)
            .containsEntry(CLAIM_NUMBER, "1234567890123456");
    }

    @Test
    void shouldReturnTrueWhenRespondent1IsTargetDefendant() {
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);

        when(caseData.isRespondent1NotRepresented()).thenReturn(false);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(notificationHelper.isTargetDefendant(caseData, respondent1)).thenReturn(true);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenRespondent1IsNotRepresented() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.isRespondent1NotRepresented()).thenReturn(true);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenOneVTwoOneLegalRepAndRespondent2IsTargetDefendant() {
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        Party respondent2 = mock(Party.class);

        when(caseData.isRespondent1NotRepresented()).thenReturn(false);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(notificationHelper.isTargetDefendant(caseData, respondent1)).thenReturn(false);
        when(notificationHelper.isTargetDefendant(caseData, respondent2)).thenReturn(true);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData))
            .thenReturn(true);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenRespondent1NotTargetAndNotOneVTwoOneLegalRep() {
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        Party respondent2 = mock(Party.class);

        when(caseData.isRespondent1NotRepresented()).thenReturn(false);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(notificationHelper.isTargetDefendant(caseData, respondent1)).thenReturn(false);
        when(notificationHelper.isTargetDefendant(caseData, respondent2)).thenReturn(true);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData))
            .thenReturn(false);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenOneVTwoOneLegalRepButRespondent2NotTarget() {
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        Party respondent2 = mock(Party.class);

        when(caseData.isRespondent1NotRepresented()).thenReturn(false);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(notificationHelper.isTargetDefendant(caseData, respondent1)).thenReturn(false);
        when(notificationHelper.isTargetDefendant(caseData, respondent2)).thenReturn(false);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoLegalRep(caseData))
            .thenReturn(true);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }
}

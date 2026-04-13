package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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

class StandardDirectionOrderDJRespSolTwoEmailDTOGeneratorTest
    extends StandardDirectionOrderDJEmailGeneratorBaseTest<StandardDirectionOrderDJRespSolTwoEmailDTOGenerator> {

    private static final String RESPONDENT_LEGAL_ORG_NAME = "Test Respondent 2 Legal Org";
    private static final String REFERENCE_TEMPLATE_SDO_DJ = "sdo-dj-order-notification-defendant-%s";

    private MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic;

    @Override
    protected StandardDirectionOrderDJRespSolTwoEmailDTOGenerator createGenerator(
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService) {
        StandardDirectionOrderDJEmailDTOGeneratorBase templateHelper =
            new StandardDirectionOrderDJEmailDTOGeneratorBase(notificationsProperties);
        return new StandardDirectionOrderDJRespSolTwoEmailDTOGenerator(organisationService, templateHelper);
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

        notificationUtilsMockedStatic.when(() -> NotificationUtils.getLegalOrganizationNameForRespondent(caseData, false, organisationService))
            .thenReturn(RESPONDENT_LEGAL_ORG_NAME);

        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result)
            .containsEntry(LEGAL_ORG_NAME, RESPONDENT_LEGAL_ORG_NAME)
            .containsEntry(CLAIM_NUMBER, "1234567890123456");
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

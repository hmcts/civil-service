package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;

class GenerateDJFormApprovedRespSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    private GenerateDJFormApprovedRespSolOneEmailDTOGenerator generator;
    private NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_APPROVAL_DEF = "interim-judgment-approval-notification-def-%s";

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        OrganisationService organisationService = mock(OrganisationService.class);
        generator = new GenerateDJFormApprovedRespSolOneEmailDTOGenerator(
            notificationsProperties,
            organisationService
        );
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
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Test Legal Org");

        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);

        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(respondent1.getPartyName()).thenReturn("Respondent 1");

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(LEGAL_ORG_DEF)).isEqualTo("Test Legal Org");
        assertThat(result.get(CLAIM_NUMBER_INTERIM)).isEqualTo("1234567890123456");
        assertThat(result.get(DEFENDANT_NAME_INTERIM)).isEqualTo("Respondent 1");
    }
}

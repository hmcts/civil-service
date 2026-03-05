package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StandardDirectionOrderDJBaseEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "standard-direction-order-dj-template";
    private static final Long CASE_REFERENCE = 1234567890123456L;

    private NotificationsProperties notificationsProperties;
    private OrganisationService organisationService;
    private TestEmailDTOGenerator generator;

    private static class TestEmailDTOGenerator extends StandardDirectionOrderDJBaseEmailDTOGenerator {
        protected TestEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                        OrganisationService organisationService) {
            super(notificationsProperties, organisationService);
        }
    }

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        organisationService = mock(OrganisationService.class);
        generator = new TestEmailDTOGenerator(notificationsProperties, organisationService);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        when(notificationsProperties.getStandardDirectionOrderDJTemplate()).thenReturn(TEMPLATE_ID);

        String result = generator.getEmailTemplateId();

        assertThat(result).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldAddStandardPropertiesToMap() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(CASE_REFERENCE);

        Map<String, String> properties = new HashMap<>();
        generator.addStandardProperties(properties, caseData);

        assertThat(properties).containsEntry("claimReferenceNumber", CASE_REFERENCE.toString());
    }

    @Test
    void shouldAddStandardPropertiesToExistingMap() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(CASE_REFERENCE);

        Map<String, String> properties = new HashMap<>();
        properties.put("existingKey", "existingValue");
        generator.addStandardProperties(properties, caseData);

        assertThat(properties)
            .containsEntry("existingKey", "existingValue")
            .containsEntry("claimReferenceNumber", CASE_REFERENCE.toString())
            .hasSize(2);
    }

    @Test
    void shouldUseCorrectConstantForLegalOrgName() {
        assertThat(StandardDirectionOrderDJBaseEmailDTOGenerator.LEGAL_ORG_NAME).isEqualTo("legalOrgName");
    }

    @Test
    void shouldUseCorrectConstantForClaimNumber() {
        assertThat(StandardDirectionOrderDJBaseEmailDTOGenerator.CLAIM_NUMBER).isEqualTo("claimReferenceNumber");
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;

class SpecDefRespClaimantEmailDTOGeneratorTest {

    private SpecDefRespEmailHelper emailHelper;
    private SpecDefRespClaimantEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        emailHelper = mock(SpecDefRespEmailHelper.class);
        generator = new SpecDefRespClaimantEmailDTOGenerator(emailHelper);
    }

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "some-template-id";
        when(emailHelper.getLipTemplate(caseData)).thenReturn(expectedTemplateId);

        String actualTemplateId = generator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("defendant-response-spec-notification-%s");
    }

    @Test
    void shouldAddClaimantNameToProperties() {
        String claimantName = "Jane Doe";
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("Jane")
                            .individualLastName("Doe")
                            .build())
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(CLAIMANT_NAME)).isEqualTo(claimantName);
    }
}

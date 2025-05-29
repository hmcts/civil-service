package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.counterclaimordivergedresponse;

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

class SpecCaseOfflineClaimantEmailDTOGeneratorTest {

    private SpecCaseOfflineHelper helper;
    private SpecCaseOfflineClaimantEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        helper = mock(SpecCaseOfflineHelper.class);
        generator = new SpecCaseOfflineClaimantEmailDTOGenerator(helper);
    }

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = CaseData.builder().build();
        when(helper.getClaimantTemplateForLipVLRSpecClaims(caseData)).thenReturn("template-id-123");

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo("template-id-123");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo("defendant-response-case-handed-offline-applicant-notification-%s");
    }

    @Test
    void shouldAddClaimantNameToProperties() {
        String expectedName = "Jane Doe";

        Party applicant1 = Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualFirstName("Jane")
            .individualLastName("Doe")
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .build();

        Map<String, String> input = new HashMap<>();
        Map<String, String> result = generator.addCustomProperties(input, caseData);

        assertThat(result).containsEntry(CLAIMANT_NAME, "Jane Doe");
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlip;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

class ClaimantLipNocHelperTest {

    @Test
    void shouldReturnExpectedLipProperties() {

        Party applicant = Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualLastName("Claimant")
            .individualFirstName("John")
            .partyName("John Claimant").build();
        Party respondent = Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualLastName("Defendant")
            .individualFirstName("Jane")
            .partyName("Jane Defendant").build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .respondent1(respondent)
            .legacyCaseReference("000MC001")
            .build();

        Map<String, String> result = ClaimantLipNocHelper.getLipProperties(caseData);

        assertThat(result).containsOnly(
            entry(RESPONDENT_NAME, "Jane Defendant"),
            entry(CLAIM_REFERENCE_NUMBER, "000MC001"),
            entry(CLAIMANT_NAME, "John Claimant")
        );
    }

    @Test
    void shouldMatchReferenceTemplateFormat() {
        String reference = String.format(ClaimantLipNocHelper.REFERENCE_TEMPLATE, "12345");
        assertThat(reference).isEqualTo("notify-lip-after-noc-approval-12345");
    }
}

package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

class SdoNarrativeServiceTest {

    private static final String HEADER_TEMPLATE = "# Your order has been issued" + "%n## Claim number: %s";
    private static final String SUMMARY_1V1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";
    private static final String SUMMARY_2V1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Claimant 2</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";
    private static final String SUMMARY_1V2 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 2</strong>%n"
        + "<br/>%s";
    private static final String FEEDBACK_LINK = "<p>%s"
        + " <a href='https://www.smartsurvey.co.uk/s/QKJTVU//' target=_blank>here</a></p>";
    private static final String FEEDBACK_PROMPT = "Feedback: Please provide judicial feedback";

    private final SdoNarrativeService service = new SdoNarrativeService();

    @Test
    void shouldBuildHeaderWithClaimNumber() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        String result = service.buildConfirmationHeader(caseData);

        assertThat(result).isEqualTo(format(HEADER_TEMPLATE, caseData.getLegacyCaseReference()));
    }

    @Test
    void shouldBuildBodyForOneVOne() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        String result = service.buildConfirmationBody(caseData);

        String expected = format(
            SUMMARY_1V1,
            caseData.getApplicant1().getPartyName(),
            caseData.getRespondent1().getPartyName()
        ) + format(FEEDBACK_LINK, FEEDBACK_PROMPT);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldBuildBodyForTwoApplicants() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .multiPartyClaimTwoApplicants()
            .build();

        String result = service.buildConfirmationBody(caseData);

        String expected = format(
            SUMMARY_2V1,
            caseData.getApplicant1().getPartyName(),
            caseData.getApplicant2().getPartyName(),
            caseData.getRespondent1().getPartyName()
        ) + format(FEEDBACK_LINK, FEEDBACK_PROMPT);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldBuildBodyForTwoRespondents() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .multiPartyClaimTwoDefendantSolicitors()
            .build();

        String result = service.buildConfirmationBody(caseData);

        String expected = format(
            SUMMARY_1V2,
            caseData.getApplicant1().getPartyName(),
            caseData.getRespondent1().getPartyName(),
            caseData.getRespondent2().getPartyName()
        ) + format(FEEDBACK_LINK, FEEDBACK_PROMPT);

        assertThat(result).isEqualTo(expected);
    }
}

package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import static java.lang.String.format;

@Service
public class SdoNarrativeService {

    private static final String CONFIRMATION_HEADER = "# Your order has been issued"
        + "%n## Claim number: %s";
    private static final String CONFIRMATION_SUMMARY_1v1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";
    private static final String CONFIRMATION_SUMMARY_2v1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Claimant 2</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";
    private static final String CONFIRMATION_SUMMARY_1v2 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 2</strong>%n"
        + "<br/>%s";
    private static final String FEEDBACK_LINK = "<p>%s"
        + " <a href='https://www.smartsurvey.co.uk/s/QKJTVU//' target=_blank>here</a></p>";
    private static final String FEEDBACK_PROMPT = "Feedback: Please provide judicial feedback";

    public String buildConfirmationBody(CaseData caseData) {
        Party applicant1 = caseData.getApplicant1();
        Party respondent1 = caseData.getRespondent1();
        Party applicant2 = caseData.getApplicant2();
        Party respondent2 = caseData.getRespondent2();

        String confirmationSummary = format(
            CONFIRMATION_SUMMARY_1v1,
            applicant1.getPartyName(),
            respondent1.getPartyName()
        );

        if (applicant2 != null) {
            confirmationSummary = format(
                CONFIRMATION_SUMMARY_2v1,
                applicant1.getPartyName(),
                applicant2.getPartyName(),
                respondent1.getPartyName()
            );
        } else if (respondent2 != null) {
            confirmationSummary = format(
                CONFIRMATION_SUMMARY_1v2,
                applicant1.getPartyName(),
                respondent1.getPartyName(),
                respondent2.getPartyName()
            );
        }

        return confirmationSummary + format(FEEDBACK_LINK, FEEDBACK_PROMPT);
    }

    public String buildConfirmationHeader(CaseData caseData) {
        return format(CONFIRMATION_HEADER, caseData.getLegacyCaseReference());
    }
}

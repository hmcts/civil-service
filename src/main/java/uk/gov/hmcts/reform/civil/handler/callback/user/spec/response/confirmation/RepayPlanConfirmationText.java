package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.EnumSet;
import java.util.Optional;

import static java.lang.String.format;

@Component
public class RepayPlanConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    /**
     * Confirmation summary text for a response with a repayment plan offer.
     *
     * @param caseData a case data
     * @return if suitable, the summary text for repayment plan offer
     */
    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN.equals(
            caseData.getDefenceAdmitPartPaymentTimeRouteRequired())
            || !EnumSet.of(RespondentResponseTypeSpec.FULL_ADMISSION, RespondentResponseTypeSpec.PART_ADMISSION)
            .contains(caseData.getRespondent1ClaimResponseTypeForSpec())
        ) {
            return Optional.empty();
        }

        StringBuilder sb = new StringBuilder();
        String applicantName = caseData.getApplicant1().getPartyName();
        if (caseData.getApplicant2() != null) {
            applicantName += " and " + caseData.getApplicant2().getPartyName();
        }
        sb.append("<br>We've emailed ").append(applicantName)
            .append(" to say you've suggested paying by instalments.")
            .append("<br><br>We'll contact you when ").append(applicantName).append(" responds.")
            .append(String
                        .format(
                            "%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
                            format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
                        ))

            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" accept your offer</h3>");
        } else {
            sb.append(" accepts your offer</h3>");
        }
        sb.append("You should<ul>")
            .append("<li>set up a repayment plan to begin when you said it would</li>")
            .append("<li>keep proof of any payments you make</li>")
            .append("</ul>")
            .append("Contact ").append(applicantName);
        if (applicantName.endsWith("s")) {
            sb.append("'");
        } else {
            sb.append("'s");
        }
        sb.append(" legal representative if you need details on how to pay")
            .append("<br><br>")
            .append("If you do not pay immediately, ").append(applicantName)
            .append(" can either:")
            .append("<ul>")
            .append("<li>ask you to sign a settlement agreement to formalise the repayment plan</li>")
            .append("<li>request a county court judgement against you</li>")
            .append("</ul>")

            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" reject your offer</h3>");
        } else {
            sb.append(" rejects your offer</h3>");
        }
        sb.append("The court will decide how you must pay");

        return Optional.of(sb.toString());
    }
}

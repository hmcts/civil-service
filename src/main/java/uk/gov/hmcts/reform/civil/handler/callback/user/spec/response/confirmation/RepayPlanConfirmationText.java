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
        sb.append("<p>You should</p><ul>")
            .append("<li><p class=\"govuk-!-margin-0\">set up a repayment plan to begin when you said it would</p></li>")
            .append("<li><p class=\"govuk-!-margin-0\">keep proof of any payments you make<p></li>")
            .append("</ul>")
            .append("<p>Contact ").append(applicantName);

        if (!caseData.isApplicant1NotRepresented()) {
            if (applicantName.endsWith("s")) {
                sb.append("'");
            } else {
                sb.append("'s");
            }
            sb.append(" legal representative if you need details on how to pay</p>");
        } else {
            sb.append(" if you need details on how to pay</p>");
        }

        sb.append("<p>If you do not pay immediately, ").append(applicantName);
        if (caseData.getRespondent2() != null || caseData.getApplicant2() != null) {
            sb.append(" can either:</p>")
                .append("<ul>")
                .append("<li><p class=\"govuk-!-margin-0\">ask you to sign a settlement agreement to formalise the repayment plan</p></li>");
        } else {
            sb.append(" can:</p>")
                .append("<ul>");
        }
        sb.append("<li><p class=\"govuk-!-margin-0\">request a county court judgment against you</p></li>")
            .append("</ul>")
            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" reject your offer</h3>");
        } else {
            sb.append(" rejects your offer</h3>");
        }
        Boolean isLipVLr  = caseData.isLipvLROneVOne();
        if (isLipVLr) {
            sb.append("<p>If the claim value is below £10,000 then the next step will be mediation. ")
                .append("The mediation service will contact you to give you a date for your appointment. ")
                .append("If you can not reach an agreement at mediation, the court will review your claim.</p>")
                .append(
                    "<p>If the claim value is greater than £10,000 then the court will review the case for the full amount.</p>")
                .append("<p>This case will now proceed offline.</p>");
        } else {
            sb.append("The court will decide how you must pay");
            if (caseData.isApplicant1NotRepresented()) {
                sb.append("<br></br>")
                    .append("<p>This case will now proceed offline.</p>");
            }
        }
        return Optional.of(sb.toString());
    }
}

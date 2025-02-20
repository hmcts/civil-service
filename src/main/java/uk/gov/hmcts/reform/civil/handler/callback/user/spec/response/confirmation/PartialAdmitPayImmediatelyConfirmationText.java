package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
public class PartialAdmitPayImmediatelyConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {
    //TODO add the featureFlag
    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        boolean isLipVLr  = caseData.isLipvLROneVOne();

        if (!RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY.equals(
            caseData.getDefenceAdmitPartPaymentTimeRouteRequired())) {
            return Optional.empty();
        }
        LocalDate whenBePaid = Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
            .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid)
            .orElse(null);
        if (whenBePaid == null) {
            throw new IllegalStateException("Unable to format the payment date.");
        }

        String formattedWhenBePaid = formatLocalDate(whenBePaid, DATE);

        String applicantName = caseData.getApplicant1().getPartyName();

        BigDecimal totalClaimAmount = new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount()));

        StringBuilder sb = new StringBuilder();
        sb.append("<br>We've emailed ").append(applicantName)
            .append(" to say you will pay immediately. ")
            .append(isLipVLr ? " We'll contact you when they respond." : "")
            .append("<h2 class=\"govuk-heading-m\">What you need to do:</h2>")
            .append("<ul>")
            .append("<li>pay ").append(applicantName).append(" By ")
            .append(formattedWhenBePaid).append("</li>")
            .append("<li>keep proof of any payments you make</li>")
            .append("<li>make sure ").append(applicantName).append(" tells the court that you've paid").append("</li>")
            .append("</ul>");
        if (caseData.getRespondent2() == null && caseData.getApplicant2() == null
            && !RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            sb.append("<h3 class=\"govuk-heading-m\">If you do not pay: </h3>")
                .append("<p>If you do not pay, the claimant can request a County Court Judgment against you.</p>");
        }
        sb.append("<p>Contact ")
            .append(applicantName);
        if (applicantName.endsWith("s")) {
            sb.append("'");
        } else {
            sb.append("'s");
        }

        sb.append(" legal representative if you need details on how to pay.</p>");
        if (isLipVLr) {
            sb.append("<h2 class=\"govuk-heading-m\">If ").append(applicantName).append( "accepts your offer of ").append(totalClaimAmount).append("</h2>");
            sb.append("<p>The claim will be settled. </p>");
            sb.append("<h2 class=\"govuk-heading-m\">If ").append(applicantName).append( " rejects your offer").append("</h2>");
            sb.append("<p>If the claim value is below £10,000 then the next step will be mediation. The mediation service will contact you to give you a date for your appointment.  If you can not reach an agreement at mediation, the court will review your claim.</p>");
            sb.append("<p>If the claim value is greater than £10,000 then the court will review the case for the full amount of ").append(totalClaimAmount).append(".</p>");
            sb.append("<p>This case will now proceed offline.").append(totalClaimAmount).append(".</p>");
        }

        return Optional.of(sb.toString());
    }
}

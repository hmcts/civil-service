package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

@Component
public class FullAdmitSetDateConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || !YesOrNo.NO.equals(caseData.getSpecDefenceFullAdmittedRequired())
            || !RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE.equals(
            caseData.getDefenceAdmitPartPaymentTimeRouteRequired())) {
            return Optional.empty();
        }

        LocalDate whenWillYouPay = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();

        String applicantName = caseData.getApplicant1().getPartyName();
        if (caseData.getApplicant2() != null) {
            applicantName += " and " + caseData.getApplicant2().getPartyName();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<br>We've emailed ")
            .append(applicantName)
            .append(" your offer to pay by ")
            .append(DateFormatHelper.formatLocalDate(whenWillYouPay, DATE))
            .append(" and your explanation of why you cannot pay before then.")
            .append("<br><br>We'll contact you when ").append(applicantName).append(" responds.");

        sb.append("<h2 class=\"govuk-heading-m\">What happens next</h2>")
            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" accept your offer</h3>");
        } else {
            sb.append(" accepts your offer</h3>");
        }
        sb.append("<ul>")
            .append("<li>pay ").append(applicantName).append(" by ")
            .append(DateFormatHelper.formatLocalDate(whenWillYouPay, DATE)).append("</li>")
            .append("<li>keep proof of any payments you make</li>")
            .append("<li>make sure ").append(applicantName).append(" tells the court that you've paid</li>")
            .append("</ul>")
            .append("<p>Contact ")
            .append(applicantName);
        if (applicantName.endsWith("s")) {
            sb.append("'");
        } else {
            sb.append("'s");
        }
        sb.append(" legal representative if you need details on how to pay.</p>")
            .append("<p>If you do not pay immediately, ").append(applicantName)
            .append(" can either:</p>")
            .append("<ul>")
            .append("<li>ask you to sign a settlement agreement to formalise the repayment plan</li>")
            .append("<li>request a county court judgment against you</li>")
            .append("</ul>")

            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" reject your offer</h3>");
        } else {
            sb.append(" rejects your offer</h3>");
        }
        sb.append("<ul>")
            .append("<li>the court will decide how you must pay</li>")
            .append("</ul>");
        return Optional.of(sb.toString());
    }
}

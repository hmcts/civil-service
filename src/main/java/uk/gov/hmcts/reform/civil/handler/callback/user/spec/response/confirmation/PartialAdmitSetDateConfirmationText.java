package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Component
public class PartialAdmitSetDateConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    /**
     * If applicable, creates the confirmation text for a case with partial admission, an amount still owed,
     * and an offer to pay by a set date.
     *
     * @param caseData a case data
     * @return the confirmation body if the case is a partial admission, the respondent hasn't paid yet,
     *     the respondent wants to pay by a set date and all fields needed for the text are available.
     */
    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || !RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE.equals(
            caseData.getDefenceAdmitPartPaymentTimeRouteRequired())) {
            return Optional.empty();
        }
        BigDecimal admitOwed = caseData.getRespondToAdmittedClaimOwingAmountPounds();
        LocalDate whenWillYouPay = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
        BigDecimal totalClaimAmount = caseData.getTotalClaimAmount();
        if (Stream.of(admitOwed, whenWillYouPay, totalClaimAmount)
            .anyMatch(Objects::isNull)) {
            return Optional.empty();
        }
        String applicantName = caseData.getApplicant1().getPartyName();
        if (caseData.getApplicant2() != null) {
            applicantName += " and " + caseData.getApplicant2().getPartyName();
        }

        final String headingThreeText = "<h3 class=\"govuk-heading-m\">If ";
        StringBuilder sb = new StringBuilder();
        sb.append("<br>You believe you owe &#163;").append(admitOwed).append(
                ". We've emailed ").append(applicantName)
            .append(" your offer to pay by ")
            .append(DateFormatHelper.formatLocalDate(whenWillYouPay, DATE));
        if (admitOwed.compareTo(totalClaimAmount) < 0) {
            sb.append(" and your explanation of why you do not owe the full amount.");
        }

        sb.append("<br><br>").append("The claimant has until 4pm on ")
            .append(formatLocalDateTime(caseData.getApplicant1ResponseDeadline(), DATE))
            .append(" to respond to your claim. <br>We will let you know when they respond.")
            .append(String
                        .format(
                            "%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
                            format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
                        ));

        sb.append("<h2 class=\"govuk-heading-m\">What happens next</h2>")

            .append(headingThreeText)
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" accept your offer</h3>");
        } else {
            sb.append(" accepts your offer</h3>");
        }
        sb.append("<ul>")
            .append("<li><p class=\"govuk-!-margin-0\">pay ").append(applicantName).append("</p></li>")
            .append("<li><p class=\"govuk-!-margin-0\">make sure any cheques or bank transfers are clear in their account by the deadline</p></li>")
            .append("<li><p class=\"govuk-!-margin-0\">keep proof of any payments you make</p></li>")
            .append("</ul>")
            .append("<p>Contact ")
            .append(applicantName);
        if (applicantName.endsWith("s")) {
            sb.append("'");
        } else {
            sb.append("'s");
        }
        final String P_TAG = ".</p>";
        sb.append(" legal representative if you need details on how to pay.</p>")

            .append("<p>Because you've said you will not pay immediately, ")
            .append(applicantName)
            .append(" can request a county court judgment against you for &#163;")
            .append(admitOwed).append(P_TAG)

            .append(headingThreeText)
            .append(applicantName)
            .append(" disagrees that you only owe &#163;")
            .append(admitOwed)
            .append("</h3>");
        if (caseData.hasDefendantAgreedToFreeMediation()) {
            sb.append("<p>We'll ask if they want to try mediation. ")
              .append("If they agree we'll contact you to arrange a call with the mediator.</p>")
              .append(
                        "<p>If they do not want to try mediation the court will review the case for the full amount of &#163;")
                    .append(totalClaimAmount).append(P_TAG);
        } else {
            sb.append("<p>The court will review the case for the full amount of &#163;")
              .append(totalClaimAmount).append(P_TAG);
        }
        sb.append(headingThreeText)
            .append(applicantName)
            .append(" rejects your offer to pay by ")
            .append(DateFormatHelper.formatLocalDate(whenWillYouPay, DATE))
            .append("</h3>");
        Boolean isLipVLR = caseData.isLipvLROneVOne();

        if (isLipVLR) {
            sb.append("<p>If the claim value is below £10,000 then the next step will be mediation. ")
                .append("The mediation service will contact you to give you a date for your appointment. ")
                .append("If you can not reach an agreement at mediation, the court will review your claim.</p>")
                .append("<p>If the claim value is greater than £10,000 then the court will review the case for the full amount.</p>")
                .append("<p>This case will now proceed offline.</p>");
        } else {
            sb.append("<p>The court will decide how you must pay</p>");
        }
        return Optional.of(sb.toString());
    }
}

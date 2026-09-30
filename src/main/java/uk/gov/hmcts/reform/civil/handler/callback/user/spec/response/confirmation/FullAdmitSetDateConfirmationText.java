package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

@Component
public class FullAdmitSetDateConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData, FeatureToggleService featureToggleService) {
        // Do not gate on SpecDefenceFullAdmitted* — often unset on FULL_ADMISSION.
        // Already-paid is a separate generator (YES on that flag). Identify set-date by
        // FULL_ADMISSION + BY_SET_DATE on the current defendant's own populated fields.
        boolean respondent2 = caseData.isCurrentDefendantRespondent2();
        RespondentResponseTypeSpec responseType;
        RespondentResponsePartAdmissionPaymentTimeLRspec paymentTimeRoute;
        RespondToClaimAdmitPartLRspec admitPart;
        if (respondent2) {
            responseType = caseData.getRespondent2ClaimResponseTypeForSpec();
            paymentTimeRoute = caseData.getDefenceAdmitPartPaymentTimeRouteRequired2();
            admitPart = caseData.getRespondToClaimAdmitPartLRspec2();
        } else if (YesOrNo.YES.equals(caseData.getIsRespondent1())
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            // Prefer R1's own response over generic (generic may reflect the other defendant)
            responseType = caseData.getRespondent1ClaimResponseTypeForSpec();
            paymentTimeRoute = caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
                ? caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                : caseData.getDefenceAdmitPartPaymentTimeRouteGeneric();
            admitPart = caseData.getRespondToClaimAdmitPartLRspec();
        } else {
            responseType = Optional.ofNullable(caseData.getRespondentClaimResponseTypeForSpecGeneric())
                .orElse(caseData.getRespondent1ClaimResponseTypeForSpec());
            paymentTimeRoute = Optional.ofNullable(caseData.getDefenceAdmitPartPaymentTimeRouteGeneric())
                .orElse(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            admitPart = caseData.getRespondToClaimAdmitPartLRspec();
        }

        if (!RespondentResponseTypeSpec.FULL_ADMISSION.equals(responseType)
            || !RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE.equals(paymentTimeRoute)) {
            return Optional.empty();
        }

        LocalDate whenWillYouPay = Optional.ofNullable(admitPart)
            .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid)
            .orElse(null);
        if (whenWillYouPay == null) {
            return Optional.empty();
        }

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
            .append("<li><p class=\"govuk-!-margin-0\">pay ").append(applicantName).append(" by ")
            .append(DateFormatHelper.formatLocalDate(whenWillYouPay, DATE)).append("</p></li>")
            .append("<li><p class=\"govuk-!-margin-0\">keep proof of any payments you make</p></li>")
            .append("<li><p class=\"govuk-!-margin-0\">make sure ").append(applicantName).append(" tells the court that you've paid</p></li>")
            .append("</ul>")
            .append("<p>Contact ")
            .append(applicantName);

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

        sb.append("<p>If you do not pay immediately, ").append(applicantName)
            .append(" can request a county court judgment against you.</p>")
            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" reject your offer</h3>");
        } else {
            sb.append(" rejects your offer</h3>");
        }
        sb.append("<ul>")
            .append("<li><p class=\"govuk-!-margin-0\">the court will decide how you must pay</p></li>")
            .append("</ul>");
        if (caseData.isApplicant1NotRepresented()) {
            sb.append("<p>This case will now proceed offline.</p>");
        }
        return Optional.of(sb.toString());
    }
}

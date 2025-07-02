package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Component
public class PartialAdmitPayImmediatelyConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData, FeatureToggleService featureToggleService) {

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
        boolean isPartAdmitLRAdmissionBulk = checkLrAdmissionBulk(caseData, featureToggleService);
        BigDecimal claimOwingAmount = getClaimOwingAmount(caseData, isPartAdmitLRAdmissionBulk);
        String applicantName = caseData.getApplicant1().getPartyName();
        Boolean isLipVLr  = caseData.isLipvLROneVOne();

        if (isPartAdmitLRAdmissionBulk) {
            return Optional.of(getPartAdmitLrAdmissionBulkConfirmationText(caseData, claimOwingAmount));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<br>We've emailed ").append(applicantName)
            .append(" to say you will pay immediately. ")
            .append(isLipVLr ? " We'll contact you when they respond." : "")
            .append("<h2 class=\"govuk-heading-m\">What you need to do:</h2>")
            .append("<ul>")
            .append("<li><p class=\"govuk-!-margin-0\">pay ").append(applicantName).append(" By ")
            .append(formattedWhenBePaid).append("</p></li>")
            .append("<li><p class=\"govuk-!-margin-0\">keep proof of any payments you make</p></li>")
            .append("<li><p class=\"govuk-!-margin-0\">make sure ").append(applicantName).append(" tells the court that you've paid").append("</p></li>")
            .append("</ul>");
        if (caseData.getRespondent2() == null && caseData.getApplicant2() == null
            && !RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            sb.append("<h3 class=\"govuk-heading-m\">If you do not pay: </h3>")
                .append("<p>If you do not pay, the claimant can request a County Court Judgment against you.</p>");
        }
        sb.append("<p>Contact ")
            .append(applicantName);

        if (!caseData.isApplicant1NotRepresented()) {
            if (applicantName.endsWith("s")) {
                sb.append("'");
            } else {
                sb.append("'s");
            }
            sb.append(" legal representative if you need details on how to pay.</p>");
        } else {
            sb.append(" if you need details on how to pay.</p>");
        }

        boolean isFullAdmission = RespondentResponseTypeSpec.FULL_ADMISSION.equals(
            caseData.getRespondentClaimResponseTypeForSpecGeneric()
        );

        if (isNull(claimOwingAmount) && isFullAdmission) {
            claimOwingAmount = caseData.getTotalClaimAmount();
        }

        if (isLipVLr) {
            sb.append("<h2 class=\"govuk-heading-m\">If ").append(applicantName).append(" accepts your offer of &#163;")
                .append(claimOwingAmount)
                .append("</h2>");
            sb.append("<p>The claim will be settled.</p>");
            sb.append("<h2 class=\"govuk-heading-m\">If ")
                .append(applicantName)
                .append(" rejects your offer")
                .append("</h2>");
            sb.append("<p>If the claim value is below £10,000 then the next step will be mediation.")
                .append("The mediation service will contact you to give you a date for your appointment.  ")
                .append("If you can not reach an agreement at mediation, the court will review your claim.</p>");
            sb.append("<p>If the claim value is greater than £10,000 then the court will review the case for the full amount.</p>");
            sb.append("<p>This case will now proceed offline.</p>");
        }

        return Optional.of(sb.toString());
    }

    private boolean checkLrAdmissionBulk(CaseData caseData, FeatureToggleService featureToggleService) {

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        boolean isRespondent1PartAdmission = RespondentResponseTypeSpec.PART_ADMISSION.equals(
            caseData.getRespondentClaimResponseTypeForSpecGeneric()
        );

        boolean isRespondent2PartAdmission = RespondentResponseTypeSpec.PART_ADMISSION.equals(
            caseData.getRespondent2ClaimResponseTypeForSpec()
        );

        boolean isPartAdmission = isRespondent1PartAdmission;

        if ((ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario) && YesOrNo.YES.equals(caseData.getIsRespondent2()))) {
            isPartAdmission = isRespondent2PartAdmission;
        } else if ((ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario) && caseData.getRespondentResponseIsSame().equals(NO))) {
            isPartAdmission = false;
        }

        return featureToggleService.isLrAdmissionBulkEnabled() && isPartAdmission;
    }

    private BigDecimal getClaimOwingAmount(CaseData caseData, boolean isPartLRAdmission) {
        BigDecimal claimOwingAmount = caseData.getRespondToAdmittedClaimOwingAmountPounds();

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        if (isPartLRAdmission) {
            if ((ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario) && YesOrNo.YES.equals(caseData.getIsRespondent2()))) {
                claimOwingAmount = caseData.getRespondToAdmittedClaimOwingAmountPounds2();
            }
        }
        return claimOwingAmount;
    }

    private String getPartAdmitLrAdmissionBulkConfirmationText(CaseData caseData, BigDecimal claimOwingAmount) {

        String applicantName = caseData.getApplicant1().getPartyName();

        final String headingThreeText = "<h3 class=\"govuk-heading-m\">If ";
        StringBuilder sb = new StringBuilder();
        sb.append("<br>You believe you owe &#163;").append(claimOwingAmount).append(
                ". <br>We've emailed ").append(applicantName)
            .append(" to say you will pay immediately. ");

        sb.append("<br><br>").append("The claimant has until 4pm on ")
            .append(formatLocalDateTime(caseData.getApplicant1ResponseDeadline(), DATE))
            .append(" to respond to your claim. <br>We will let you know when they respond.");

        sb.append("<h2 class=\"govuk-heading-m\">What happens next</h2>")
            .append(headingThreeText)
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" accept your offer</h3>");
        } else {
            sb.append(" accepts your offer</h3>");
        }
        sb.append("<ul>")
            .append("<li><p class=\"govuk-!-margin-0\">pay ").append(applicantName).append(" within 5 days of the response").append("</p></li>")
            .append("<li><p class=\"govuk-!-margin-0\">make sure any cheques or bank transfers are clear in their account by the deadline</p></li>")
            .append("<li><p class=\"govuk-!-margin-0\">keep proof of any payments you make</p></li>")
            .append("</ul>");

        sb.append(headingThreeText)
            .append("you do not pay:</h3>")
            .append("<p>If you do not pay within 5 days of the response, the claimant can request a County Court Judgment against you.")
            .append("<br><br>").append("Contact ")
            .append(applicantName)
            .append("'s legal representative if you need details on how to pay.</p>");

        return sb.toString();
    }
}

package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class PartialAdmitPaidFullConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData, FeatureToggleService featureToggleService) {
        if (!RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || NO.equals(caseData.getSpecDefenceAdmittedRequired())) {
            return Optional.empty();
        }
        BigDecimal howMuchWasPaid = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
            .map(RespondToClaim::getHowMuchWasPaid).orElse(null);
        BigDecimal totalClaimAmount = caseData.getTotalClaimAmount();
        if (Stream.of(howMuchWasPaid, totalClaimAmount)
            .anyMatch(Objects::isNull)) {
            return Optional.empty();
        }

        if (new BigDecimal(MonetaryConversions.poundsToPennies(totalClaimAmount)).compareTo(howMuchWasPaid) > 0) {
            return Optional.empty();
        }

        String applicantName = caseData.getApplicant1().getPartyName();

        StringBuilder sb = new StringBuilder();
        sb.append("<br>You told us you've paid the &#163;")
            .append(totalClaimAmount)
            .append(" you believe you owe. We've sent ")
            .append(applicantName)
            .append(" this response.")
            .append("<h2 class=\"govuk-heading-m\">What happens next</h2>")
            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName)
            .append(" accepts your response</h3>")
            .append("<p>The claim will be settled.</p>")
            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName)
            .append(" rejects your response</h3>");
        Boolean isLipVLr  = caseData.isLipvLROneVOne();
        if (isLipVLr) {
            sb.append("<p>If the claim value is below £10,000 then the next step will be mediation. ")
                .append("The mediation service will contact you to give you a date for your appointment. ")
                .append("If you can not reach an agreement at mediation, the court will review your claim.</p>")
                .append("<p>If the claim value is greater than £10,000 then the court will review the case for the full amount.</p>")
                .append("<p>This case will now proceed offline.</p>");
        } else {
            sb.append("<p>The court will review the case. You may have to go to a hearing.</p>")
                .append("<p>We'll contact you to tell you what to do next.</p>");
        }

        return Optional.of(sb.toString());
    }
}

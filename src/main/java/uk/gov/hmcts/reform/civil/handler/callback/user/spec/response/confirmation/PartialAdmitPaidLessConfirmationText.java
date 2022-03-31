package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class PartialAdmitPaidLessConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
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

        if (howMuchWasPaid.compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(totalClaimAmount))) >= 0) {
            return Optional.empty();
        }
        String applicantName = caseData.getApplicant1().getPartyName();

        String sb = "<br>You told us you've paid the &#163;"
            + MonetaryConversions.penniesToPounds(howMuchWasPaid)
            + ". We've sent "
            + applicantName
            + " this response."
            + "<h2 class=\"govuk-heading-m\">What happens next</h2>"
            + "<h3 class=\"govuk-heading-m\">If "
            + applicantName + " accepts your response</h3>"
            + "<p>The claim will be settled. We'll contact you when they respond.</p>"
            + "<h3 class=\"govuk-heading-m\">If "
            + applicantName + " rejects your response</h3>"
            + "<p>The court will review the case. You may have to go to a hearing.</p>"
            + "<p>We'll contact you to tell you what to do next.</p>";
        return Optional.of(sb);
    }
}

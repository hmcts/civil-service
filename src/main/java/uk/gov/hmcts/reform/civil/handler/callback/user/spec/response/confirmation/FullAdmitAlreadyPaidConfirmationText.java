package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Optional;

@Component
public class FullAdmitAlreadyPaidConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData, FeatureToggleService featureToggleService) {
        // Match only when defendant explicitly said already paid (YES) on their own field.
        // Complements FullAdmitSetDateConfirmationText (FULL_ADMISSION + BY_SET_DATE, no admitted flag).
        boolean respondent2 = caseData.isCurrentDefendantRespondent2();
        RespondentResponseTypeSpec responseType;
        YesOrNo fullAdmittedRequired;
        if (respondent2) {
            responseType = caseData.getRespondent2ClaimResponseTypeForSpec();
            fullAdmittedRequired = caseData.getSpecDefenceFullAdmitted2Required();
        } else if (YesOrNo.YES.equals(caseData.getIsRespondent1())
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            responseType = caseData.getRespondent1ClaimResponseTypeForSpec();
            fullAdmittedRequired = caseData.getSpecDefenceFullAdmittedRequired();
        } else {
            responseType = Optional.ofNullable(caseData.getRespondentClaimResponseTypeForSpecGeneric())
                .orElse(caseData.getRespondent1ClaimResponseTypeForSpec());
            fullAdmittedRequired = caseData.getSpecDefenceFullAdmittedRequired();
        }

        if (!RespondentResponseTypeSpec.FULL_ADMISSION.equals(responseType)
            || !YesOrNo.YES.equals(fullAdmittedRequired)) {
            return Optional.empty();
        }

        String applicantName = caseData.getApplicant1().getPartyName();
        if (caseData.getApplicant2() != null) {
            applicantName += " and " + caseData.getApplicant2().getPartyName();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<br>You told us you've paid the &#163;").append(caseData.getTotalClaimAmount())
            .append(" you believe you owe. We've sent ")
            .append(applicantName)
            .append(" this response.")

            .append("<h2 class=\"govuk-heading-m\">What happens next</h2>")
            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" accept your response</h3>");
        } else {
            sb.append(" accepts your response</h3>");
        }
        sb.append("<p>The claim will be settled</p>")

            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" reject your response</h3>");
        } else {
            sb.append(" rejects your response</h3>");
        }
        sb.append("<p>The court will review the case. You may have to go to a hearing.")
            .append("<br><br>We'll contact you to tell you what to do next</p>");
        return Optional.of(sb.toString());
    }
}

package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Optional;

@Component
public class FullAdmitAlreadyPaidConfirmationText implements RespondToClaimConfirmationTextSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData, FeatureToggleService featureToggleService) {
        boolean currentRespondentFullAdmission;
        boolean currentRespondentDefenceFullyAdmitted;

        if (YES.equals(caseData.getIsRespondent1())) {
            currentRespondentFullAdmission =
                RespondentResponseTypeSpec.FULL_ADMISSION.equals(
                    caseData.getRespondent1ClaimResponseTypeForSpec());
            currentRespondentDefenceFullyAdmitted =
                YES.equals(caseData.getSpecDefenceFullAdmittedRequired());

        } else if (YES.equals(caseData.getIsRespondent2())) {
            currentRespondentFullAdmission =
                RespondentResponseTypeSpec.FULL_ADMISSION.equals(
                    caseData.getRespondent2ClaimResponseTypeForSpec());
            currentRespondentDefenceFullyAdmitted =
                YES.equals(caseData.getSpecDefenceFullAdmitted2Required());

        } else {
            currentRespondentFullAdmission =
                RespondentResponseTypeSpec.FULL_ADMISSION.equals(
                    caseData.getRespondent1ClaimResponseTypeForSpec())
                    || RespondentResponseTypeSpec.FULL_ADMISSION.equals(
                    caseData.getRespondent2ClaimResponseTypeForSpec());

            currentRespondentDefenceFullyAdmitted =
                YES.equals(caseData.getSpecDefenceFullAdmittedRequired())
                    || YES.equals(caseData.getSpecDefenceFullAdmitted2Required());
        }

        if (!currentRespondentFullAdmission || !currentRespondentDefenceFullyAdmitted) {
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

package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
@RequiredArgsConstructor
public class PayImmediatelyConfText implements RespondToResponseConfirmationTextGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!isDefendantFullAdmitPayImmediately(caseData)) {
            return Optional.empty();
        }
        LocalDate whenBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
        String formattedWhenBePaid = formatLocalDate(whenBePaid, DATE);
        return Optional.of(format(
            "They must make sure you have the money by %s. "
                + "Any cheques or transfers should be clear in your account"
                + "<p>You need to tell us if you’ve settled the claim, for example because the defendant has paid you.</p>"
                + "<p>You can settle for less than the full claim amount.</p>"
                + "<p><h3>If you haven’t been paid.</h3></p>"
                + "<p>If the defendant has not paid you, you can request a County Court Judgment "
                + "by completing the following form and sending it to the email address below.</p>"
                + "<p><li><a href=\"%s\" target=\"_blank\">N225 </a>- Ask for judgment on a claim for a specified amount of money</li></ul></p>"
                + "<p>Email: <a href=\"mailto:contactocmc@justice.gov.uk\">contactocmc@justice.gov.uk</a></p>",
            formattedWhenBePaid,
            "https://formfinder.hmctsformfinder.justice.gov.uk/n225-eng.pdf"
        ));
    }

    private boolean isDefendantFullAdmitPayImmediately(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            &&  IMMEDIATELY.equals(caseData.getDefenceAdmitPartPaymentTimeRouteRequired())
            && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            && null == caseData.getApplicant1ProceedWithClaim();
    }
}

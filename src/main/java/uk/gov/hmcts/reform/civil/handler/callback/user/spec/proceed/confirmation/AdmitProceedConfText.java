package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
@RequiredArgsConstructor
public class AdmitProceedConfText implements RespondToResponseConfirmationTextGenerator {

    private static final Set<RespondentResponseTypeSpec> ADMISSION = EnumSet.of(
        RespondentResponseTypeSpec.FULL_ADMISSION,
        RespondentResponseTypeSpec.PART_ADMISSION
    );

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (isdefendatFullAdmitPayImmidietely(caseData)) {
            LocalDate whenBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
            String formattedWhenBePaid = formatLocalDate(whenBePaid, DATE);
            return Optional.ofNullable(format(
                      "They must make sure you have the money by %s. "
                    + "Any cheques or transfers should be clear in your account."
                    + "<p>You need to tell us if you've settled the claim, for example because the defenrant has paid you.</p>"
                    + "<p>You can settle for less than the full claim amount.</p>"
                    + "<p><h3>If you haven't been paid.</h3></p>"
                    + "<p>If the defentand has not paid you, you can request a County Court Judgement"
                    + "by completing the following form and sending it to the email address below.</p>"
                    + "<p><li><a href=\"%s\" target=\"_blank\">N225 </a>- Ask for judgement on a claim for a specified amount of money</li></ul></p>"
                    + "<p>Email: <a href=\"mailto:contactocmc@justice.gov.uk\">contactocmc@justice.gov.uk</a></p>",
                      formattedWhenBePaid,
                      "https://formfinder.hmctsformfinder.justice.gov.uk/n225-eng.pdf"
            ));
        } else if (YesOrNo.NO.equals(caseData.getApplicant1ProceedsWithClaimSpec())
            || !ADMISSION.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            return Optional.empty();
        }
        return Optional.of("<br>You've chosen to proceed with the claim.&nbsp;"
                               + "This means that your claim cannot continue online."
                               + "<br>We'll review the case and contact you about what to do next");
    }

    private boolean isdefendatFullAdmitPayImmidietely(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            &&  caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()));
    }
}

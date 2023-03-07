package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

@Component
public class JudgmentSubmittedConfText implements RespondToResponseConfirmationTextGenerator {

    private static final Set<RespondentResponsePartAdmissionPaymentTimeLRspec> PAYMENT_PLAN = EnumSet.of(
        RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
        RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE
    );

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if ((YesOrNo.NO.equals(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec()))
            || (YesOrNo.NO.equals(caseData.getApplicant1AcceptPartAdmitPaymentPlanSpec()))
            || !PAYMENT_PLAN.contains(caseData.getDefenceAdmitPartPaymentTimeRouteRequired())) {
            return Optional.empty();
        }
        return Optional.of(format(
            "<br /><h2 class=\"govuk-heading-m\"><u>What happens next</u></h2>"
                + "<br>This case will now proceed offline. Any updates will be sent by post.<br><br>"
        ));
    }
}


package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

@Component
public class JudgmentSubmittedConfHeader  implements RespondToResponseConfirmationHeaderGenerator {

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

        String claimNumber = caseData.getLegacyCaseReference();
        return Optional.of(format(
            "# Judgment Submitted %n## A county court judgment(ccj) has been submitted for case %s",
            claimNumber
        ));
    }
}


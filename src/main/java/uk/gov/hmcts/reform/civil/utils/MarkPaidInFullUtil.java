package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;

public class MarkPaidInFullUtil {

    private MarkPaidInFullUtil() {
        //no op
    }

    public static boolean checkMarkPaidInFull(CaseData data) {
        return (Objects.nonNull(data.getActiveJudgment())
            && data.getActiveJudgment().getFullyPaymentMadeDate() != null
            && Objects.nonNull(data.getCertOfSC())
            && data.getCertOfSC().getDefendantFinalPaymentDate() != null);
    }
}

package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.cosc.CoscApplicationStatus.ACTIVE;
import static uk.gov.hmcts.reform.civil.enums.cosc.CoscApplicationStatus.PROCESSED;

public class MarkPaidInFullUtil {

    private MarkPaidInFullUtil() {
        //no op
    }

    public static boolean checkMarkPaidInFull(CaseData data) {
        return (Objects.nonNull(data.getActiveJudgment())
            && data.getActiveJudgment().getFullyPaymentMadeDate() != null
            && Objects.nonNull(data.getCoSCApplicationStatus())
            && ACTIVE.equals(data.getCoSCApplicationStatus()));
    }

    public static boolean checkMarkPaidInFullAndPaidForApplication(CaseData data) {
        return (Objects.nonNull(data.getActiveJudgment())
            && data.getActiveJudgment().getFullyPaymentMadeDate() != null
            && Objects.nonNull(data.getCoSCApplicationStatus())
            && PROCESSED.equals(data.getCoSCApplicationStatus()));
    }
}

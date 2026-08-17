package uk.gov.hmcts.reform.civil.model.judgmentonline;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JoJudgmentRecordReasonList", generate = true)
public enum JudgmentRecordedReason {
    @CCD(label = "A determination of means has been made")
    DETERMINATION_OF_MEANS,
    @CCD(label = "A judge has made an order")
    JUDGE_ORDER
}

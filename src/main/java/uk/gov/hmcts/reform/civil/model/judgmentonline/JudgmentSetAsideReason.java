package uk.gov.hmcts.reform.civil.model.judgmentonline;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JoSetAsideReasonList", generate = true)
public enum JudgmentSetAsideReason {
    @CCD(label = "A judge has made an order")
    JUDGE_ORDER,
    @CCD(label = "A judgment has been made in error")
    JUDGMENT_ERROR,
    
}

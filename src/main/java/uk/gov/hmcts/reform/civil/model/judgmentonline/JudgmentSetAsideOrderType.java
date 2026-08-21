package uk.gov.hmcts.reform.civil.model.judgmentonline;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JoSetAsideOrderTypeList", generate = true)
public enum JudgmentSetAsideOrderType {

    @CCD(label = "Order following an application to set aside")
    ORDER_AFTER_APPLICATION,
    @CCD(label = "Order following defence received")
    ORDER_AFTER_DEFENCE
}

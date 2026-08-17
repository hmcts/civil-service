package uk.gov.hmcts.reform.civil.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DecisionOnRequestReconsiderationOptionList", generate = true)
public enum DecisionOnRequestReconsiderationOptions {

    @CCD(label = "Yes")
    YES,
    @CCD(label = "No, create a new SDO")
    CREATE_SDO,
    @CCD(label = "No, previous order needs amending")
    CREATE_GENERAL_ORDER
}

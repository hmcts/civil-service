package uk.gov.hmcts.reform.civil.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ConfirmReferToJudgeDefenceReceived", generate = true)
public enum ConfirmationToggle {
    @CCD(label = "I confirm that I want to refer this decision to a judge")
    CONFIRM
}

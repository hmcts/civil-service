package uk.gov.hmcts.reform.civil.model.transferonlinecase;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ReasonNotSuitableSDOList", generate = true)
@Getter
public enum NotSuitableSdoOptions {
    @CCD(label = "The case should be sent to another hearing centre for directions")
    CHANGE_LOCATION,
    @CCD(label = "Other reason")
    OTHER_REASONS
}

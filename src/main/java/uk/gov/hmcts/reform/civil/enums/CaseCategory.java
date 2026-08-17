package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "CaseAccessCategory", generate = true)
@Getter
@RequiredArgsConstructor
public enum CaseCategory {
    UNSPEC_CLAIM,
    SPEC_CLAIM;
}

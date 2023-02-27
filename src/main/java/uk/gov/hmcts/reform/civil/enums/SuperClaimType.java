package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Deprecated
//Use CaseCategory instead
public enum SuperClaimType {
    UNSPEC_CLAIM,
    SPEC_CLAIM;
}

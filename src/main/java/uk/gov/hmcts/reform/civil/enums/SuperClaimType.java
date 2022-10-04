package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * spec and unspec claim.
 *
 * @deprecated replaced by CaseCategory
 */
@Getter
@RequiredArgsConstructor
@Deprecated
public enum SuperClaimType {
    UNSPEC_CLAIM,
    SPEC_CLAIM;
}

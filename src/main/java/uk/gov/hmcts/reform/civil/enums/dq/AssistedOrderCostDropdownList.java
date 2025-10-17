package uk.gov.hmcts.reform.civil.enums.dq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssistedOrderCostDropdownList {

    COSTS,
    SUBJECT_DETAILED_ASSESSMENT,
    STANDARD_BASIS,
    INDEMNITY_BASIS,
    YES,
    NO,
    CLAIMANT,
    DEFENDANT
}

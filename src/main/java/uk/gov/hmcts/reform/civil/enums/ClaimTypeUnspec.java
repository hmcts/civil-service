package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimTypeUnspec {
    PERSONAL_INJURY,
    CLINICAL_NEGLIGENCE,
    PROFESSIONAL_NEGLIGENCE,
    BREACH_OF_CONTRACT,
    CONSUMER,
    CONSUMER_CREDIT,
    OTHER;
}

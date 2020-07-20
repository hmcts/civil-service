package uk.gov.hmcts.reform.unspec.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimType {
    PERSONAL_INJURY_ROAD(SubType.PERSONAL_INJURY),
    PERSONAL_INJURY_WORK(SubType.PERSONAL_INJURY),
    PERSONAL_INJURY_PUBLIC(SubType.PERSONAL_INJURY),
    PERSONAL_INJURY_HOLIDAY(SubType.PERSONAL_INJURY),
    PERSONAL_INJURY_DISEASE(SubType.PERSONAL_INJURY),
    PERSONAL_INJURY_OTHER(SubType.PERSONAL_INJURY),
    CLINICAL_NEGLIGENCE(SubType.PERSONAL_INJURY),
    BREACH_OF_CONTRACT(SubType.OTHER),
    CONSUMER_CREDIT(SubType.OTHER),
    OTHER(SubType.OTHER);

    private final SubType subType;

    public enum SubType {
        PERSONAL_INJURY,
        OTHER
    }

    public boolean isPersonalInjury() {
        return this.subType.equals(SubType.PERSONAL_INJURY);
    }
}

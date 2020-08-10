package uk.gov.hmcts.reform.unspec.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceLocationType {
    RESIDENCE("Usual residence"),
    BUSINESS("Place of business"),
    OTHER("Other");

    private final String label;
}

package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentHearingType {
    TRI("trial"),
    DIS("hearing"),
    DRH("hearing");

    private final String label;

    /**
     * Gets the {@link DocumentHearingType} based on the provided hearing type string.
     *
     * @param hearingType The hearing type string (e.g., "AAA7-TRI").
     * @return The corresponding {@link DocumentHearingType}.
     * @throws IllegalArgumentException If an unexpected hearing type is received.
     */
    public static DocumentHearingType getType(String hearingType) {
        String[] parts = hearingType.split("-");
        try {
            return DocumentHearingType.valueOf(parts.length == 2 ? parts[1] : hearingType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Unexpected hearing type received: %s", hearingType));
        }
    }
}

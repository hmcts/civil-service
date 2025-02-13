package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocumentContext.CONTENT;
import static uk.gov.hmcts.reform.civil.enums.DocumentContext.TITLE;

@Getter
@RequiredArgsConstructor
public enum DocumentHearingType {
    TRI("trial", "dreial"),
    DIS("disposal hearing", ""),
    DRH("dispute resolution hearing", "");

    private final String label;
    private final String labelWelsh;

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

    private static String getTypeText(DocumentHearingType documentHearingType, AllocatedTrack allocatedTrack, DocumentContext context) {
        if (documentHearingType.equals(TRI)) {
            return allocatedTrack.equals(FAST_CLAIM) ? documentHearingType.getLabel() : "hearing";
        }
        return context.equals(TITLE) ? documentHearingType.getLabel() : "hearing";
    }

    public static String getTitleText(DocumentHearingType hearingType, AllocatedTrack allocatedTrack) {
        return getTypeText(hearingType, allocatedTrack, TITLE);
    }

    public static String getContentText(DocumentHearingType hearingType, AllocatedTrack allocatedTrack) {
        return getTypeText(hearingType, allocatedTrack, CONTENT);
    }

}

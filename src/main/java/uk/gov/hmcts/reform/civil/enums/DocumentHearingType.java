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
    DIS("disposal hearing", "wrandawiad gwaredu"),
    DRH("dispute resolution hearing", "wrandawiad datrys anghydfod");

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

    private static String getTypeText(DocumentHearingType documentHearingType, AllocatedTrack allocatedTrack, DocumentContext context, boolean isWelsh) {
        String labelText;
        String hearingText;

        if (documentHearingType.equals(TRI) && !isWelsh) {
            labelText = documentHearingType.getLabel();
            hearingText = "hearing";
            return allocatedTrack.equals(FAST_CLAIM) ? labelText : hearingText;
        } else if (documentHearingType.equals(TRI) && context.equals(TITLE)) {
            labelText = documentHearingType.getLabelWelsh();
            hearingText = "wrandawiad";
            return allocatedTrack.equals(FAST_CLAIM) ? labelText : hearingText;
        } else if (documentHearingType.equals(TRI)) {
            labelText = "treial";
            hearingText = "gwrandawiad";
            return allocatedTrack.equals(FAST_CLAIM) ? labelText : hearingText;
        } else {
            labelText = isWelsh ? documentHearingType.getLabelWelsh() : documentHearingType.getLabel();
            hearingText = isWelsh ? "gwrandawiad" : "hearing";
            return context.equals(TITLE) ? labelText : hearingText;
        }
    }

    public static String getTitleText(DocumentHearingType hearingType, AllocatedTrack allocatedTrack, boolean isWelsh) {
        return getTypeText(hearingType, allocatedTrack, TITLE, isWelsh);
    }

    public static String getContentText(DocumentHearingType hearingType, AllocatedTrack allocatedTrack, boolean isWelsh) {
        return getTypeText(hearingType, allocatedTrack, CONTENT, isWelsh);
    }

    public static String getPluralTypeTextWelsh(DocumentHearingType documentHearingType, AllocatedTrack allocatedTrack) {
        return documentHearingType.equals(DocumentHearingType.TRI) && allocatedTrack.equals(FAST_CLAIM) ? "dreialon" : "wrandawiadau";
    }
}

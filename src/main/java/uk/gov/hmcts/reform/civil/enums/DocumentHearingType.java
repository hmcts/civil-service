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
        if (TRI.equals(documentHearingType)) {
            return getTrialText(allocatedTrack, context, isWelsh);
        }

        String labelText = isWelsh ? documentHearingType.getLabelWelsh() : documentHearingType.getLabel();
        String hearingText = isWelsh ? "gwrandawiad" : "hearing";
        return TITLE.equals(context) ? labelText : hearingText;
    }

    private static String getTrialText(AllocatedTrack allocatedTrack, DocumentContext context, boolean isWelsh) {
        if (!isWelsh) {
            return getTrackSpecificText(allocatedTrack, TRI.getLabel(), "hearing");
        }
        if (TITLE.equals(context)) {
            return getTrackSpecificText(allocatedTrack, TRI.getLabelWelsh(), "wrandawiad");
        }
        return getTrackSpecificText(allocatedTrack, "treial", "gwrandawiad");
    }

    private static String getTrackSpecificText(AllocatedTrack allocatedTrack, String labelText, String hearingText) {
        return FAST_CLAIM.equals(allocatedTrack) ? labelText : hearingText;
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

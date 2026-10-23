package uk.gov.hmcts.reform.civil.notification.handlers;

/**
 * Splits the summary a {@link Notifier} builds into the two halves the history event shows.
 *
 * <p>{@code BaseNotifier} returns one string of the form
 * {@code "Attempted: <recipients> || Errors: <errors>"}. The Record notification details event puts
 * the recipients in its Summary and any errors in its Description, which is what DTSCCI-4296 asks
 * for on General Applications and what the main claim already does.</p>
 *
 * <p>Shared by the Civil and General Application handlers deliberately. Both read the same string
 * because both notify through the same {@code BaseNotifier}, so parsing it in two places would be
 * two places to drift.</p>
 */
public final class NotificationSummaryParser {

    private static final String ATTEMPTED_PREFIX = "Attempted: ";
    private static final String DELIMITER = " || Errors: ";

    private NotificationSummaryParser() {
    }

    /**
     * The recipients half, without the {@code Attempted: } prefix.
     */
    public static String attempted(String combined) {
        if (combined == null) {
            return null;
        }

        int delimiterPos = combined.indexOf(DELIMITER);
        String left = (delimiterPos >= 0) ? combined.substring(0, delimiterPos) : combined;

        return left.startsWith(ATTEMPTED_PREFIX) ? left.substring(ATTEMPTED_PREFIX.length()) : left;
    }

    /**
     * The errors half, or null when there were none.
     */
    public static String errors(String combined) {
        if (combined == null) {
            return null;
        }

        int delimiterPos = combined.indexOf(DELIMITER);
        if (delimiterPos < 0) {
            return null;
        }

        String right = combined.substring(delimiterPos + DELIMITER.length());
        return !right.isBlank() ? right : null;
    }
}

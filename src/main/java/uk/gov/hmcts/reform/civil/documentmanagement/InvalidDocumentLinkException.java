package uk.gov.hmcts.reform.civil.documentmanagement;

/**
 * Thrown when a document self-href / path does not resolve to a usable document id. Two distinct
 * causes share this type, each with its own message so the log line is truthful:
 *
 * <ul>
 *     <li>the path is shorter than a full document UUID (e.g. a truncated stub such as
 *     {@code documents/null}), described by {@link #MESSAGE_TEMPLATE};</li>
 *     <li>the path is long enough but its trailing segment is not a valid UUID, described by
 *     {@link #MALFORMED_MESSAGE_TEMPLATE}.</li>
 * </ul>
 *
 * <p>Both are bad-input errors rather than transient download failures, so this type is excluded
 * from the download retry policy rather than consuming the retry budget.</p>
 */
public class InvalidDocumentLinkException extends DocumentDownloadException {

    public static final String MESSAGE_TEMPLATE =
        "Invalid document link '%s': expected a path of at least %d characters ending in a document UUID.";
    public static final String MALFORMED_MESSAGE_TEMPLATE =
        "Invalid document link '%s': the trailing document id is not a valid UUID.";

    public InvalidDocumentLinkException(String selfHref) {
        super(String.format(MESSAGE_TEMPLATE, selfHref, SecuredDocumentManagementService.DOC_UUID_LENGTH));
    }

    public InvalidDocumentLinkException(String documentPath, Throwable cause) {
        super(String.format(MALFORMED_MESSAGE_TEMPLATE, documentPath));
        initCause(cause);
    }
}

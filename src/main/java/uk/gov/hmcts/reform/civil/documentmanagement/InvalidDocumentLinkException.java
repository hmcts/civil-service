package uk.gov.hmcts.reform.civil.documentmanagement;

/**
 * Thrown when a document self-href / path does not contain a full trailing document UUID
 * (e.g. a truncated stub such as {@code documents/null}). Distinct from a transient download
 * failure: it is a bad-input error, so it is excluded from the download retry policy rather
 * than being retried five times.
 */
public class InvalidDocumentLinkException extends DocumentDownloadException {

    public static final String MESSAGE_TEMPLATE =
        "Invalid document link '%s': expected a path of at least %d characters ending in a document UUID.";

    public InvalidDocumentLinkException(String selfHref) {
        super(String.format(MESSAGE_TEMPLATE, selfHref, SecuredDocumentManagementService.DOC_UUID_LENGTH));
    }
}

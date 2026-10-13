package uk.gov.hmcts.reform.civil.service.docmosis;

public class DocumentGenerationFailedException extends RuntimeException {

    public DocumentGenerationFailedException(String message) {
        super(message);
    }
}

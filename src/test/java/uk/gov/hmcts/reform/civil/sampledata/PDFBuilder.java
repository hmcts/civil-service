package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CONSENT_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DIRECTION_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DISMISSAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_APPLICATION_DRAFT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.WRITTEN_REPRESENTATION_CONCURRENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL;

public final class PDFBuilder {

    private PDFBuilder() {
    }

    public static final CaseDocument GENERAL_ORDER_DOCUMENT = buildCaseDocument(GENERAL_ORDER);

    public static final CaseDocument DIRECTION_ORDER_DOCUMENT = buildCaseDocument(DIRECTION_ORDER);

    public static final CaseDocument DISMISSAL_ORDER_DOCUMENT = buildCaseDocument(DISMISSAL_ORDER);

    public static final CaseDocument HEARING_ORDER_DOCUMENT = buildCaseDocument(HEARING_ORDER);

    public static final CaseDocument WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT = buildCaseDocument(WRITTEN_REPRESENTATION_SEQUENTIAL);

    public static final CaseDocument WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT = buildCaseDocument(WRITTEN_REPRESENTATION_CONCURRENT);

    public static final CaseDocument REQUEST_FOR_INFORMATION_DOCUMENT = buildCaseDocument(REQUEST_FOR_INFORMATION);

    public static final CaseDocument CONSENT_ORDER_DOCUMENT = buildCaseDocument(CONSENT_ORDER);

    public static final CaseDocument APPLICATION_DRAFT_DOCUMENT = buildCaseDocument(GENERAL_APPLICATION_DRAFT);

    private static CaseDocument buildCaseDocument(uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType documentType) {
        Document documentLink = new Document()
            .setDocumentUrl("fake-url")
            .setDocumentFileName("file-name")
            .setDocumentBinaryUrl("binary-url");
        return new CaseDocument()
            .setCreatedBy("John")
            .setDocumentName("document name")
            .setDocumentSize(0L)
            .setDocumentType(documentType)
            .setCreatedDatetime(LocalDateTime.now())
            .setDocumentLink(documentLink);
    }
}

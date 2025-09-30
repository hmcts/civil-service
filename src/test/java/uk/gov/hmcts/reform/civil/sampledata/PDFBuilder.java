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

    public static final CaseDocument GENERAL_ORDER_DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(GENERAL_ORDER)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public static final CaseDocument DIRECTION_ORDER_DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(DIRECTION_ORDER)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public static final CaseDocument DISMISSAL_ORDER_DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(DISMISSAL_ORDER)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public static final CaseDocument HEARING_ORDER_DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(HEARING_ORDER)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public static final CaseDocument WRITTEN_REPRESENTATION_SEQUENTIAL_DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(WRITTEN_REPRESENTATION_SEQUENTIAL)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public static final CaseDocument WRITTEN_REPRESENTATION_CONCURRENT_DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(WRITTEN_REPRESENTATION_CONCURRENT)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public static final CaseDocument REQUEST_FOR_INFORMATION_DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(REQUEST_FOR_INFORMATION)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public static final CaseDocument CONSENT_ORDER_DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(CONSENT_ORDER)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public static final CaseDocument APPLICATION_DRAFT_DOCUMENT = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(GENERAL_APPLICATION_DRAFT)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();
}

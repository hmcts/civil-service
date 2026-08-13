package uk.gov.hmcts.reform.civil.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.scanneddocument.CCDScannedDocument;
import uk.gov.hmcts.reform.civil.model.scanneddocument.CCDScannedDocumentType;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocument;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocumentType;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ScannedDocumentMapperTest {

    private ScannedDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ScannedDocumentMapper();
    }

    @Test
    void shouldMapScannedDocumentToCCDCollectionElement() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ScannedDocument scannedDocument = ScannedDocument.builder()
            .id(id.toString())
            .fileName("test.pdf")
            .controlNumber("12345")
            .documentType(ScannedDocumentType.FORM)
            .subtype("N9a")
            .formSubtype("N9a")
            .submittedBy("BulkScan")
            .scannedDate(now)
            .deliveryDate(now)
            .exceptionRecordReference("EX123")
            .documentManagementUrl(URI.create("http://dm-store/documents/123"))
            .documentManagementBinaryUrl(URI.create("http://dm-store/documents/123/binary"))
            .build();

        Element<CCDScannedDocument> element = mapper.to(scannedDocument);

        assertThat(element).isNotNull();
        assertThat(element.getId()).isEqualTo(id);
        CCDScannedDocument ccdDoc = element.getValue();
        assertThat(ccdDoc.getFileName()).isEqualTo("test.pdf");
        assertThat(ccdDoc.getControlNumber()).isEqualTo("12345");
        assertThat(ccdDoc.getType()).isEqualTo(CCDScannedDocumentType.form);
        assertThat(ccdDoc.getSubtype()).isEqualTo("N9a");
        assertThat(ccdDoc.getUrl().getDocumentUrl()).isEqualTo("http://dm-store/documents/123");
        assertThat(ccdDoc.getUrl().getDocumentBinaryUrl()).isEqualTo("http://dm-store/documents/123/binary");
    }

    @Test
    void shouldMapCCDCollectionElementToScannedDocument() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Document document = new Document()
            .setDocumentUrl("http://dm-store/documents/123")
            .setDocumentBinaryUrl("http://dm-store/documents/123/binary")
            .setDocumentFileName("test.pdf");

        CCDScannedDocument ccdDoc = CCDScannedDocument.builder()
            .fileName("test.pdf")
            .controlNumber("12345")
            .type(CCDScannedDocumentType.form)
            .subtype("N9a")
            .formSubtype("N9a")
            .submittedBy("BulkScan")
            .scannedDate(now)
            .deliveryDate(now)
            .exceptionRecordReference("EX123")
            .url(document)
            .build();

        Element<CCDScannedDocument> element = new Element<>(id, ccdDoc);

        ScannedDocument domainDoc = mapper.from(element);

        assertThat(domainDoc).isNotNull();
        assertThat(domainDoc.getId()).isEqualTo(id.toString());
        assertThat(domainDoc.getFileName()).isEqualTo("test.pdf");
        assertThat(domainDoc.getControlNumber()).isEqualTo("12345");
        assertThat(domainDoc.getDocumentType()).isEqualTo(ScannedDocumentType.FORM);
        assertThat(domainDoc.getSubtype()).isEqualTo("N9a");
        assertThat(domainDoc.getDocumentManagementUrl()).isEqualTo(URI.create("http://dm-store/documents/123"));
    }

    @Test
    void shouldReturnNullWhenElementIsNull() {
        assertThat(mapper.from(null)).isNull();
        assertThat(mapper.to(null)).isNull();
    }
}

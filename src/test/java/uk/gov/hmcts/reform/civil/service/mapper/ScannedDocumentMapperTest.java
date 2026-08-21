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

    @Test
    void shouldMapScannedDocumentToCCDCollectionElementWithGeneratedIdAndDefaultType() {
        ScannedDocument scannedDocument = ScannedDocument.builder()
            .fileName("default-type.pdf")
            .controlNumber("CTRL-001")
            .documentType(null)
            .subtype("UNKNOWN")
            .build();

        Element<CCDScannedDocument> element = mapper.to(scannedDocument);

        assertThat(element).isNotNull();
        assertThat(element.getId()).isNotNull();
        assertThat(element.getValue()).isNotNull();
        assertThat(element.getValue().getType()).isEqualTo(CCDScannedDocumentType.other);
        assertThat(element.getValue().getFileName()).isEqualTo("default-type.pdf");
        assertThat(element.getValue().getControlNumber()).isEqualTo("CTRL-001");
        assertThat(element.getValue().getSubtype()).isEqualTo("UNKNOWN");
    }

    @Test
    void shouldPreserveExistingDocumentUrlWhenMappingToCCDDocument() {
        UUID id = UUID.randomUUID();
        Document existingDocument = new Document()
            .setDocumentUrl("http://dm-store/documents/existing")
            .setDocumentBinaryUrl("http://dm-store/documents/existing/binary")
            .setDocumentFileName("existing.pdf");

        ScannedDocument scannedDocument = ScannedDocument.builder()
            .id(id.toString())
            .fileName("ignored-file-name.pdf")
            .documentType(ScannedDocumentType.FORM)
            .url(existingDocument)
            .documentManagementUrl(URI.create("http://dm-store/documents/management-url"))
            .documentManagementBinaryUrl(URI.create("http://dm-store/documents/management-url/binary"))
            .build();

        Element<CCDScannedDocument> element = mapper.to(scannedDocument);

        assertThat(element).isNotNull();
        assertThat(element.getId()).isEqualTo(id);
        assertThat(element.getValue().getUrl()).isSameAs(existingDocument);
        assertThat(element.getValue().getUrl().getDocumentUrl()).isEqualTo("http://dm-store/documents/existing");
        assertThat(element.getValue().getUrl().getDocumentBinaryUrl()).isEqualTo("http://dm-store/documents/existing/binary");
        assertThat(element.getValue().getUrl().getDocumentFileName()).isEqualTo("existing.pdf");
    }

    @Test
    void shouldCreateDocumentWhenOnlyDocumentManagementBinaryUrlExists() {
        UUID id = UUID.randomUUID();

        ScannedDocument scannedDocument = ScannedDocument.builder()
            .id(id.toString())
            .fileName("binary-only.pdf")
            .documentType(ScannedDocumentType.OTHER)
            .documentManagementBinaryUrl(URI.create("http://dm-store/documents/123/binary"))
            .build();

        Element<CCDScannedDocument> element = mapper.to(scannedDocument);

        assertThat(element).isNotNull();
        assertThat(element.getId()).isEqualTo(id);
        assertThat(element.getValue().getType()).isEqualTo(CCDScannedDocumentType.other);
        assertThat(element.getValue().getUrl()).isNotNull();
        assertThat(element.getValue().getUrl().getDocumentUrl()).isNull();
        assertThat(element.getValue().getUrl().getDocumentBinaryUrl()).isEqualTo("http://dm-store/documents/123/binary");
        assertThat(element.getValue().getUrl().getDocumentFileName()).isEqualTo("binary-only.pdf");
    }

    @Test
    void shouldReturnNullWhenElementValueIsNull() {
        Element<CCDScannedDocument> element = new Element<>(UUID.randomUUID(), null);

        ScannedDocument result = mapper.from(element);

        assertThat(result).isNull();
    }

    @Test
    void shouldMapCCDDocumentWithDefaultsWhenOptionalFieldsAreMissing() {
        Document document = new Document()
            .setDocumentUrl("http://dm-store/documents/456")
            .setDocumentBinaryUrl("http://dm-store/documents/456/binary")
            .setDocumentFileName("fallback-name.pdf");

        CCDScannedDocument ccdDoc = CCDScannedDocument.builder()
            .fileName(null)
            .type(null)
            .subtype(null)
            .formSubtype("N180")
            .deliveryDate(null)
            .url(document)
            .build();

        Element<CCDScannedDocument> element = new Element<>(null, ccdDoc);

        LocalDateTime beforeMapping = LocalDateTime.now();
        ScannedDocument result = mapper.from(element);
        LocalDateTime afterMapping = LocalDateTime.now();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getFileName()).isEqualTo("fallback-name.pdf");
        assertThat(result.getDocumentType()).isEqualTo(ScannedDocumentType.OTHER);
        assertThat(result.getSubtype()).isEqualTo("N180");
        assertThat(result.getFormSubtype()).isEqualTo("N180");
        assertThat(result.getDeliveryDate()).isBetween(beforeMapping, afterMapping);
        assertThat(result.getDocumentManagementUrl()).isEqualTo(URI.create("http://dm-store/documents/456"));
        assertThat(result.getDocumentManagementBinaryUrl()).isEqualTo(URI.create("http://dm-store/documents/456/binary"));
        assertThat(result.getUrl()).isSameAs(document);
    }

    @Test
    void shouldMapCCDDocumentWithoutUrls() {
        UUID id = UUID.randomUUID();
        LocalDateTime deliveryDate = LocalDateTime.now();

        CCDScannedDocument ccdDoc = CCDScannedDocument.builder()
            .fileName("no-url.pdf")
            .type(CCDScannedDocumentType.other)
            .subtype("OTHER")
            .deliveryDate(deliveryDate)
            .url(null)
            .build();

        Element<CCDScannedDocument> element = new Element<>(id, ccdDoc);

        ScannedDocument result = mapper.from(element);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id.toString());
        assertThat(result.getFileName()).isEqualTo("no-url.pdf");
        assertThat(result.getDocumentType()).isEqualTo(ScannedDocumentType.OTHER);
        assertThat(result.getSubtype()).isEqualTo("OTHER");
        assertThat(result.getDeliveryDate()).isEqualTo(deliveryDate);
        assertThat(result.getDocumentManagementUrl()).isNull();
        assertThat(result.getDocumentManagementBinaryUrl()).isNull();
        assertThat(result.getUrl()).isNull();
    }
}

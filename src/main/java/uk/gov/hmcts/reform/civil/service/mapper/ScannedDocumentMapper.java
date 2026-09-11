package uk.gov.hmcts.reform.civil.service.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.scanneddocument.CCDScannedDocument;
import uk.gov.hmcts.reform.civil.model.scanneddocument.CCDScannedDocumentType;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocument;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocumentType;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ScannedDocumentMapper {

    public Element<CCDScannedDocument> to(ScannedDocument scannedDocument) {
        if (scannedDocument == null) {
            return null;
        }

        CCDScannedDocumentType type = scannedDocument.getDocumentType() != null
            ? CCDScannedDocumentType.valueOf(scannedDocument.getDocumentType().name().toLowerCase())
            : CCDScannedDocumentType.other;

        Document doc = scannedDocument.getUrl();
        if (doc == null && (scannedDocument.getDocumentManagementUrl() != null || scannedDocument.getDocumentManagementBinaryUrl() != null)) {
            doc = new Document()
                .setDocumentUrl(scannedDocument.getDocumentManagementUrl() != null ? scannedDocument.getDocumentManagementUrl().toString() : null)
                .setDocumentBinaryUrl(scannedDocument.getDocumentManagementBinaryUrl() != null ? scannedDocument.getDocumentManagementBinaryUrl().toString() : null)
                .setDocumentFileName(scannedDocument.getFileName());
        }

        CCDScannedDocument ccdScannedDocument = CCDScannedDocument.builder()
            .controlNumber(scannedDocument.getControlNumber())
            .deliveryDate(scannedDocument.getDeliveryDate())
            .type(type)
            .exceptionRecordReference(scannedDocument.getExceptionRecordReference())
            .fileName(scannedDocument.getFileName())
            .scannedDate(scannedDocument.getScannedDate())
            .subtype(scannedDocument.getSubtype())
            .formSubtype(scannedDocument.getFormSubtype())
            .submittedBy(scannedDocument.getSubmittedBy())
            .url(doc)
            .build();

        UUID id = scannedDocument.getId() != null ? UUID.fromString(scannedDocument.getId()) : UUID.randomUUID();
        return new Element<>(id, ccdScannedDocument);
    }

    public ScannedDocument from(Element<CCDScannedDocument> element) {
        if (element == null || element.getValue() == null) {
            return null;
        }

        CCDScannedDocument ccdScannedDocument = element.getValue();

        LocalDateTime deliveryDate = ccdScannedDocument.getDeliveryDate();
        deliveryDate = deliveryDate == null ? LocalDateTime.now() : deliveryDate;

        String fileName = ccdScannedDocument.getFileName();
        if (fileName == null && ccdScannedDocument.getUrl() != null) {
            fileName = ccdScannedDocument.getUrl().getDocumentFileName();
        }

        String subType = ccdScannedDocument.getSubtype();
        subType = subType == null ? ccdScannedDocument.getFormSubtype() : subType;

        URI docUrl = ccdScannedDocument.getUrl() != null && ccdScannedDocument.getUrl().getDocumentUrl() != null
            ? URI.create(ccdScannedDocument.getUrl().getDocumentUrl()) : null;
        URI docBinaryUrl = ccdScannedDocument.getUrl() != null && ccdScannedDocument.getUrl().getDocumentBinaryUrl() != null
            ? URI.create(ccdScannedDocument.getUrl().getDocumentBinaryUrl()) : null;

        ScannedDocumentType docType = ccdScannedDocument.getType() != null
            ? ScannedDocumentType.valueOf(ccdScannedDocument.getType().name().toUpperCase())
            : ScannedDocumentType.OTHER;

        return ScannedDocument.builder()
            .id(element.getId() != null ? element.getId().toString() : null)
            .fileName(fileName)
            .documentManagementUrl(docUrl)
            .documentManagementBinaryUrl(docBinaryUrl)
            .documentType(docType)
            .scannedDate(ccdScannedDocument.getScannedDate())
            .deliveryDate(deliveryDate)
            .subtype(subType)
            .formSubtype(ccdScannedDocument.getFormSubtype())
            .submittedBy(ccdScannedDocument.getSubmittedBy())
            .exceptionRecordReference(ccdScannedDocument.getExceptionRecordReference())
            .controlNumber(ccdScannedDocument.getControlNumber())
            .url(ccdScannedDocument.getUrl())
            .build();
    }
}

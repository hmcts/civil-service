package uk.gov.hmcts.reform.civil.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocument;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocumentSubtype;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocumentType;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "Scanned Documents Controller")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/scanned-documents")
public class ScannedDocumentsController {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DocumentManagementService documentManagementService;

    @Operation(summary = "Returns a scanned pdf for a given claim external id or case reference")
    @GetMapping(
        value = "/{externalId}/{documentType}/{documentSubtype}",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> scannedDocument(
        @Parameter(name = "Claim external id or case reference")
        @PathVariable("externalId") @NotBlank String externalId,
        @Parameter(name = "Claim document type")
        @PathVariable("documentType") @NotBlank String documentType,
        @Parameter(name = "Claim document subtype")
        @PathVariable("documentSubtype") @NotBlank String documentSubtype,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    ) {
        log.info("Fetching scanned document for externalId/caseId: {}, type: {}, subtype: {}",
                 externalId, documentType, documentSubtype);

        String subTypeValue;
        try {
            subTypeValue = ScannedDocumentSubtype.valueOf(documentSubtype.toUpperCase()).value;
        } catch (IllegalArgumentException e) {
            subTypeValue = documentSubtype;
        }

        Long caseId;
        try {
            caseId = Long.parseLong(externalId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid case reference: " + externalId);
        }

        CaseDetails caseDetails = coreCaseDataService.getCase(caseId, authorisation);
        if (caseDetails == null) {
            throw new IllegalArgumentException("Case not found for id: " + externalId);
        }

        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        if (caseData.getScannedDocuments() == null || caseData.getScannedDocuments().isEmpty()) {
            throw new IllegalArgumentException("Document is not available for download");
        }

        final ScannedDocumentType scannedDocumentType = ScannedDocumentType.fromValue(documentType);
        final String targetSubtype = subTypeValue;
        Optional<ScannedDocument> scannedDocument = caseData.getScannedDocuments().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .filter(doc -> doc.getDocumentType() == scannedDocumentType)
            .filter(doc -> (doc.getSubtype() != null && doc.getSubtype().equalsIgnoreCase(targetSubtype))
                || (doc.getFormSubtype() != null && doc.getFormSubtype().equalsIgnoreCase(targetSubtype)))
            .findFirst();

        ScannedDocument doc = scannedDocument.orElseThrow(
            () -> new IllegalArgumentException("Document is not available for download"));

        String docUrl = doc.getUrl() != null ? doc.getUrl().getDocumentUrl()
            : (doc.getDocumentManagementUrl() != null ? doc.getDocumentManagementUrl().toString() : null);

        if (docUrl == null) {
            throw new IllegalArgumentException("Document URL is missing");
        }

        byte[] pdfDocument = documentManagementService.downloadDocument(authorisation, docUrl);

        return ResponseEntity
            .ok()
            .contentLength(pdfDocument.length)
            .body(new ByteArrayResource(pdfDocument));
    }
}

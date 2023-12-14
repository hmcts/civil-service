package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.UploadedDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.documentmanagement.ClaimFormService;

import javax.validation.constraints.NotNull;

@Tag(name = "Document Controller")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(
    path = "/case/document"
)
public class DocumentController {

    private final ClaimFormService claimFormService;
    private final DocumentManagementService documentManagementService;

    @PostMapping("/generateSealedDoc")
    public ResponseEntity<CaseDocument> uploadSealedDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation, @NotNull @RequestBody CaseData caseData) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(claimFormService.uploadSealedDocument(authorisation, caseData));
    }

    @PostMapping(value = "/generateAnyDoc")
    @Operation(summary = "Upload document")
    public ResponseEntity<CaseDocument> uploadAnyDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam("file")MultipartFile file) {

        UploadedDocument uploadedDocument = new UploadedDocument(file.getOriginalFilename(), file);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(documentManagementService.uploadDocument(authorisation, uploadedDocument));
    }

    @GetMapping(value = "/downloadDocument/{documentId}")
    public @ResponseBody
    ResponseEntity<Resource> downloadDocumentById(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @NotNull @PathVariable String documentId) {
        DownloadedDocumentResponse documentResponse = claimFormService.downloadDocumentById(authorisation, documentId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(documentResponse.mimeType()));
        headers.set("original-file-name", documentResponse.fileName());

        return  ResponseEntity.ok()
            .headers(headers)
            .body(documentResponse.file());
    }

}

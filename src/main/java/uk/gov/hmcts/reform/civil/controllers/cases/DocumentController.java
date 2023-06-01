package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentUploadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
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
    public CaseDocument uploadSealedDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation, @NotNull @RequestBody CaseData caseData) {
        return claimFormService.uploadSealedDocument(authorisation, caseData);
    }

    @PostMapping(value = "/generateAnyDoc")
    @Operation(summary = "Upload document")
    public CaseDocument uploadAnyDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam("file")MultipartFile file) {
        try {
            UploadedDocument uploadedDocument = new UploadedDocument(file.getOriginalFilename(), file.getBytes());
            return documentManagementService.uploadDocument(authorisation, uploadedDocument);
        } catch (Exception e) {
            throw new DocumentUploadException(file.getOriginalFilename());
        }
    }

    @PostMapping(value = "/downloadSealedDoc",
        produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    byte[] downloadSealedDocument(
        @NotNull @RequestBody CaseDocument caseDocument) {
        return claimFormService.downloadSealedDocument(caseDocument);
    }

}

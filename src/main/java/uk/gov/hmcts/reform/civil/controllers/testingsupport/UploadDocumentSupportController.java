package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.helpers.ResourceReader;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;

@Tag(name = "Upload Document Support Controller")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/testing-support",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
)
@ConditionalOnExpression("${testing.support.enabled:false}")
public class UploadDocumentSupportController {

    private final DocumentManagementService documentManagementService;

    @PostMapping(value = {"/upload/test-document"})
    @Operation(summary = "Upload document")
    public Document uploadTestDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {

        PDF pdf = new PDF("TestFile.pdf", ResourceReader.readBytes("/test_support/TEST_DOCUMENT_1.pdf"), SEALED_CLAIM);
        return documentManagementService.uploadDocument(authorisation, pdf).getDocumentLink();
    }
}

package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.helpers.ResourceReader;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentService;

import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;

@Api
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

    private final DocumentService documentService;

    @PostMapping(value = {"/upload/test-document"})
    @ApiOperation("Upload document")
    public Document uploadTestDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {

        PDF pdf = new PDF("mockFile.pdf", ResourceReader.readBytes("test_support/000LR003.pdf"), SEALED_CLAIM);
        return documentService.uploadDocument(authorisation, pdf).getDocumentLink();
    }
}

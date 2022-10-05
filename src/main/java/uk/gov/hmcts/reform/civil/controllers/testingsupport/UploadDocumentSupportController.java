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
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

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

    private final DocumentManagementService documentManagementService;

    @PostMapping(value = {"/upload/test-document"})
    @ApiOperation("Upload document")
    public Document uploadTestDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {

        PDF pdf = new PDF("TestFile.pdf", ResourceReader.readBytes("/test_support/TEST_DOCUMENT_1.pdf"), SEALED_CLAIM);
        return documentManagementService.uploadDocument(authorisation, pdf).getDocumentLink();
    }
}

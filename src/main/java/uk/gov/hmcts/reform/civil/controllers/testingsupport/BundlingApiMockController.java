package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.helpers.ResourceReader;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.IdValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;

@Tag(name = "Bundling / Stitching API Mock Controller")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/testing-support",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
)
@ConditionalOnExpression("${testing.support.enabled:false}")
public class BundlingApiMockController {

    private final DocumentManagementService documentManagementService;

    @PostMapping(path = "/stitch-me")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> mockStitchingRequest(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest bundleRequest) {

        Map<String, Object> data = bundleRequest.getCaseDetails().getData();

        PDF pdf = new PDF("TestFile.pdf",
                          ResourceReader.readBytes("/test_support/TEST_DOCUMENT_1.pdf"),
                          SEALED_CLAIM);
        var document = documentManagementService.uploadDocument(authorisation, pdf).getDocumentLink();

        var bundle = new IdValue<>("", Bundle.builder().stitchedDocument(Optional.of(document)).build());
        List<IdValue<Bundle>> caseBundles = List.of(bundle);
        data.put("caseBundles", caseBundles);

        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

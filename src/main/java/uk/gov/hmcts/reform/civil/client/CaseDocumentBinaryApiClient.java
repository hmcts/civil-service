package uk.gov.hmcts.reform.civil.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

/**
 * Streams a document binary from CDAM. The library's {@code CaseDocumentClientApi#getDocumentBinary}
 * decodes the body into a {@code Resource} via {@code SpringDecoder}, which reads the whole document
 * into a byte array. Returning {@link Response} skips decoding so the body can be copied straight to
 * the caller. The caller must check the status and close the body.
 */
@FeignClient(name = "case-document-am-binary-api", url = "${case_document_am.url}/cases/documents",
    configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface CaseDocumentBinaryApiClient {

    @GetMapping("/{documentId}/binary")
    Response getDocumentBinary(
        @RequestHeader("Authorization") String authorisation,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable("documentId") UUID documentId
    );
}

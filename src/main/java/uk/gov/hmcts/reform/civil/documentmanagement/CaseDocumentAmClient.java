package uk.gov.hmcts.reform.civil.documentmanagement;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.document.am.feign.FeignSupportConfig;
import uk.gov.hmcts.reform.ccd.document.am.model.CaseDocumentsMetadata;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.PatchDocumentMetaDataResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PatchDocumentResponse;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "case-document-am-client-api", url = "${case_document_am.url}/cases/documents",
        configuration = FeignSupportConfig.class)
public interface CaseDocumentAmClient {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String DOCUMENT_ID = "documentId";

    @PatchMapping(value = "/{documentId}")
    PatchDocumentResponse patchDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                        @PathVariable(DOCUMENT_ID) UUID documentId,
                                        @RequestBody DocumentTTLRequest ttl);
}

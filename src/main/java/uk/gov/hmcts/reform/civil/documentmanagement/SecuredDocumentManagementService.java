package uk.gov.hmcts.reform.civil.documentmanagement;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.documentmanagement.model.UploadedDocument;
import uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.net.URI;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.civil.constants.DocumentManagementConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.civil.constants.DocumentManagementConstants.CREATED_BY;
import static uk.gov.hmcts.reform.civil.constants.DocumentManagementConstants.JURISDICTION_ID;

@Slf4j
@Service("documentManagementService")
@RequiredArgsConstructor
public class SecuredDocumentManagementService implements DocumentManagementService {

    protected static final int DOC_UUID_LENGTH = 36;
    protected static final String FILES_NAME = "files";
    private static final String CDAM_FORBIDDEN_EXCEPTION = "ForbiddenException";
    private static final String TTL_EXPIRED_MESSAGE = "TTL has expired";

    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final DocumentManagementConfiguration documentManagementConfiguration;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final Tika tika;

    @Retryable(retryFor = {DocumentUploadException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 3))
    @Override
    public CaseDocument uploadDocument(String authorisation, PDF pdf) {
        String originalFileName = pdf.getFileBaseName();
        log.info("Uploading file {}", originalFileName);
        try {
            MultipartFile file
                = new InMemoryMultipartFile(FILES_NAME, originalFileName, APPLICATION_PDF_VALUE, pdf.getBytes()
            );

            DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(
                Classification.RESTRICTED.toString(),
                CASE_TYPE_ID,
                JURISDICTION_ID,
                Collections.singletonList(file)
            );

            UploadResponse response = caseDocumentClientApi.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                documentUploadRequest
            );

            Document document = response.getDocuments().stream()
                .findFirst()
                .orElseThrow(() -> new DocumentUploadException(originalFileName));

            uk.gov.hmcts.reform.civil.documentmanagement.model.Document documentLink =
                new uk.gov.hmcts.reform.civil.documentmanagement.model.Document()
                    .setDocumentUrl(document.links.self.href)
                    .setDocumentBinaryUrl(document.links.binary.href)
                    .setDocumentFileName(originalFileName)
                    .setDocumentHash(document.hashToken);

            return new CaseDocument()
                .setDocumentLink(documentLink)
                .setDocumentName(originalFileName)
                .setDocumentType(pdf.getDocumentType())
                .setCreatedDatetime(LocalDateTimeHelper.fromUTC(document.createdOn
                                                                   .toInstant()
                                                                   .atZone(ZoneId.systemDefault())
                                                                   .toLocalDateTime()))
                .setDocumentSize(document.size)
                .setCreatedBy(CREATED_BY);
        } catch (Exception ex) {
            log.error("Failed uploading file {}", originalFileName, ex);
            throw new DocumentUploadException(originalFileName, ex);
        }
    }

    @Retryable(retryFor = {DocumentUploadException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 3))
    @Override
    public CaseDocument uploadDocument(String authorisation, UploadedDocument uploadedDocument) {

        String originalFileName = uploadedDocument.getFileBaseName();
        log.info("Uploading file {}", originalFileName);

        try {
            MultipartFile file
                = new InMemoryMultipartFile(FILES_NAME, originalFileName, tika.detect(originalFileName), uploadedDocument.getFile().getBytes());

            DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(
                Classification.RESTRICTED.toString(),
                "CIVIL",
                "CIVIL",
                Collections.singletonList(file)
            );

            UploadResponse response = caseDocumentClientApi.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                documentUploadRequest
            );

            Document document = response.getDocuments().stream()
                .findFirst()
                .orElseThrow(() -> new DocumentUploadException(originalFileName));

            uk.gov.hmcts.reform.civil.documentmanagement.model.Document documentLink =
                new uk.gov.hmcts.reform.civil.documentmanagement.model.Document()
                    .setDocumentUrl(document.links.self.href)
                    .setDocumentBinaryUrl(document.links.binary.href)
                    .setDocumentFileName(originalFileName)
                    .setDocumentHash(document.hashToken);

            return new CaseDocument()
                .setDocumentLink(documentLink)
                .setDocumentName(originalFileName)
                .setCreatedDatetime(LocalDateTimeHelper.fromUTC(document.createdOn
                                                                   .toInstant()
                                                                   .atZone(ZoneId.systemDefault())
                                                                   .toLocalDateTime()))
                .setDocumentSize(document.size)
                .setCreatedBy(CREATED_BY);

        } catch (Exception ex) {
            throw new DocumentUploadException(originalFileName, ex);
        }

    }

    @Retryable(retryFor = {DocumentDownloadException.class},
        noRetryFor = {DocumentNotFoundException.class, DocumentAccessException.class,
            InvalidDocumentReferenceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 3))
    @Override
    public byte[] downloadDocument(String authorisation, String documentPath) {
        log.info("Downloading document {}", documentPath);
        try {
            UserInfo userInfo = userService.getUserInfo(authorisation);
            String userRoles = String.join(",", this.documentManagementConfiguration.getUserRoles());

            ResponseEntity<Resource> responseEntity = caseDocumentClientApi.getDocumentBinary(
                authorisation,
                authTokenGenerator.generate(),
                UUID.fromString(documentPath.substring(documentPath.lastIndexOf("/") + 1))
            );

            if (responseEntity == null) {
                Document documentMetadata = getDocumentMetaData(authorisation, documentPath);
                responseEntity = documentDownloadClientApi.downloadBinary(
                    authorisation,
                    authTokenGenerator.generate(),
                    userRoles,
                    userInfo.getUid(),
                    URI.create(documentMetadata.links.binary.href).getPath().replaceFirst("/", "")
                );
            }

            return Optional.ofNullable(responseEntity.getBody())
                .map(ByteArrayResource.class::cast)
                .map(ByteArrayResource::getByteArray)
                .orElseThrow(RuntimeException::new);
        } catch (Exception ex) {
            throw classifyDownloadFailure(documentPath, ex);
        }
    }

    @Retryable(retryFor = {DocumentDownloadException.class},
        noRetryFor = {DocumentNotFoundException.class, DocumentAccessException.class,
            InvalidDocumentReferenceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 3))
    @Override
    public DownloadedDocumentResponse downloadDocumentWithMetaData(String authorisation, String documentPath) {
        log.info("Downloading document {}", documentPath);
        try {
            UserInfo userInfo = userService.getUserInfo(authorisation);
            String userRoles = String.join(",", this.documentManagementConfiguration.getUserRoles());
            Document documentMetadata = getDocumentMetaData(authorisation, documentPath);

            ResponseEntity<Resource> responseEntity = caseDocumentClientApi.getDocumentBinary(
                authorisation,
                authTokenGenerator.generate(),
                UUID.fromString(documentPath.substring(documentPath.lastIndexOf("/") + 1))
            );

            if (responseEntity == null) {
                responseEntity = documentDownloadClientApi.downloadBinary(
                    authorisation,
                    authTokenGenerator.generate(),
                    userRoles,
                    userInfo.getUid(),
                    URI.create(documentMetadata.links.binary.href).getPath().replaceFirst("/", "")
                );
            }

            return new DownloadedDocumentResponse(responseEntity.getBody(), documentMetadata.originalDocumentName,
                                                  tika.detect(documentMetadata.originalDocumentName));
        } catch (Exception ex) {
            throw classifyDownloadFailure(documentPath, ex);
        }
    }

    @Override
    public void deleteDocument(String authorisation, String documentPath) {
        log.info("Deleting document {}", documentPath);
        try {
            caseDocumentClientApi.deleteDocument(authorisation, authTokenGenerator.generate(), getDocumentIdFromSelfHref(documentPath), true);
        } catch (Exception ex) {
            throw classifyDownloadFailure(documentPath, ex);
        }
    }

    public Document getDocumentMetaData(String authorisation, String documentPath) {
        log.info("Getting metadata for file {}", documentPath);

        try {
            return caseDocumentClientApi.getMetadataForDocument(
                authorisation,
                authTokenGenerator.generate(),
                getDocumentIdFromSelfHref(documentPath)
            );

        } catch (Exception ex) {
            throw classifyDownloadFailure(documentPath, ex);
        }
    }

    /**
     * Maps a download/metadata failure onto a specific exception so callers and the
     * controller advice can return a meaningful status: CDAM 404 -> not found,
     * CDAM 403 -> access refused, a malformed reference -> bad request, and any other
     * (transient) failure -> the retryable {@link DocumentDownloadException}. An
     * already-classified exception is returned as-is so it is not re-wrapped or retried.
     */
    private RuntimeException classifyDownloadFailure(String documentPath, Exception ex) {
        if (ex instanceof DocumentNotFoundException
            || ex instanceof DocumentAccessException
            || ex instanceof InvalidDocumentReferenceException) {
            return (RuntimeException) ex;
        }
        if (ex instanceof FeignException.NotFound) {
            log.error("Document {} not found in document management", documentPath, ex);
            return new DocumentNotFoundException(documentPath, ex);
        }
        if (ex instanceof FeignException.Forbidden) {
            log.error("Access to document {} refused by document management", documentPath, ex);
            return new DocumentAccessException(documentPath, ex);
        }
        if (ex instanceof IllegalArgumentException) {
            log.error("Invalid document reference {}", documentPath, ex);
            return new InvalidDocumentReferenceException(documentPath, ex);
        }
        log.error("Failed downloading document {}", documentPath, ex);
        return new DocumentDownloadException(documentPath, ex);
    }

    private UUID getDocumentIdFromSelfHref(String selfHref) {
        if (selfHref == null || selfHref.length() < DOC_UUID_LENGTH) {
            log.error("Invalid document reference, cannot extract document id: {}", selfHref);
            throw new InvalidDocumentReferenceException(selfHref);
        }
        return UUID.fromString(selfHref.substring(selfHref.length() - DOC_UUID_LENGTH));
    }

    private boolean isDocumentTtlExpired(Exception ex) {
        return ex instanceof FeignException.Forbidden feignException
            && feignException.contentUTF8().contains(CDAM_FORBIDDEN_EXCEPTION)
            && feignException.contentUTF8().contains(TTL_EXPIRED_MESSAGE);
    }
}

package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import uk.gov.hmcts.reform.civil.config.DocumentManagementConfiguration;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Slf4j
@Service("documentManagementService")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "document_management", name = "secured", havingValue = "true")
public class SecuredDocumentManagementService implements DocumentManagementService {

    protected static final int DOC_UUID_LENGTH = 36;
    public static final String CREATED_BY = "Civil";
    protected static final String FILES_NAME = "files";

    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final DocumentManagementConfiguration documentManagementConfiguration;
    private final CaseDocumentClientApi caseDocumentClientApi;

    @Retryable(value = {DocumentUploadException.class}, backoff = @Backoff(delay = 200))
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

            return CaseDocument.builder()
                .documentLink(uk.gov.hmcts.reform.civil.model.documents.Document.builder()
                                  .documentUrl(document.links.self.href)
                                  .documentBinaryUrl(document.links.binary.href)
                                  .documentFileName(originalFileName)
                                  .build())
                .documentName(originalFileName)
                .documentType(pdf.getDocumentType())
                .createdDatetime(LocalDateTime.now())
                .documentSize(document.size)
                .createdBy(CREATED_BY)
                .build();
        } catch (Exception ex) {
            log.error("Failed uploading file {}", originalFileName, ex);
            throw new DocumentUploadException(originalFileName, ex);
        }
    }

    @Retryable(value = DocumentDownloadException.class, backoff = @Backoff(delay = 200))
    @Override
    public byte[] downloadDocument(String authorisation, String documentPath) {
        log.info("Downloading document {}", documentPath);
        try {
            UserInfo userInfo = userService.getUserInfo(authorisation);
            String userRoles = String.join(",", this.documentManagementConfiguration.getUserRoles());
            Document documentMetadata = getDocumentMetaData(authorisation, documentPath);

            ResponseEntity<Resource> responseEntity = documentDownloadClientApi.downloadBinary(
                authorisation,
                authTokenGenerator.generate(),
                userRoles,
                userInfo.getUid(),
                URI.create(documentMetadata.links.binary.href).getPath().replaceFirst("/", "")
            );

            return Optional.ofNullable(responseEntity.getBody())
                .map(ByteArrayResource.class::cast)
                .map(ByteArrayResource::getByteArray)
                .orElseThrow(RuntimeException::new);
        } catch (Exception ex) {
            log.error("Failed downloading document {}", documentPath, ex);
            throw new DocumentDownloadException(documentPath, ex);
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
            log.error("Failed getting metadata for {}", documentPath, ex);
            throw new DocumentDownloadException(documentPath, ex);
        }
    }

    private UUID getDocumentIdFromSelfHref(String selfHref) {
        return UUID.fromString(selfHref.substring(selfHref.length() - DOC_UUID_LENGTH));
    }

    private void logFiles(List<MultipartFile> files) {
        files.forEach(file -> {
            log.info("Name: {}", file.getName());
            log.info("OriginalName {}", file.getOriginalFilename());
        });
    }

}

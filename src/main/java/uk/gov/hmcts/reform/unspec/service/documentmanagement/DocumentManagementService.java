package uk.gov.hmcts.reform.unspec.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.unspec.config.DocumentManagementConfiguration;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.service.UserService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentManagementService {

    public static final String UNSPEC = "Unspec";
    protected static final String FILES_NAME = "files";

    private final DocumentUploadClientApi documentUploadClientApi;
    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final DocumentManagementConfiguration documentManagementConfiguration;

    @Retryable(value = {DocumentUploadException.class}, backoff = @Backoff(delay = 200))
    public CaseDocument uploadDocument(String authorisation, PDF pdf) {
        String originalFileName = pdf.getFileBaseName();
        log.info("Uploading file {}", originalFileName);
        try {
            MultipartFile file
                = new InMemoryMultipartFile(FILES_NAME, originalFileName, APPLICATION_PDF_VALUE, pdf.getBytes());

            UserInfo userInfo = userService.getUserInfo(authorisation);
            UploadResponse response = documentUploadClientApi.upload(
                authorisation,
                authTokenGenerator.generate(),
                userInfo.getUid(),
                documentManagementConfiguration.getUserRoles(),
                Classification.RESTRICTED,
                singletonList(file)
            );

            Document document = response.getEmbedded().getDocuments().stream()
                .findFirst()
                .orElseThrow(() -> new DocumentUploadException(originalFileName));

            return CaseDocument.builder()
                .documentLink(uk.gov.hmcts.reform.unspec.model.documents.Document.builder()
                                  .documentUrl(document.links.self.href)
                                  .documentBinaryUrl(document.links.binary.href)
                                  .documentFileName(originalFileName)
                                  .build())
                .documentName(originalFileName)
                .documentType(pdf.getDocumentType())
                .createdDatetime(LocalDateTime.now())
                .documentSize(document.size)
                .createdBy(UNSPEC)
                .build();
        } catch (Exception ex) {
            log.error("Failed uploading file {}", originalFileName, ex);
            throw new DocumentUploadException(originalFileName, ex);
        }
    }

    @Retryable(value = DocumentDownloadException.class, backoff = @Backoff(delay = 200))
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
                URI.create(documentMetadata.links.binary.href).getPath()
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
            UserInfo userInfo = userService.getUserInfo(authorisation);
            String userRoles = String.join(",", this.documentManagementConfiguration.getUserRoles());
            return documentMetadataDownloadClient.getDocumentMetadata(
                authorisation,
                authTokenGenerator.generate(),
                userRoles,
                userInfo.getUid(),
                documentPath
            );
        } catch (Exception ex) {
            log.error("Failed getting metadata for {}", documentPath, ex);
            throw new DocumentDownloadException(documentPath, ex);
        }
    }
}

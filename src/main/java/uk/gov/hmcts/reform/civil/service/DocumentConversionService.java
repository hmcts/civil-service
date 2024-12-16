package uk.gov.hmcts.reform.civil.service;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.exceptions.DocumentConversionException;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentConversionService {

    private static final String PDF_MIME_TYPE = "application/pdf";

    @Value("${docmosis.tornado.url}")
    private String documentConversionUrl;

    @Value("${docmosis.tornado.key}")
    private String docmosisAccessKey;

    private final Tika tika;

    private final RestTemplate restTemplate;

    private final DocumentManagementService documentManagementService;

    public byte[] convertDocumentToPdf(Document sourceDocument, Long caseId, String auth) {
        if (PDF_MIME_TYPE.equalsIgnoreCase(tika.detect(sourceDocument.getDocumentFileName()))) {
            return documentManagementService.downloadDocument(auth, sourceDocument.getDocumentUrl());
        }
        log.info("Converting document to pdf for caseId {}", caseId);
        return convert(sourceDocument, caseId, auth);
    }

    public String getConvertedFilename(String filename) {
        return FilenameUtils.getBaseName(filename) + ".pdf";
    }

    public byte[] convert(Document sourceDocument, Long caseId, String auth) {
        try {
            String filename = getConvertedFilename(sourceDocument.getDocumentFileName());
            byte[] docInBytes = documentManagementService.downloadDocument(auth, sourceDocument.getDocumentUrl());
            File file = new File(filename);
            Files.write(docInBytes, file);

            return restTemplate
                .postForObject(
                    documentConversionUrl + "/rs/convert",
                    createRequest(file, filename),
                    byte[].class
                );

        } catch (HttpClientErrorException clientEx) {
            log.error("failed to convert document to pdf for caseId {}", caseId, clientEx);
            throw new DocumentConversionException(
                "Error converting document to pdf for caseId " + caseId,
                clientEx
            );
        } catch (IOException ex) {
            log.error("Error creating temp file while converting for caseId {}", caseId, ex);
            throw new DocumentConversionException(
                "Error creating temp file for caseId " + caseId,
                ex
            );
        }
    }

    private HttpEntity<MultiValueMap<String, Object>> createRequest(
        File file,
        String outputFilename
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("accessKey", docmosisAccessKey);
        body.add("outputName", outputFilename);
        body.add("file", new FileSystemResource(file));

        return new HttpEntity<>(body, headers);
    }
}

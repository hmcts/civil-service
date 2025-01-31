package uk.gov.hmcts.reform.civil.service.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
//import uk.gov.hmcts.reform.civil.client.DocmosisApiClient;
import uk.gov.hmcts.reform.civil.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentGeneratorService {

    // private final DocmosisApiClient docmosisApiClient;
    private final DocmosisConfiguration configuration;
    private final ObjectMapper mapper;

    public DocmosisDocument generateDocmosisDocument(MappableObject templateData, DocmosisTemplates template) {
        return generateDocmosisDocument(templateData.toMap(mapper), template, "pdf");
    }

    public DocmosisDocument generateDocmosisDocument(MappableObject templateData, DocmosisTemplates template, String generateDocx) {
        return generateDocmosisDocument(templateData.toMap(mapper), template, generateDocx);
    }

    public DocmosisDocument generateDocmosisDocument(Map<String, Object> templateData, DocmosisTemplates template, String outputFormat) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        DocmosisRequest requestBody = DocmosisRequest.builder()
            .templateName(template.getTemplate())
            .data(templateData)
            .outputFormat(outputFormat)
            .outputName("IGNORED")
            .accessKey(configuration.getAccessKey())
            .build();

        byte[] response = new byte[]{};

        try {
            // response = docmosisApiClient.createDocument(requestBody);
            response = Files.readAllBytes(Paths.get("dummy.pdf"));
        } catch (IOException e) {
            System.out.println("Error reading the PDF file: " + e.getMessage());
        } catch (HttpClientErrorException ex) {
            log.error("Docmosis document generation failed for " + ex.getMessage());
            throw ex;
        }

        return new DocmosisDocument(template.getDocumentTitle(), response);
    }
}

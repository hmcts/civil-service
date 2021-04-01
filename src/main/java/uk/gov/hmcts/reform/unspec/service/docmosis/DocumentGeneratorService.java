package uk.gov.hmcts.reform.unspec.service.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.unspec.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.unspec.model.common.MappableObject;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisRequest;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentGeneratorService {

    public static final String API_RENDER = "/rs/render";
    private final RestTemplate restTemplate;
    private final DocmosisConfiguration configuration;
    private final ObjectMapper mapper;

    public DocmosisDocument generateDocmosisDocument(MappableObject templateData, DocmosisTemplates template) {
        return generateDocmosisDocument(templateData.toMap(mapper), template);
    }

    public DocmosisDocument generateDocmosisDocument(Map<String, Object> templateData, DocmosisTemplates template) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        DocmosisRequest requestBody = DocmosisRequest.builder()
            .templateName(template.getTemplate())
            .data(templateData)
            .outputFormat("pdf")
            .outputName("IGNORED")
            .accessKey(configuration.getAccessKey())
            .build();

        HttpEntity<DocmosisRequest> request = new HttpEntity<>(requestBody, headers);

        byte[] response;

        try {
            response = restTemplate.exchange(configuration.getUrl() + API_RENDER,
                                             HttpMethod.POST, request, byte[].class
            ).getBody();
        } catch (HttpClientErrorException ex) {
            log.error("Docmosis document generation failed for " + ex.getMessage());
            throw ex;
        }

        return new DocmosisDocument(template.getDocumentTitle(), response);
    }
}

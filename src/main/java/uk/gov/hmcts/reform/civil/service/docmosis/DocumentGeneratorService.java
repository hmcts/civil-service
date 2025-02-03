package uk.gov.hmcts.reform.civil.service.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentGeneratorService {

    private final ObjectMapper mapper;

    @SuppressWarnings("unused")
    public DocmosisDocument generateDocmosisDocument(MappableObject templateData, DocmosisTemplates template) {
        return generateDocmosisDocument(templateData.toMap(mapper), template, "pdf");
    }

    @SuppressWarnings("unused")
    public DocmosisDocument generateDocmosisDocument(MappableObject templateData, DocmosisTemplates template, String generateDocx) {
        return generateDocmosisDocument(templateData.toMap(mapper), template, generateDocx);
    }

    @SuppressWarnings("unused")
    public DocmosisDocument generateDocmosisDocument(Map<String, Object> templateData, DocmosisTemplates template, String outputFormat) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        byte[] response = new byte[]{};

        try {
            response = DocumentGeneratorService.class.getClassLoader().getResourceAsStream("dummy.pdf").readAllBytes();
        } catch (IOException e) {
            System.out.println("Error reading the PDF file: " + e.getMessage());
        }

        return new DocmosisDocument(template.getDocumentTitle(), response);
    }
}

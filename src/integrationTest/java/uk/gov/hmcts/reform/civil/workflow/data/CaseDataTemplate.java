package uk.gov.hmcts.reform.civil.workflow.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class CaseDataTemplate {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private CaseDataTemplate() {
    }

    public static CaseData load(String fileName) {
        // Templates are intentionally minimal start states that tests can refine with toBuilder().
        try (InputStream inputStream = CaseDataTemplate.class
            .getResourceAsStream("/templates/case-data/" + fileName + ".json")) {
            return MAPPER.readValue(
                Objects.requireNonNull(inputStream, "Missing case data template: " + fileName),
                CaseData.class
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load case template: " + fileName, e);
        }
    }

    public static CaseData CLAIM_ISSUED() {
        return load("claim-issued");
    }
}

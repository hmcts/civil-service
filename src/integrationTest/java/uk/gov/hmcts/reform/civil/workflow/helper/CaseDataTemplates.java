package uk.gov.hmcts.reform.civil.workflow.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("java:S112")
public final class CaseDataTemplates {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    public static final String TEMPLATES_CASE_DATA = "/templates/case-data/";

    private CaseDataTemplates() {
    }

    public static CaseData load(String fileName) {
        return load(fileName, CaseData.class);
    }

    public static CaseData load(String fileName, Consumer<ObjectNode> mutator) {
        return load(fileName, CaseData.class, mutator);
    }

    public static <T> T load(String fileName, Class<T> targetClass) {
        try (InputStream inputStream = CaseDataTemplates.class
            .getResourceAsStream(TEMPLATES_CASE_DATA + fileName + ".json")) {
            return MAPPER.treeToValue(loadTemplateNode(fileName, inputStream), targetClass);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load case template: " + fileName, e);
        }
    }

    public static <T> T load(String fileName, Class<T> targetClass, Consumer<ObjectNode> mutator) {
        try (InputStream inputStream = CaseDataTemplates.class
            .getResourceAsStream(TEMPLATES_CASE_DATA + fileName + ".json")) {
            ObjectNode template = loadTemplateNode(fileName, inputStream);
            mutator.accept(template);
            return MAPPER.treeToValue(template, targetClass);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load case template: " + fileName, e);
        }
    }

    public static void set(ObjectNode node, String fieldName, Object value) {
        node.set(fieldName, value == null ? MAPPER.nullNode() : MAPPER.valueToTree(value));
    }

    private static ObjectNode loadTemplateNode(String fileName, InputStream inputStream) throws IOException {
        JsonNode template = MAPPER.readTree(
            Objects.requireNonNull(inputStream, "Missing case data template: " + fileName)
        );
        return (ObjectNode) template;
    }
}

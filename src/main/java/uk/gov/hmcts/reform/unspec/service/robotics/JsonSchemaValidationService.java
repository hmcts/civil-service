package uk.gov.hmcts.reform.unspec.service.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.service.robotics.exception.JsonSchemaValidationException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
@Service
public class JsonSchemaValidationService {

    private String jsonSchemaFile;

    public JsonSchemaValidationService() {
        this.jsonSchemaFile = "/schema/rpa-json-schema.json";
    }

    public JsonSchemaValidationService(String jsonSchemaFile) {
        this.jsonSchemaFile = jsonSchemaFile;
    }

    public boolean isValid(String body) {
        return isValid(body, jsonSchemaFile);
    }

    public boolean isValid(String body, String jsonSchemaFileName) {
        Set<ValidationMessage> errors = validate(body, jsonSchemaFileName);
        if (!errors.isEmpty()) {
            log.error("Schema validation errors: {}", errors);
            return false;
        }
        return true;
    }

    public Set<ValidationMessage> validate(String payload) {
        return validate(payload, jsonSchemaFile);
    }

    public Set<ValidationMessage> validate(String body, String jsonSchemaFileName) {
        String jsonSchemaContents = readJsonSchema(jsonSchemaFileName);
        var jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(jsonSchemaContents);
        return jsonSchema.validate(getJsonNodeFromStringContent(body));
    }

    private JsonNode getJsonNodeFromStringContent(String content) {
        try {
            return new ObjectMapper().readTree(content);
        } catch (JsonProcessingException e) {
            throw new JsonSchemaValidationException(e.getMessage(), e);
        }
    }

    private String readJsonSchema(String input) {
        try {
            URL resource = getClass().getResource(input);
            URI url = resource.toURI();
            return Files.readString(Paths.get(url));
        } catch (NoSuchFileException | NullPointerException e) {
            throw new JsonSchemaValidationException(format("no file found with the link '%s'", input), e);
        } catch (IOException | URISyntaxException e) {
            throw new JsonSchemaValidationException(format("failed to read from file '%s'", input), e);
        }
    }

    public String getJsonSchemaFile() {
        return jsonSchemaFile;
    }
}

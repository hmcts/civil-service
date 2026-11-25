package uk.gov.hmcts.reform.civil.service.robotics;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.utils.ResourceReader;

import java.util.List;

import static java.lang.String.format;

@Slf4j
@Service
public class JsonSchemaValidationService {

    private String jsonSchemaFile;

    @Autowired
    public JsonSchemaValidationService() {
        this.jsonSchemaFile = "schema/rpa-json-schema.json";
    }

    public JsonSchemaValidationService(String jsonSchemaFile) {
        this.jsonSchemaFile = jsonSchemaFile;
    }

    public boolean isValid(String body) {
        return isValid(body, jsonSchemaFile);
    }

    public boolean isValid(String body, String jsonSchemaFileName) {
        List<Error> errors = validate(body, jsonSchemaFileName);
        if (!errors.isEmpty()) {
            log.error("Schema validation errors count: {}", errors.size());
            log.error("Schema validation errors: {}", errors);
            return false;
        }
        return true;
    }

    public List<Error> validate(String payload) {
        return validate(payload, jsonSchemaFile);
    }

    public List<Error> validate(String body, String jsonSchemaFileName) {
        String jsonSchemaContents = readJsonSchema(jsonSchemaFileName);
        var jsonSchema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7).getSchema(jsonSchemaContents);
        try {
            return jsonSchema.validate(body, InputFormat.JSON);
        } catch (JacksonException e) {
            throw new JsonSchemaValidationException(e.getMessage(), e);
        }
    }

    private String readJsonSchema(String input) {
        try {
            return ResourceReader.readString(input);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new JsonSchemaValidationException(format("no file found with the link '%s'", input), e);
        }
    }

    public String getJsonSchemaFile() {
        return jsonSchemaFile;
    }
}

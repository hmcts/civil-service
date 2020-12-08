package uk.gov.hmcts.reform.unspec.matcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

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
public class IsValidJson extends TypeSafeMatcher<String> {

    private String jsonSchemaFileName;

    public IsValidJson() {
        this.jsonSchemaFileName = "/rpa-json-schema.json";
    }

    public IsValidJson(String jsonSchemaFileName) {
        this.jsonSchemaFileName = jsonSchemaFileName;
    }

    @Override
    protected boolean matchesSafely(String json) {
        return isValidJsonPayload(json, jsonSchemaFileName);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("only valid as per json schema " + jsonSchemaFileName);
    }

    public static Matcher<String> validateJson() {
        return new IsValidJson();
    }

    public static Matcher<String> validateJson(String jsonSchemaFileName) {
        return new IsValidJson(jsonSchemaFileName);
    }

    private boolean isValidJsonPayload(String body, String jsonSchemaFileName) {
        String jsonSchemaContents = readJsonSchema(jsonSchemaFileName);
        var jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(jsonSchemaContents);
        Set<ValidationMessage> errors = jsonSchema.validate(getJsonNodeFromStringContent(body));
        if (!errors.isEmpty()) {
            log.error("Schema validation errors: {}", errors);
            return false;
        }
        return true;
    }

    @SneakyThrows
    private JsonNode getJsonNodeFromStringContent(String content) {
        return new ObjectMapper().readTree(content);
    }

    private String readJsonSchema(String input) {
        try {
            URL resource = getClass().getResource(input);
            URI url = resource.toURI();
            return Files.readString(Paths.get(url));
        } catch (NoSuchFileException e) {
            throw new RuntimeException(format("no file found with the link '%s'", input), e);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(format("failed to read from file '%s'", input), e);
        }
    }
}

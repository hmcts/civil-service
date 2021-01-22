package uk.gov.hmcts.reform.unspec.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import uk.gov.hmcts.reform.unspec.service.robotics.JsonSchemaValidationService;

public class IsValidJson extends TypeSafeMatcher<String> {

    private JsonSchemaValidationService validationService = new JsonSchemaValidationService();

    @Override
    protected boolean matchesSafely(String json) {
        return validationService.isValid(json);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("only valid as per json schema " + validationService.getJsonSchemaFile());
    }

    public static Matcher<String> validateJson() {
        return new IsValidJson();
    }
}

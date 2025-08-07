package uk.gov.hmcts.reform.civil.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.postcode.PostcodeLookupService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class PostcodeValidator {

    private final PostcodeLookupService postcodeLookupService;

    // Regex pattern for basic postcode format validation
    // UK postcodes: 1-2 letters, 1-2 digits, optional letter, space, digit, 2 letters
    private static final Pattern POSTCODE_PATTERN = Pattern.compile(
        "^[A-Z]{1,2}\\d{1,2}[A-Z]?\\s?\\d[A-Z]{2}$",
        Pattern.CASE_INSENSITIVE
    );

    // Maximum length for postcode input
    private static final int MAX_POSTCODE_LENGTH = 10;

    public PostcodeValidator(PostcodeLookupService postcodeLookupService) {
        this.postcodeLookupService = postcodeLookupService;
    }

    public List<String> validate(String postcode) {
        List<String> errors = new ArrayList<>();

        if (postcode == null) {
            errors.add("Please enter Postcode");
            return errors;
        }

        // Input sanitization and validation
        String sanitizedPostcode = sanitizePostcode(postcode);

        if (sanitizedPostcode.isEmpty()) {
            errors.add("Please enter Postcode");
            return errors;
        }

        if (sanitizedPostcode.length() > MAX_POSTCODE_LENGTH) {
            errors.add("Postcode format is invalid");
            return errors;
        }

        if (!POSTCODE_PATTERN.matcher(sanitizedPostcode).matches()) {
            errors.add("Postcode format is invalid");
            return errors;
        }

        // Check for Northern Ireland postcodes (BT prefix)
        if (isNorthernIrelandPostcode(sanitizedPostcode)) {
            errors.add("Postcode must be in England or Wales");
            return errors;
        }

        // Validate against lookup service
        try {
            if (!postcodeLookupService.validatePostCodeForDefendant(sanitizedPostcode)) {
                errors.add("Postcode must be in England or Wales");
            }
        } catch (Exception e) {
            // Log the exception appropriately in your application
            errors.add("Unable to validate postcode");
        }

        return errors;
    }

    private String sanitizePostcode(String postcode) {
        if (postcode == null) {
            return "";
        }

        // Remove any non-alphanumeric characters except spaces
        String cleaned = postcode.replaceAll("[^A-Za-z0-9\\s]", "");

        // Normalize whitespace and convert to uppercase
        return cleaned.trim().replaceAll("\\s+", " ").toUpperCase(Locale.UK);
    }

    private boolean isNorthernIrelandPostcode(String sanitizedPostcode) {
        return sanitizedPostcode.startsWith("BT");
    }
}

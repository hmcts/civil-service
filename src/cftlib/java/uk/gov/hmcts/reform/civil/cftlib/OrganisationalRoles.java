package uk.gov.hmcts.reform.civil.cftlib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The organisational roles bin/shared/add-org-roles-to-users.sh gives civil's test users in AAT and preview, read so
 * the local stack gives them the same ones. Each role is built as organisational-role-assignment.sh requests it.
 */
final class OrganisationalRoles {

    private static final String SCRIPT = "organisational-role-assignment.sh";

    // ${dir}/organisational-role-assignment.sh "email" "password" "classification" "role" 'attributes' "category"
    // ['authorisations']
    private static final Pattern ASSIGNMENT = Pattern.compile(
        Pattern.quote(SCRIPT)
            + "\\s+\"([^\"]+)\"\\s+\"[^\"]*\"\\s+\"([^\"]+)\"\\s+\"([^\"]+)\"\\s+'([^']*)'\\s+\"([^\"]+)\""
            + "(?:\\s+'([^']*)')?\\s*$"
    );

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OrganisationalRoles() {
    }

    /**
     * Each user's role assignments, by email, in the order the script gives them. A line that runs the assignment
     * script but can't be read fails, rather than leaving a user without a role they have in AAT.
     */
    static Map<String, List<Map<String, Object>>> fromScript(Path script) {
        List<String> lines;
        try {
            lines = Files.readAllLines(script);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + script, e);
        }
        Map<String, List<Map<String, Object>>> roles = new LinkedHashMap<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#") || !trimmed.contains(SCRIPT)) {
                continue;
            }
            Matcher matcher = ASSIGNMENT.matcher(trimmed);
            if (!matcher.find()) {
                throw new IllegalStateException("Could not read the role assignment in " + script + ": " + trimmed);
            }
            roles.computeIfAbsent(matcher.group(1), email -> new ArrayList<>()).add(assignment(matcher));
        }
        return roles;
    }

    private static Map<String, Object> assignment(Matcher matcher) {
        String roleName = matcher.group(3);
        Map<String, Object> assignment = new LinkedHashMap<>();
        assignment.put("roleType", "ORGANISATION");
        assignment.put("roleName", roleName);
        assignment.put("classification", matcher.group(2));
        // As the assignment script: the role assignment service only accepts hmcts-* roles as BASIC grants.
        assignment.put("grantType", roleName.startsWith("hmcts-") ? "BASIC" : "STANDARD");
        assignment.put("roleCategory", matcher.group(5));
        assignment.put("readOnly", false);
        assignment.put("attributes", json(matcher.group(4), new TypeReference<Map<String, Object>>() { }));
        assignment.put("authorisations", matcher.group(6) == null
            ? List.of() : json(matcher.group(6), new TypeReference<List<String>>() { }));
        return assignment;
    }

    private static <T> T json(String value, TypeReference<T> type) {
        try {
            return MAPPER.readValue(value, type);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + value, e);
        }
    }
}

package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.json.JsonBackedCCDConfig;
import uk.gov.hmcts.ccd.sdk.json.JsonCCDConfigSupport;
import uk.gov.hmcts.reform.civil.enums.CaseState;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

/**
 * Registers the CIVIL and GENERALAPPLICATION JSON definitions with the decentralised runtime, which runs their
 * about-to-submit and submitted callbacks through {@code DecentralisedCallbackHandler}. The build packages the
 * definitions from ccd-definition; this environment's files are picked by the patterns
 * {@code ccd-definition/exclusions.json} gives {@code civil.decentralised.definitions-environment}, as the spreadsheet
 * build does.
 *
 * <p>The runtime keeps case data as the maps CCD and the callbacks exchange. Civil's models only round-trip through
 * this service's own object mapper, so the runtime does not convert case data into them.
 */
@Configuration
@ConditionalOnProperty(name = "civil.decentralised.enabled", havingValue = "true")
public class CivilJsonCcdConfig {

    @Bean
    public CCDConfig<Map<String, Object>, CaseState, PlaceholderRole> civilCaseTypeConfig(
        JsonCCDConfigSupport support,
        ResourceLoader resources,
        @Value("${civil.decentralised.definitions-root}") String definitionsRoot,
        @Value("${civil.decentralised.definitions-environment}") String environment
    ) {
        return new JsonBackedCCDConfig<>(
            support, CASE_TYPE, definitionsRoot + "/civil", exclusions(resources, definitionsRoot, environment)
        ) { };
    }

    @Bean
    public CCDConfig<Map<String, Object>, CaseState, PlaceholderRole> generalApplicationCaseTypeConfig(
        JsonCCDConfigSupport support,
        ResourceLoader resources,
        @Value("${civil.decentralised.definitions-root}") String definitionsRoot,
        @Value("${civil.decentralised.definitions-environment}") String environment
    ) {
        return new JsonBackedCCDConfig<>(
            support, GENERALAPPLICATION_CASE_TYPE, definitionsRoot + "/generalapplication",
            exclusions(resources, definitionsRoot, environment)
        ) { };
    }

    /**
     * The file patterns exclusions.json leaves out for an environment. An environment it doesn't name fails startup,
     * rather than loading another environment's variants.
     */
    static List<String> exclusions(ResourceLoader resources, String definitionsRoot, String environment) {
        try (InputStream in = resources.getResource(definitionsRoot + "/exclusions.json").getInputStream()) {
            Map<String, List<String>> byEnvironment = new ObjectMapper().readValue(in, new TypeReference<>() { });
            List<String> patterns = byEnvironment.get(environment);
            if (patterns == null) {
                throw new IllegalStateException("ccd-definition/exclusions.json has no environment '" + environment
                    + "'; it has " + byEnvironment.keySet());
            }
            return patterns;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + definitionsRoot + "/exclusions.json", e);
        }
    }

    /**
     * Access is still defined in the JSON definitions; the runtime only needs a role type to build the config.
     */
    public enum PlaceholderRole implements HasRole {
        PLACEHOLDER;

        @Override
        public String getRole() {
            return name();
        }

        @Override
        public String getCaseTypePermissions() {
            return "";
        }
    }
}

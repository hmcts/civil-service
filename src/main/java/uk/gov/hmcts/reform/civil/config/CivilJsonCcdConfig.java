package uk.gov.hmcts.reform.civil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.json.JsonBackedCCDConfig;
import uk.gov.hmcts.ccd.sdk.json.JsonCCDConfigSupport;
import uk.gov.hmcts.reform.civil.enums.CaseState;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

/**
 * Registers the CIVIL and GENERALAPPLICATION JSON definitions with the decentralised runtime, which runs their
 * about-to-submit and submitted callbacks through {@code DecentralisedCallbackHandler}. The build packages the
 * definitions from civil-ccd-definition; {@code civil.decentralised.excluded-files} picks this environment's variants.
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
        @Value("${civil.decentralised.definitions-root}") String definitionsRoot,
        @Value("${civil.decentralised.excluded-files}") String excludedFiles
    ) {
        return new JsonBackedCCDConfig<>(support, CASE_TYPE, definitionsRoot + "/civil", patterns(excludedFiles)) { };
    }

    @Bean
    public CCDConfig<Map<String, Object>, CaseState, PlaceholderRole> generalApplicationCaseTypeConfig(
        JsonCCDConfigSupport support,
        @Value("${civil.decentralised.definitions-root}") String definitionsRoot,
        @Value("${civil.decentralised.excluded-files}") String excludedFiles
    ) {
        return new JsonBackedCCDConfig<>(
            support, GENERALAPPLICATION_CASE_TYPE, definitionsRoot + "/generalapplication", patterns(excludedFiles)
        ) { };
    }

    private static List<String> patterns(String excludedFiles) {
        return List.of(excludedFiles.split(","));
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

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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

/**
 * Registers the CIVIL and GENERALAPPLICATION JSON definitions with the decentralised runtime, which runs their
 * about-to-submit and submitted callbacks through {@code DecentralisedCallbackHandler}.
 */
@Configuration
@ConditionalOnProperty(name = "civil.decentralised.enabled", havingValue = "true")
public class CivilJsonCcdConfig {

    @Bean
    public CCDConfig<CaseData, CaseState, PlaceholderRole> civilJsonCcdConfig(
        JsonCCDConfigSupport support,
        @Value("${civil.decentralised.definitions-root}") String definitionsRoot
    ) {
        return new JsonBackedCCDConfig<>(support, CASE_TYPE, definitionsRoot + "/civil") { };
    }

    @Bean
    public CCDConfig<GeneralApplicationCaseData, CaseState, PlaceholderRole> generalApplicationJsonCcdConfig(
        JsonCCDConfigSupport support,
        @Value("${civil.decentralised.definitions-root}") String definitionsRoot
    ) {
        return new JsonBackedCCDConfig<>(support, GENERALAPPLICATION_CASE_TYPE, definitionsRoot + "/generalapplication") { };
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

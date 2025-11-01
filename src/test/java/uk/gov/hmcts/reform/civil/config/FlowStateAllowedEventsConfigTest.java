package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlowStateAllowedEventsConfigTest {

    @Nested
    @DisplayName("Test Binding Config")
    @SpringJUnitConfig(FlowStateAllowedEventsConfigTest.BindConfigTests.TestFlowStateConfig.class)
    class BindConfigTests {

        @Autowired
        private FlowStateAllowedEventsConfig config;

        @Configuration
        @EnableConfigurationProperties(FlowStateAllowedEventsConfig.class)
        @org.springframework.context.annotation.PropertySource(
            value = "classpath:config/flowstate-whitelist-events.yml", factory = YamlPropertySourceFactory.class)
        @org.springframework.context.annotation.PropertySource(
            value = "classpath:config/flowstate-allowed-events.yml", factory = YamlPropertySourceFactory.class)
        @org.springframework.context.annotation.PropertySource(
            value = "classpath:config/flowstate-allowed-spec-events.yml", factory = YamlPropertySourceFactory.class)
        static class TestFlowStateConfig {
        }

        @Test
        void shouldVerifyAllThreePropertiesAreBoundCorrectly() {
            assertThat(config.getEventWhitelist()).isNotNull().isNotEmpty();
            assertThat(config.getAllowedEvents()).isNotNull().isNotEmpty();
            assertThat(config.getAllowedEventsSpec()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Test Invalid Binding")
    class InvalidConfigTests {

        // Intentionally invalid configurations for negative tests
        @Configuration
        @EnableConfigurationProperties(FlowStateAllowedEventsConfig.class)
        @org.springframework.context.annotation.PropertySource(
            value = "classpath:config/invalid-flowstate-whitelist-events.yml",
            factory = YamlPropertySourceFactory.class)
        static class InvalidWhitelistTestConfig {
        }

        @Configuration
        @EnableConfigurationProperties(FlowStateAllowedEventsConfig.class)
        @org.springframework.context.annotation.PropertySource(
            value = "classpath:config/invalid-flowstate-allowed-events.yml",
            factory = YamlPropertySourceFactory.class)
        static class InvalidAllowedEventsTestConfig {
        }

        @Configuration
        @EnableConfigurationProperties(FlowStateAllowedEventsConfig.class)
        @org.springframework.context.annotation.PropertySource(
            value = "classpath:config/invalid-flowstate-allowed-spec-events.yml",
            factory = YamlPropertySourceFactory.class)
        static class InvalidAllowedSpecEventsTestConfig {
        }

        @Test
        void shouldFailToLoadContextWhenWhitelistYamlHasInvalidEventName() {
            assertThatThrownBy(() -> new org.springframework.context.annotation.AnnotationConfigApplicationContext(
                InvalidWhitelistTestConfig.class)).isInstanceOf(Exception.class);
        }

        @Test
        void shouldFailToLoadContextWhenAllowedEventsYamlHasInvalidEventName() {
            assertThatThrownBy(() -> new org.springframework.context.annotation.AnnotationConfigApplicationContext(
                InvalidAllowedEventsTestConfig.class)).isInstanceOf(Exception.class);
        }

        @Test
        void shouldFailToLoadContextWhenAllowedSpecEventsYamlHasInvalidEventName() {
            assertThatThrownBy(() -> new org.springframework.context.annotation.AnnotationConfigApplicationContext(
                InvalidAllowedSpecEventsTestConfig.class)).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Test Valid Config")
    @SpringJUnitConfig(FlowStateAllowedEventsConfigTest.ValidConfigTests.TestFlowStateConfig.class)
    class ValidConfigTests {

        @Autowired
        private FlowStateAllowedEventsConfig config;

        @Configuration
        @EnableConfigurationProperties(FlowStateAllowedEventsConfig.class)
        @org.springframework.context.annotation.PropertySource(
            value = "classpath:config/valid-flowstate-events.yml", factory = YamlPropertySourceFactory.class)
        static class TestFlowStateConfig {
        }

        @Test
        void shouldVerifyConfigurationMethodsWorkCorrectly() {
            // whitelist
            assertThat(config.isWhitelistEvent(CaseEvent.SEND_AND_REPLY)).isTrue();
            assertThat(config.isWhitelistEvent(CaseEvent.DISMISS_CLAIM)).isFalse();

            // allowed events
            List<CaseEvent> draftEvents = config.getAllowedEvents("MAIN.DRAFT");
            assertThat(draftEvents).contains(CaseEvent.CREATE_CLAIM, CaseEvent.migrateCase);

            List<String> statesForMigrateCase = config.getAllowedStates(CaseEvent.migrateCase);
            assertThat(statesForMigrateCase).isNotEmpty().contains("MAIN.DRAFT", "MAIN.CLAIM_SUBMITTED");

            List<String> statesForCreateClaim = config.getAllowedStates(CaseEvent.CREATE_CLAIM);
            assertThat(statesForCreateClaim).isNotEmpty().contains("MAIN.DRAFT");

            // allowed events spec
            List<CaseEvent> specDraftEvents = config.getAllowedEventsSpec("MAIN.SPEC_DRAFT");
            assertThat(specDraftEvents).contains(
                CaseEvent.CREATE_CLAIM_SPEC,
                CaseEvent.CREATE_LIP_CLAIM,
                CaseEvent.migrateCase
            );

            List<String> specStatesForMigrateCase = config.getAllowedStatesSpec(CaseEvent.migrateCase);
            assertThat(specStatesForMigrateCase).isNotEmpty().contains("MAIN.DRAFT", "MAIN.SPEC_DRAFT");

            List<String> statesForCreateClaimSpec = config.getAllowedStatesSpec(CaseEvent.CREATE_CLAIM_SPEC);
            assertThat(statesForCreateClaimSpec).isNotEmpty().contains("MAIN.SPEC_DRAFT");
        }

        @Test
        void shouldReturnEmptyCollections_forUnknownOrMissingEntries() {
            assertThat(config.getAllowedEvents("MAIN.NOT_PRESENT")).isEmpty();
            assertThat(config.getAllowedEvents("INVALID.STATE")).isEmpty();
            assertThat(config.getAllowedStates(CaseEvent.CONTACT_INFORMATION_UPDATED)).isEmpty();

            assertThat(config.getAllowedEventsSpec("MAIN.NOT_PRESENT")).isEmpty();
            assertThat(config.getAllowedEventsSpec("INVALID.STATE")).isEmpty();
            assertThat(config.getAllowedStatesSpec(CaseEvent.CONTACT_INFORMATION_UPDATED_WA)).isEmpty();
        }

        @Test
        void shouldBindEventWhitelistFromYaml() {
            List<CaseEvent> eventWhitelist = config.getEventWhitelist();

            assertThat(eventWhitelist).isNotNull().isNotEmpty().contains(
                CaseEvent.SEND_AND_REPLY,
                CaseEvent.ADD_CASE_NOTE,
                CaseEvent.REMOVE_DOCUMENT
            );
        }

        @SuppressWarnings("java:S5853")
        @Test
        void shouldBindAllowedEventsFromYaml() {
            Map<String, List<CaseEvent>> allowedEvents = config.getAllowedEvents();

            assertThat(allowedEvents).isNotNull().isNotEmpty();

            assertThat(allowedEvents).containsKey("MAIN.DRAFT");
            assertThat(allowedEvents.get("MAIN.DRAFT")).contains(CaseEvent.CREATE_CLAIM, CaseEvent.migrateCase);

            assertThat(allowedEvents).containsKey("MAIN.CLAIM_SUBMITTED");
            assertThat(allowedEvents.get("MAIN.CLAIM_SUBMITTED")).contains(
                CaseEvent.INITIATE_GENERAL_APPLICATION,
                CaseEvent.migrateCase
            );
        }

        @SuppressWarnings("java:S5853")
        @Test
        void shouldBindAllowedEventsSpecFromYaml() {
            Map<String, List<CaseEvent>> allowedEventsSpec = config.getAllowedEventsSpec();

            assertThat(allowedEventsSpec).isNotNull().isNotEmpty();

            assertThat(allowedEventsSpec).containsKey("MAIN.DRAFT");
            assertThat(allowedEventsSpec.get("MAIN.DRAFT")).contains(
                CaseEvent.CREATE_CLAIM,
                CaseEvent.CREATE_LIP_CLAIM,
                CaseEvent.migrateCase
            );

            assertThat(allowedEventsSpec).containsKey("MAIN.SPEC_DRAFT");
            assertThat(allowedEventsSpec.get("MAIN.SPEC_DRAFT")).contains(
                CaseEvent.CREATE_CLAIM_SPEC,
                CaseEvent.CREATE_LIP_CLAIM,
                CaseEvent.migrateCase
            );
        }
    }

}

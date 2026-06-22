package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseUserRolesCachePropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        CaseUserRolesCacheProperties properties = new CaseUserRolesCacheProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getTtlSeconds()).isEqualTo(30);
        assertThat(properties.getNegativeTtlSeconds()).isEqualTo(10);
        assertThat(properties.getKeyPrefix()).isEqualTo("civil:v1:case-user-roles");
        assertThat(properties.getCaffeineMaxSize()).isEqualTo(10000);
    }

    @Test
    void shouldSetEnabled() {
        CaseUserRolesCacheProperties properties = new CaseUserRolesCacheProperties();
        properties.setEnabled(false);

        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void shouldSetTtlSeconds() {
        CaseUserRolesCacheProperties properties = new CaseUserRolesCacheProperties();
        properties.setTtlSeconds(60);

        assertThat(properties.getTtlSeconds()).isEqualTo(60);
    }

    @Test
    void shouldSetNegativeTtlSeconds() {
        CaseUserRolesCacheProperties properties = new CaseUserRolesCacheProperties();
        properties.setNegativeTtlSeconds(5);

        assertThat(properties.getNegativeTtlSeconds()).isEqualTo(5);
    }

    @Test
    void shouldSetKeyPrefix() {
        CaseUserRolesCacheProperties properties = new CaseUserRolesCacheProperties();
        String newPrefix = "custom:prefix";
        properties.setKeyPrefix(newPrefix);

        assertThat(properties.getKeyPrefix()).isEqualTo(newPrefix);
    }

    @Test
    void shouldSetCaffeineMaxSize() {
        CaseUserRolesCacheProperties properties = new CaseUserRolesCacheProperties();
        properties.setCaffeineMaxSize(5000);

        assertThat(properties.getCaffeineMaxSize()).isEqualTo(5000);
    }

    @Test
    void shouldAllowMultiplePropertyUpdates() {
        CaseUserRolesCacheProperties properties = new CaseUserRolesCacheProperties();

        properties.setEnabled(false);
        properties.setTtlSeconds(120);
        properties.setNegativeTtlSeconds(15);
        properties.setKeyPrefix("custom:key");
        properties.setCaffeineMaxSize(20000);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getTtlSeconds()).isEqualTo(120);
        assertThat(properties.getNegativeTtlSeconds()).isEqualTo(15);
        assertThat(properties.getKeyPrefix()).isEqualTo("custom:key");
        assertThat(properties.getCaffeineMaxSize()).isEqualTo(20000);
    }
}
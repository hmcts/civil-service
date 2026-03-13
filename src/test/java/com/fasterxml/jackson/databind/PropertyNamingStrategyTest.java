package com.fasterxml.jackson.databind;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyNamingStrategyTest {

    private final PropertyNamingStrategy strategy = new PropertyNamingStrategy() {
    };

    @Test
    void shouldReturnDefaultFieldName() {
        assertThat(strategy.nameForField(null, null, "field_name")).isEqualTo("field_name");
    }

    @Test
    void shouldReturnDefaultGetterName() {
        assertThat(strategy.nameForGetterMethod(null, null, "getter_name")).isEqualTo("getter_name");
    }

    @Test
    void shouldReturnDefaultSetterName() {
        assertThat(strategy.nameForSetterMethod(null, null, "setter_name")).isEqualTo("setter_name");
    }

    @Test
    void shouldReturnDefaultConstructorParameterName() {
        assertThat(strategy.nameForConstructorParameter(null, null, "param_name")).isEqualTo("param_name");
    }

    @Test
    void shouldExposeSnakeCaseStrategyType() {
        PropertyNamingStrategy.SnakeCaseStrategy snakeCase = new PropertyNamingStrategy.SnakeCaseStrategy();

        assertThat(snakeCase).isInstanceOf(PropertyNamingStrategies.SnakeCaseStrategy.class);
    }
}

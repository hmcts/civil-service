package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;

import java.io.Serializable;

/**
 * Compatibility bridge for legacy DTO annotations that still reference
 * PropertyNamingStrategy.SnakeCaseStrategy.
 * TODO(DTSCCI-3888): Remove once all upstream DTOs use PropertyNamingStrategies directly
 * (or equivalent Jackson 3-compatible annotations) and no longer load this legacy type.
 */
public abstract class PropertyNamingStrategy implements Serializable {

    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return defaultName;
    }

    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return defaultName;
    }

    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return defaultName;
    }

    public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, String defaultName) {
        return defaultName;
    }

    public static class SnakeCaseStrategy extends PropertyNamingStrategies.SnakeCaseStrategy {
    }
}

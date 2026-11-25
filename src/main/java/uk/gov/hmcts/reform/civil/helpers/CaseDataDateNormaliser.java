package uk.gov.hmcts.reform.civil.helpers;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes the dates in an event's updates from outside this service, such as civil-citizen-ui's, as CCD's Date fields
 * expect them.
 *
 * <p>Callers send some dates as date-times, such as {@code 2026-11-24T00:00:00.000Z}. CCD stores the about-to-submit
 * callback's data, which this service has read into its model and written back as plain dates, and validates that.
 * For a decentralised case CCD validates the event's own data instead, before the runtime runs the callback, and
 * refuses a date-time in a Date field. So each value whose field is a {@link LocalDate} in the model is read as the
 * model reads it, and written as the callback would write it. Everything else is left as it was sent.
 *
 * <p>Centralised, CCD validates after the callback, so with {@code civil.decentralised.enabled} off the updates are
 * sent as they came.
 */
@Component
public class CaseDataDateNormaliser {

    private final ObjectMapper objectMapper;
    private final boolean decentralised;
    private final Map<JavaType, Map<String, JavaType>> properties = new ConcurrentHashMap<>();

    public CaseDataDateNormaliser(ObjectMapper objectMapper,
                                  @Value("${civil.decentralised.enabled:false}") boolean decentralised) {
        this.objectMapper = objectMapper;
        this.decentralised = decentralised;
    }

    public Map<String, Object> normalise(Map<String, Object> updates, Class<?> caseDataClass) {
        if (updates == null || !decentralised) {
            return updates;
        }
        return normaliseFields(updates, objectMapper.constructType(caseDataClass));
    }

    private Map<String, Object> normaliseFields(Map<?, ?> fields, JavaType beanType) {
        Map<String, JavaType> types = properties.computeIfAbsent(beanType, this::propertyTypes);
        Map<String, Object> normalised = new LinkedHashMap<>();
        fields.forEach((name, value) -> normalised.put(String.valueOf(name), normaliseValue(value, types.get(name))));
        return normalised;
    }

    private Object normaliseValue(Object value, JavaType type) {
        if (value == null || type == null) {
            return value;
        }
        if (type.hasRawClass(LocalDate.class)) {
            return value instanceof String text ? asDate(text) : value;
        }
        if (value instanceof Map<?, ?> map) {
            if (type.isMapLikeType()) {
                Map<Object, Object> normalised = new LinkedHashMap<>();
                map.forEach((key, item) -> normalised.put(key, normaliseValue(item, type.getContentType())));
                return normalised;
            }
            return type.isContainerType() ? value : normaliseFields(map, type);
        }
        if (value instanceof List<?> list && type.isCollectionLikeType()) {
            List<Object> normalised = new ArrayList<>(list.size());
            list.forEach(item -> normalised.add(normaliseValue(item, type.getContentType())));
            return normalised;
        }
        return value;
    }

    private String asDate(String text) {
        try {
            return objectMapper.convertValue(text, LocalDate.class).toString();
        } catch (IllegalArgumentException e) {
            // Not a date the model can read: CCD reports it as it would have.
            return text;
        }
    }

    private Map<String, JavaType> propertyTypes(JavaType beanType) {
        Map<String, JavaType> types = new ConcurrentHashMap<>();
        for (BeanPropertyDefinition property : objectMapper.getSerializationConfig().introspect(beanType)
            .findProperties()) {
            JavaType propertyType = property.getPrimaryType();
            if (propertyType != null) {
                types.put(property.getName(), propertyType);
            }
        }
        return types;
    }
}

package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Data
@NoArgsConstructor
public class BaseCaseData implements MappableObject {

    @JsonIgnore
    private String ccdCaseType;

    @JsonIgnore
    public BaseCaseData copy() {
        return copyInto(new BaseCaseData());
    }

    @JsonIgnore
    protected <T extends BaseCaseData> T copyInto(T target) {
        copyFields(getClass(), target);
        return target;
    }

    private void copyFields(Class<?> sourceClass, Object target) {
        if (sourceClass == null || sourceClass == Object.class) {
            return;
        }
        copyFields(sourceClass.getSuperclass(), target);
        for (Field field : sourceClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            boolean accessible = field.canAccess(this);
            field.setAccessible(true);
            try {
                field.set(target, field.get(this));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to copy field " + field.getName(), e);
            } finally {
                field.setAccessible(accessible);
            }
        }
    }
}

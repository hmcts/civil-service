package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.Method;

@JacksonStdImpl
public class CustomEnumSerializer extends StdSerializer<Enum<?>> {

    public CustomEnumSerializer() {
        super(Enum.class, true);
    }

    @Override
    public final void serialize(Enum<?> en, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        try {
            String id = en.name();
            Class<?> declaringClass = en.getDeclaringClass();
            Method method = declaringClass.getDeclaredMethod("getDisplayedValue");
            String displayedValue = (String) method.invoke(en);
            if (serializers.isEnabled(SerializationFeature.WRITE_ENUM_KEYS_USING_INDEX)) {
                gen.writeString(id);
            } else if (serializers.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)) {
                gen.writeString(displayedValue);
            } else {
                gen.writeString(en.toString());
            }
        } catch (Exception e) {
            gen.writeString(en.toString());
        }
    }
}

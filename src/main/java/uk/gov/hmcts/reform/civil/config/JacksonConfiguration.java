package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.reform.civil.enums.UserRole;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static com.fasterxml.jackson.databind.MapperFeature.INFER_BUILDER_TYPE_BINDINGS;

@Configuration
public class JacksonConfiguration {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
        .ofPattern(DATE_TIME_FORMAT, Locale.UK);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonDateTimeFormatCustomizer() {
        return builder -> {
            builder.simpleDateFormat(DATE_TIME_FORMAT);
            builder.serializers(new LocalDateSerializer(DATE_FORMATTER));
            builder.serializers(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        };
    }

    public ObjectMapper getMapper() {
        ObjectMapper mapper = JsonMapper.builder()
            .configure(ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .enable(INFER_BUILDER_TYPE_BINDINGS)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

        SimpleModule deserialization = new SimpleModule();
        deserialization.addDeserializer(HasRole.class, new HasRoleDeserializer());
        mapper.registerModule(deserialization);

        JavaTimeModule datetime = new JavaTimeModule();
        datetime.addSerializer(LocalDateSerializer.INSTANCE);
        mapper.registerModule(datetime);

        return mapper;
    }

    public static class HasRoleDeserializer extends StdDeserializer<HasRole> {
        static final long serialVersionUID = 1L;

        public HasRoleDeserializer() {
            this(null);
        }

        protected HasRoleDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public HasRole deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode node = parser.readValueAsTree();

            return Arrays
                .stream(UserRole.values())
                .filter(r -> r.getRole().equals(node.asText()))
                .findFirst()
                .get();
        }
    }
}

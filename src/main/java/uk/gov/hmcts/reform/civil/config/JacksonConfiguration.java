package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseDataParent;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

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
            builder.mixIn(CaseDataParent.class, CaseDataParentMixin.class);
        };
    }

    private abstract static class CaseDataParentMixin {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        abstract Set<DefendantResponseShowTag> getShowConditionFlags();
    }
}

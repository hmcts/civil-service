package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LenientLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (JsonToken.START_ARRAY == parser.currentToken()) {
            return deserializeArray(parser);
        }

        String raw = parser.getValueAsString();
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException exception) {
            log.warn("Unparseable LocalDate '{}' for field '{}' - treating as null", raw, parser.currentName());
            return null;
        }
    }

    private LocalDate deserializeArray(JsonParser parser) throws IOException {
        List<Integer> values = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (!parser.currentToken().isNumeric()) {
                parser.skipChildren();
                log.warn("Unparseable LocalDate array for field '{}' - treating as null", parser.currentName());
                return null;
            }
            values.add(parser.getIntValue());
        }

        if (values.size() != 3) {
            log.warn("Unparseable LocalDate array '{}' for field '{}' - treating as null", values, parser.currentName());
            return null;
        }

        try {
            return LocalDate.of(values.get(0), values.get(1), values.get(2));
        } catch (DateTimeException exception) {
            log.warn("Unparseable LocalDate array '{}' for field '{}' - treating as null", values, parser.currentName());
            return null;
        }
    }
}

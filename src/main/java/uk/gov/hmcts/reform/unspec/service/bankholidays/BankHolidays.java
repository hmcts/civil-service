package uk.gov.hmcts.reform.unspec.service.bankholidays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankHolidays {

    @JsonProperty(Countries.ENGLAND_AND_WALES)
    Division englandAndWales;

    static final class Countries {

        static final String ENGLAND_AND_WALES = "england-and-wales";

        private Countries() {
            // NO-OP
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Division {
        @JsonProperty("events")
        List<EventDate> events;

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class EventDate {
            @JsonProperty("date")
            @JsonDeserialize(using = LocalDateDeserializer.class)
            LocalDate date;
        }
    }
}

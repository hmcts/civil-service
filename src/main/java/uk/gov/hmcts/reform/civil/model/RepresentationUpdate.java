package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepresentationUpdate {
    @JsonProperty("party")
    String party;

    @JsonProperty("name")
    String clientName;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    LocalDateTime date;

    @JsonProperty("by")
    String by;

    @JsonProperty("via")
    String via;

    @JsonProperty("added")
    ChangedRepresentative added;

    @JsonProperty("removed")
    ChangedRepresentative removed;
}

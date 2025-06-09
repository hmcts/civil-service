package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static java.util.Objects.requireNonNull;

@Slf4j
@Schema(
    name = "Warning",
    description = "Warning object containing the list of warnings (warningCode,warningText)"
)
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class WarningValues {
    @Schema(requiredMode = REQUIRED,
        description = "A list of warnings")
    private List<Warning> values = new ArrayList<>();

    public WarningValues(List<Warning> values) {
        requireNonNull(values);
        this.values = values;
    }

    public WarningValues(String values) {
        requireNonNull(values);
        try {
            this.values = new ObjectMapper().reader()
                .forType(new TypeReference<List<Warning>>() {
                })
                .readValue(values);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("Could not deserialize values");
        }
    }

    public List<Warning> getValues() {
        return values;
    }

    @JsonIgnore
    public String getValuesAsJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(values);
    }
}

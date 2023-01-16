package uk.gov.hmcts.reform.civil.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Element<T> {

    @JsonProperty("id")
    private final UUID id;
    @NotNull
    @Valid
    @JsonProperty("value")
    private final T value;
}

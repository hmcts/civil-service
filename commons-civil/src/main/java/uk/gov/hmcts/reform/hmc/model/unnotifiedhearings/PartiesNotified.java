package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PartiesNotified {

    @JsonProperty("serviceData")
    private PartiesNotifiedServiceData serviceData;
}

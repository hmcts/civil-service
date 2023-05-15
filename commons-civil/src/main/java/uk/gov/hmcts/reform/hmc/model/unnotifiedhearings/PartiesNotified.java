package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartiesNotified {
    private Integer requestVersion;
    private JsonNode serviceData;
}

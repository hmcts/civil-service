package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartiesNotifiedResponse {

    private LocalDateTime responseReceivedDateTime;

    private Integer requestVersion;

    private LocalDateTime partiesNotified;

    private JsonNode serviceData;
}

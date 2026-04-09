package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PartiesNotifiedResponse {

    private LocalDateTime responseReceivedDateTime;

    private Integer requestVersion;

    private LocalDateTime partiesNotified;

    private PartiesNotifiedServiceData serviceData;
}

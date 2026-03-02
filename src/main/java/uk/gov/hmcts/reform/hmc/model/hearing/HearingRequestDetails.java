package uk.gov.hmcts.reform.hmc.model.hearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class HearingRequestDetails {

    @JsonProperty("hearingRequestID")
    private String hearingRequestId;
    private String status;
    private LocalDateTime timestamp;
    private Long versionNumber;
    private String hearingGroupRequestId;
    private LocalDateTime partiesNotified;
}

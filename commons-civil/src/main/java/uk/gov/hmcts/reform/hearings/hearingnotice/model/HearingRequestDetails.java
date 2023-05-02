package uk.gov.hmcts.reform.hearings.hearingnotice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
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

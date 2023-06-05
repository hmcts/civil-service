package uk.gov.hmcts.reform.hmc.model.hearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingResponse {

    @JsonProperty("listAssistTransactionID")
    private String listAssistTransactionId;

    private LocalDateTime receivedDateTime;

    @JsonProperty("laCaseStatus")
    private ListAssistCaseStatus listAssistCaseStatus;

    private ListingStatus listingStatus;

    private String hearingCancellationReason;

    private List<HearingDaySchedule> hearingDaySchedule;
}

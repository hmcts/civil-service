package uk.gov.hmcts.reform.hearings.hearingnotice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.ListingStatus;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.ListAssistCaseStatus;

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

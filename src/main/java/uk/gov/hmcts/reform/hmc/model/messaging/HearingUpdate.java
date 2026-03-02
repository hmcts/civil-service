package uk.gov.hmcts.reform.hmc.model.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.hmc.model.hearing.ListAssistCaseStatus;
import uk.gov.hmcts.reform.hmc.model.hearing.ListingStatus;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class HearingUpdate {

    private LocalDateTime hearingResponseReceivedDateTime;

    private LocalDateTime hearingEventBroadcastDateTime;

    @JsonProperty("HMCStatus")
    private HmcStatus hmcStatus;

    @JsonProperty("hearingListingStatus")
    private ListingStatus listingStatus;

    private LocalDateTime nextHearingDate;

    @JsonProperty("ListAssistCaseStatus")
    private ListAssistCaseStatus listAssistCaseStatus;

    private String listAssistSessionID;

    @JsonProperty("hearingVenueId")
    private String hearingEpimsId;

    private String hearingRoomId;

    private String hearingJudgeId;
}

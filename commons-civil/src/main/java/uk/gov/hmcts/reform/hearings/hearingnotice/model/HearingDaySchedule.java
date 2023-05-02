package uk.gov.hmcts.reform.hearings.hearingnotice.model;

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
public class HearingDaySchedule {

    private LocalDateTime hearingStartDateTime;

    private LocalDateTime hearingEndDateTime;

    private String listAssistSessionId;

    @JsonProperty("hearingVenueId")
    private String hearingVenueEpimsId;

    private String hearingRoomId;

    private String hearingJudgeId;

    private List<String> panelMemberIds;

    private List<Attendees> attendees;
}

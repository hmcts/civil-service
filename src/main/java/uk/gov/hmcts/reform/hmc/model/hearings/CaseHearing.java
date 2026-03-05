package uk.gov.hmcts.reform.hmc.model.hearings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class CaseHearing {

    @JsonProperty("hearingID")
    private Long hearingId;

    private LocalDateTime hearingRequestDateTime;

    private String hearingType;

    private String hmcStatus;

    private LocalDateTime lastResponseReceivedDateTime;

    private Integer requestVersion;

    private String hearingListingStatus;

    private String listAssistCaseStatus;

    private List<HearingDaySchedule> hearingDaySchedule;

    private String hearingGroupRequestId;

    private Boolean hearingIsLinkedFlag;

    private List<String> hearingChannels;

}

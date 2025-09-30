package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
public class GAHearingNoticeDetail {

    private DynamicList hearingLocation;
    private LocalDate hearingDate;
    private String hearingTimeHourMinute;
    private GAJudicialHearingType channel;
    private GAHearingDuration hearingDuration;
    private String hearingDurationOther;

    @JsonCreator
    GAHearingNoticeDetail(@JsonProperty("hearingLocation") DynamicList hearingLocation,
                          @JsonProperty("hearingDate") LocalDate hearingDate,
                          @JsonProperty("hearingTimeHourMinute") String hearingTimeHourMinute,
                          @JsonProperty("channel") GAJudicialHearingType channel,
                          @JsonProperty("hearingDuration") GAHearingDuration hearingDuration,
                          @JsonProperty("hearingDurationOther") String hearingDurationOther) {
        this.hearingLocation = hearingLocation;
        this.hearingDate = hearingDate;
        this.hearingTimeHourMinute = hearingTimeHourMinute;
        this.channel = channel;
        this.hearingDuration = hearingDuration;
        this.hearingDurationOther = hearingDurationOther;
    }
}

package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TimelineOfEventDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private final LocalDate timelineDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String timelineDescription;

    @JsonCreator
    public TimelineOfEventDetails(@JsonProperty("timelineDate") LocalDate timelineDate,
                                  @JsonProperty("timelineDescription") String timelineDescription) {
        this.timelineDate = timelineDate;
        this.timelineDescription = timelineDescription;
    }

}

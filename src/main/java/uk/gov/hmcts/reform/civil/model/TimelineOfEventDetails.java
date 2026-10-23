package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TimelineOfEventDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate timelineDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timelineDay;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timelineMonth;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timelineYear;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timelineDescription;

    @JsonCreator
    public TimelineOfEventDetails(@JsonProperty("timelineDate") LocalDate timelineDate,
                                  @JsonProperty("timelineDay") String timelineDay,
                                  @JsonProperty("timelineMonth") String timelineMonth,
                                  @JsonProperty("timelineYear") String timelineYear,
                                  @JsonProperty("timelineDescription") String timelineDescription) {
        this.timelineDate = timelineDate;
        this.timelineDay = timelineDay;
        this.timelineMonth = timelineMonth;
        this.timelineYear = timelineYear;
        this.timelineDescription = timelineDescription;
    }

}

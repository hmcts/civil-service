package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "TimelineOfEvents", generate = true)
@Data
@NoArgsConstructor
public class TimelineOfEventDetails {

    @CCD(label = "Date", hint = "For example, 12 11 2007", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate timelineDate;
    @CCD(label = "What happened", searchable = false, typeOverride = FieldType.TextArea)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timelineDescription;

    @JsonCreator
    public TimelineOfEventDetails(@JsonProperty("timelineDate") LocalDate timelineDate,
                                  @JsonProperty("timelineDescription") String timelineDescription) {
        this.timelineDate = timelineDate;
        this.timelineDescription = timelineDescription;
    }

}

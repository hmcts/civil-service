package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EventTemplateData {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate date;
    private String explanation;

    @JsonIgnore
    public static List<EventTemplateData> toEventTemplateDataList(List<TimelineOfEvents> timelineOfEvents) {
        return Optional.ofNullable(timelineOfEvents).map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(event ->
                     new EventTemplateData()
                         .setDate(event.getValue().getTimelineDate())
                         .setExplanation(event.getValue().getTimelineDescription())).toList();
    }
}

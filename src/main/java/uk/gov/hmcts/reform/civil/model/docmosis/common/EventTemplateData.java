package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Builder
public class EventTemplateData {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate date;
    private String explanation;

    @JsonIgnore
    public static List<EventTemplateData> toEventTemplateDataList(List<TimelineOfEvents> timelineOfEvents) {
        return Optional.ofNullable(timelineOfEvents).map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(event ->
                     EventTemplateData.builder()
                         .date(event.getValue().getTimelineDate())
                         .explanation(event.getValue().getTimelineDescription())
                         .build()).collect(Collectors.toList());
    }
}

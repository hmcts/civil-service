package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;

import java.time.LocalDate;

/**
 * TimelineOfEventDetails' date format is different when brought from front and when used in Docmosis' templates.
 * One solution would be to write a custom serializer for TimelineEventDetails and then use it in docmosis' objects,
 * but this solution seems less convoluted
 */
public class TimelineEventDetailsDocmosis extends TimelineOfEventDetails {

    public TimelineEventDetailsDocmosis(LocalDate timelineDate, String timelineDay, String timelineMonth, String timelineYear, String timelineDescription) {
        super(timelineDate, timelineDay, timelineMonth, timelineYear, timelineDescription);
    }

    public TimelineEventDetailsDocmosis(TimelineOfEventDetails details) {
        this(details.getTimelineDate(), details.getTimelineDay(), details.getTimelineMonth(), details.getTimelineYear(), details.getTimelineDescription());
    }

    @Override
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public LocalDate getTimelineDate() {
        return super.getTimelineDate();
    }
}

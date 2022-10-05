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

    public TimelineEventDetailsDocmosis(LocalDate timelineDate, String timelineDescription) {
        super(timelineDate, timelineDescription);
    }

    public TimelineEventDetailsDocmosis(TimelineOfEventDetails details) {
        this(details.getTimelineDate(), details.getTimelineDescription());
    }

    @Override
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public LocalDate getTimelineDate() {
        return super.getTimelineDate();
    }
}

package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

@Data
@Builder

public class TimelineOfEvents {

    private final TimelineOfEventDetails value;
    @JsonIgnore
    private final String id;
}

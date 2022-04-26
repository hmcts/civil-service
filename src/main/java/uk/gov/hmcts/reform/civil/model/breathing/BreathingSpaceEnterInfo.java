package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BreathingSpaceEnterInfo {

    private final BreathingSpaceType type;

    private final String reference;

    private final LocalDate start;

    private final LocalDate expectedEnd;

    private final String event;

    private final String eventDescription;
}

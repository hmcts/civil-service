package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BreathingSpaceLiftInfo {

    private final LocalDate expectedEnd;

    private final String eventDescription;
}

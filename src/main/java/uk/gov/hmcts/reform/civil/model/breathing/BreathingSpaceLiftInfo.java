package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BreathingSpaceLiftInfo {

    private LocalDate expectedEnd;

    private String event;

    private String eventDescription;
}

package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BreathingSpaceLiftInfo {

    private LocalDate expectedEnd;

    private String event;

    private String eventDescription;
}

package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreathingSpaceEnterInfo {

    private BreathingSpaceType type;

    private String reference;

    private LocalDate start;

    private LocalDate expectedEnd;

    private String event;

    private String eventDescription;
}

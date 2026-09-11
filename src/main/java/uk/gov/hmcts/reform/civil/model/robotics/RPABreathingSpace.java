package uk.gov.hmcts.reform.civil.model.robotics;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;

import java.time.LocalDate;

@Data
public class RPABreathingSpace {

    private String reference;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate startDate;
    private BreathingSpaceType type;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate endDate;
    private String reasonForLifting;
}

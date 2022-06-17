package uk.gov.hmcts.reform.civil.model.robotics;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;

import java.time.LocalDate;

@Data
@Builder
public class RPABreathingSpace {

    private final BreathingSpaceType type;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate endDate;
}

package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BreathingSpaceEnterInfo {

    private BreathingSpaceType type;

    private String reference;

    private LocalDate start;
}


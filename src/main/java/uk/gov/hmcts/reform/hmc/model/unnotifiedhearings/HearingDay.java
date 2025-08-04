package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class HearingDay {

    private LocalDateTime hearingStartDateTime;
    private LocalDateTime hearingEndDateTime;
}

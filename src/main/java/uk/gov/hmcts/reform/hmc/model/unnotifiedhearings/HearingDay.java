package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HearingDay {

    private LocalDateTime hearingStartDateTime;
    private LocalDateTime hearingEndDateTime;
}

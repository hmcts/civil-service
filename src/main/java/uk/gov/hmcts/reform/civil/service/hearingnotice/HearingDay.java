package uk.gov.hmcts.reform.civil.service.hearingnotice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class HearingDay {

    private final LocalDateTime hearingStartDateTime;
    private final LocalDateTime hearingEndDateTime;
}

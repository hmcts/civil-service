package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class NextHearingDetails {

    private final String hearingID;
    private final LocalDateTime hearingDateTime;
}

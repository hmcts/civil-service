package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvalidHearingNoticeProcessed {

    private String hearingId;
    private Long requestVersion;
    private LocalDateTime responseReceivedDateTime;
}

package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class PartiesNotifiedServiceData {

    private boolean hearingNoticeGenerated;
    private LocalDateTime hearingDate;
    private String hearingLocation;
}

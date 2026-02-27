package uk.gov.hmcts.reform.hmc.model.unnotifiedhearings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PartiesNotifiedServiceData {

    private boolean hearingNoticeGenerated;
    private List<HearingDay> days;
    private LocalDateTime hearingDate;
    private String hearingLocation;
}

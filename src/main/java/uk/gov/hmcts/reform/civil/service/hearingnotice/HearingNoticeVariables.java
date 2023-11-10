package uk.gov.hmcts.reform.civil.service.hearingnotice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class HearingNoticeVariables implements MappableObject {

    public String hearingId;
    public Long caseId;
    public LocalDateTime hearingStartDateTime;
    public String hearingLocationEpims;
    public LocalDateTime responseDateTime;
    public String caseState;
    public List<HearingDay> days;
    public Long requestVersion;
}

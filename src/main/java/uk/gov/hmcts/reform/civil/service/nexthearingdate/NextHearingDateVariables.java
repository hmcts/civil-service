package uk.gov.hmcts.reform.civil.service.nexthearingdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.hmc.model.hearing.ListingStatus;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class NextHearingDateVariables implements MappableObject {

    public Long caseId;
    public String hearingId;
    public LocalDateTime nextHearingDate;
    public HmcStatus hmcStatus;
    public UpdateType updateType;
    public ListingStatus hearingListingStatus;
}

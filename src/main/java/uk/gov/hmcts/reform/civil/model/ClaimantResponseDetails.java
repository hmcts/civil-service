package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.dq.DQ;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class ClaimantResponseDetails {

    private DQ dq;
    private String litigiousPartyID;
    private LocalDateTime responseDate;
}

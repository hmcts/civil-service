package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.dq.DQ;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ClaimantResponseDetails {

    private DQ dq;
    private String litigiousPartyID;
    private LocalDateTime responseDate;
}
package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RequestedCourtForTabDetails {

    private String requestedCourt;
    private String requestedCourtName;
    private String reasonForHearingAtSpecificCourt;
    private YesOrNo requestHearingHeldRemotely;
    private String requestHearingHeldRemotelyReason;

}

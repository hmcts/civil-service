package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestedCourtForTabDetails {

    private String legalRep;
    private String requestedCourt;
    private YesOrNo requestHearingAtSpecificCourt;
    private String reasonForHearingAtSpecificCourt;

}

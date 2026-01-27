package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RemoteHearingLRspec {

    /**
     * Was used to say if the party chose a preferred court.
     *
     * @deprecated location is mandatory for all parties now
     */
    @Deprecated(forRemoval = true)
    private YesOrNo remoteHearingRequested;
    private String reasonForRemoteHearing;
}

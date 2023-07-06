package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class RemoteHearing {

    /**
     * Was used to say if the party chose a preferred court.
     *
     * @deprecated location is mandatory for all parties now
     */
    @Deprecated(forRemoval = true)
    private final YesOrNo hearingToBeHeldRemotely;
    private final String hearingToBeHeldRemotelyReason;
}

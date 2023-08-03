package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)

public class RemoteHearing {

    @Deprecated(forRemoval = true)
    private final YesOrNo remoteHearingRequested;
    private final String reasonForRemoteHearing;
}

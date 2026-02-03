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
public class RemoteHearing {

    @Deprecated(forRemoval = true)
    private YesOrNo remoteHearingRequested;
    private String reasonForRemoteHearing;
}

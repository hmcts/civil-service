package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

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
    @CCD(
            label = "Do you want the hearing to be held remotely?",
            hint = "This will be over telephone or video",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(forRemoval = true)
    private YesOrNo remoteHearingRequested;
    @CCD(
            label = "Briefly explain your reasons",
            showCondition = "remoteHearingRequested = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reasonForRemoteHearing;
}

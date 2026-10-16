package uk.gov.hmcts.reform.civil.service.robotics.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RoboticsEmailParams {

    private CaseData caseData;
    private boolean isMultiParty;
    private String authToken;
    /**
     * When false, Breathing Space events are omitted from the RPA payload
     * (NOTIFY_RPA_ON_CONTINUOUS_FEED). Handed-offline / adhoc feeds keep these events.
     */
    private boolean includeBreathingSpaceEvents = true;

    public RoboticsEmailParams(CaseData caseData, boolean isMultiParty, String authToken) {
        this(caseData, isMultiParty, authToken, true);
    }
}

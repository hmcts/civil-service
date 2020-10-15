package uk.gov.hmcts.reform.unspec.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;

import java.util.List;

@Data
@Builder
public class FileDirectionsQuestionnaire {

    private final List<String> explainedToClient;
    private final YesOrNo oneMonthStayRequested;
    private final YesOrNo reactionProtocolCompliedWith;
    private final String reactionProtocolNotCompliedWithReason;
}

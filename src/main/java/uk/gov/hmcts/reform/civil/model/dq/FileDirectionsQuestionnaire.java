package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDirectionsQuestionnaire {

    private List<String> explainedToClient;
    private YesOrNo oneMonthStayRequested;
    private YesOrNo reactionProtocolCompliedWith;
    private String reactionProtocolNotCompliedWithReason;
}

package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FileDirectionsQuestionnaire {

    private List<String> explainedToClient;
    private YesOrNo oneMonthStayRequested;
    private YesOrNo reactionProtocolCompliedWith;
    private String reactionProtocolNotCompliedWithReason;
}

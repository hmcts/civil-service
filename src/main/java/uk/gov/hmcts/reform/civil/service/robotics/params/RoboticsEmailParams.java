package uk.gov.hmcts.reform.civil.service.robotics.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoboticsEmailParams {

    private CaseData caseData;
    private boolean isMultiParty;
    private String authToken;
}

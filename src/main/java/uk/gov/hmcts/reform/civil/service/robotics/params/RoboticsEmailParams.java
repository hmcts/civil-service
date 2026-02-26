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
}

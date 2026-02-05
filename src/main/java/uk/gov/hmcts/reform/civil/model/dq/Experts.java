package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Experts {

    private YesOrNo expertRequired;
    private ExpertReportsSent expertReportsSent;
    private YesOrNo jointExpertSuitable;
    private List<Element<Expert>> details;

    public Experts copy() {
        return new Experts()
            .setExpertRequired(expertRequired)
            .setExpertReportsSent(expertReportsSent)
            .setJointExpertSuitable(jointExpertSuitable)
            .setDetails(details);
    }
}

package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Experts {

    private YesOrNo expertRequired;
    private ExpertReportsSent expertReportsSent;
    private YesOrNo jointExpertSuitable;
    private List<Element<Expert>> details;
}

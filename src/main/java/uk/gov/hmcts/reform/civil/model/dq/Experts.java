package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder
public class Experts {

    private final YesOrNo expertRequired;
    private final ExpertReportsSent expertReportsSent;
    private final YesOrNo jointExpertSuitable;
    private final List<Element<Expert>> details;
}

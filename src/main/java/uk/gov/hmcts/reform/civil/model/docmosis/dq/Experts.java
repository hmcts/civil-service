package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;

@Data
@Builder
public class Experts {

    private final YesOrNo expertRequired;
    private final String expertReportsSent;
    private final YesOrNo jointExpertSuitable;
    private final List<Expert> details;

}

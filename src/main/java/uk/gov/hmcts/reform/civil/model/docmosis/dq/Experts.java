package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;

@Data
@Builder
public class Experts {

    private YesOrNo expertRequired;
    private String expertReportsSent;
    private YesOrNo jointExpertSuitable;
    private List<Expert> details;

}

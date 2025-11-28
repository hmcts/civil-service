package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;

@Data
@Builder
public class LipExperts {

    private List<ExpertReportTemplate> details;
    private YesOrNo caseNeedsAnExpert;
    private String expertCanStillExamineDetails;
    private YesOrNo expertReportRequired;
}

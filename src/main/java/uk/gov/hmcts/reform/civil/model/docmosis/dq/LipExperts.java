package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;

@Data
@Builder
public class LipExperts {

    private final List<ExpertReportTemplate> details;
    private final YesOrNo caseNeedsAnExpert;
    private final String expertCanStillExamineDetails;
    private final YesOrNo expertReportRequired;
}

package uk.gov.hmcts.reform.civil.model.docmosis.dq;

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
public class LipExperts {

    private List<ExpertReportTemplate> details;
    private YesOrNo caseNeedsAnExpert;
    private String expertCanStillExamineDetails;
    private YesOrNo expertReportRequired;
}

package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertLiP {

    private YesOrNo caseNeedsAnExpert;
    private String expertCanStillExamineDetails;
    private YesOrNo expertReportRequired;
    private List<Element<ExpertReportLiP>> details;

    @JsonIgnore
    public List<ExpertReportLiP> getUnwrappedDetails() {
        return unwrapElements(details);
    }
}

package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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

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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ExpertLiP {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo caseNeedsAnExpert;
    @CCD(label = " ", searchable = false)
    private String expertCanStillExamineDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo expertReportRequired;
    @CCD(label = " ", searchable = false)
    private List<Element<ExpertReportLiP>> details;

    @JsonIgnore
    public List<ExpertReportLiP> getUnwrappedDetails() {
        return unwrapElements(details);
    }
}

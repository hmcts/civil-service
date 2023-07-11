package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RequirementsLip;

import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Data
@Builder(toBuilder = true)
public class HearingSupportLip {

    private final YesOrNo supportRequirementLip;
    private final List<Element<RequirementsLip>> requirementsLip;

    @JsonIgnore
    public List<RequirementsLip> getUnwrappedRequirementsLip() {
        return unwrapElements(requirementsLip);
    }

}

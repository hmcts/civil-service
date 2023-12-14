package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RevisedHearingRequirements {

    private YesOrNo revisedHearingRequirements;
    private String revisedHearingComments;

}

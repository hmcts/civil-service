package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeterWithoutHearing {

    private String deterWithoutHearingLabel;
    private YesOrNo deterWithoutHearingYesNo;
    private String deterWithoutHearingWhyNot;
}

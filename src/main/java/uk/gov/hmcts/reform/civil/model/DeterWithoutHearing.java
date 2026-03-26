package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeterWithoutHearing {

    private String deterWithoutHearingLabel;
    private YesOrNo deterWithoutHearingYesNo;
    private String deterWithoutHearingWhyNot;
}

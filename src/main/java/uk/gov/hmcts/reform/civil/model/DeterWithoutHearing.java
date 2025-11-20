package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class DeterWithoutHearing {

    private String deterWithoutHearingLabel;
    private YesOrNo deterWithoutHearingYesNo;
    private String deterWithoutHearingWhyNot;
}

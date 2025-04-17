package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class DeterWithoutHearing {

    private final String deterWithoutHearingLabel;
    private final YesOrNo deterWithoutHearingYesNo;
    private final String deterWithoutHearingWhyNot;
}

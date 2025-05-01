package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class HearingSupportRequirementsDJ {

    private String hearingType;
    private String hearingTypeTelephoneHearing;
    private DynamicList hearingTemporaryLocation;
    private String hearingPreferredTelephoneNumber1;
    private String hearingPreferredEmail;
    private String hearingLengthEstimate;
    private String hoursEstimateOther;
    private YesOrNo hearingUnavailableDates;
    private String[] hearingSupportRequirementsDisabledAccess;
    private String[] hearingSupportRequirementHearingLoop;
    private String[] hearingSupportRequirementInterpreter;
    private String hearingSupportRequirementSignLanguageDropdown;
    private String[] hearingSupportRequirementLanguage;
    private String hearingSupportRequirementLanguageDropdown;
    private String[] hearingSupportRequirementOther;
    private String hearingSupportRequirementOtherDropdown;
    private List<Element<HearingDates>> hearingDates;
    private YesOrNo hearingSupportQuestion;
    private String hearingSupportAdditional;
}

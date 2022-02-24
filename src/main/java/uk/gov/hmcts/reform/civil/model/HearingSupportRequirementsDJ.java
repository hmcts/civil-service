package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class HearingSupportRequirementsDJ {
    private final String hearingType;
    private final String hearingTypeTelephoneHearing;
    private final String hearingPreferredLocation;
    private final String hearingPreferredTelephoneNumber1;
    private final String hearingPreferredEmail;
    private final String hearingLengthEstimate;
    private final String hoursEstimateOther;
    private final YesOrNo hearingUnavailableDates;
    private final String[] hearingSupportRequirementsDisabledAccess;
    private final String[] hearingSupportRequirementHearingLoop;
    private final String[] hearingSupportRequirementInterpreter;
    private final String hearingSupportRequirementSignLanguageDropdown;
    private final String[] hearingSupportRequirementLanguage;
    private final String hearingSupportRequirementLanguageDropdown;
    private final String[] hearingSupportRequirementOther;
    private final String hearingSupportRequirementOtherDropdown;
    private final List<Element<HearingDates>> hearingDates;


}

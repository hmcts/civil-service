package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class HearingSupportRequirementsDJ {
    private final String HearingType;
    private final String hearingTypeTelephoneHearing;
    private final String HearingPreferredLocation;
    private final String HearingPreferredTelephoneNumber1;
    private final String HearingPreferredEmail;
    private final String HearingLengthEstimate;
    private final String HoursEstimateOther;
    private final YesOrNo HearingUnavailableDates;
    private final String[] HearingSupportRequirementsDisabledAccess;
    private final String[] HearingSupportRequirementHearingLoop;
    private final String[] HearingSupportRequirementInterpreter;
    private final String HearingSupportRequirementSignLanguageDropdown;
    private final String[] HearingSupportRequirementLanguage;
    private final String HearingSupportRequirementLanguageDropdown;
    private final String[] HearingSupportRequirementOther;
    private final String HearingSupportRequirementOtherDropdown;
    private final List<Element<HearingDates>> HearingDates;



}

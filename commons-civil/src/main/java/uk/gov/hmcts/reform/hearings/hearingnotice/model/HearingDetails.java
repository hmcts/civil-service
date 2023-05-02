package uk.gov.hmcts.reform.hearings.hearingnotice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.HearingWindowModel;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.PanelRequirementsModel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingDetails {

    private boolean autoListFlag;

    private String listingAutoChangeReasonCode;

    private String hearingType;

    private HearingWindowModel hearingWindow;

    private Integer duration;

    private List<String> nonStandardHearingDurationReasons;

    private String hearingPriorityType;

    private Integer numberOfPhysicalAttendees;

    private boolean hearingInWelshFlag;

    private List<HearingLocation> hearingLocations;

    private List<String> facilitiesRequired;

    private String listingComments;

    private String hearingRequester;

    private boolean privateHearingRequiredFlag;

    private String leadJudgeContractType;

    private PanelRequirementsModel panelRequirements;

    private boolean hearingIsLinkedFlag;

    private List<String> amendReasonCodes;

    private List<String> hearingChannels;

    private boolean isMultiDayHearing;
}

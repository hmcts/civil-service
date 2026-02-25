package uk.gov.hmcts.reform.hmc.model.hearing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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

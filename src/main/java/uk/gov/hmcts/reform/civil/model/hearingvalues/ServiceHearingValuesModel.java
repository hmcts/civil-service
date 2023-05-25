package uk.gov.hmcts.reform.civil.model.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHearingValuesModel {

    private String hmctsServiceID;
    private String hmctsInternalCaseName;
    private String publicCaseName;
    private boolean caseAdditionalSecurityFlag;
    private List<CaseCategoryModel> caseCategories;
    private String caseDeepLink;
    @JsonProperty("caserestrictedFlag")
    private boolean caseRestrictedFlag;
    private String externalCaseReference;
    private String caseManagementLocationCode;
    private String caseSLAStartDate;
    private boolean autoListFlag;
    private String hearingType;
    private HearingWindowModel hearingWindow;
    private Integer duration;
    private String hearingPriorityType;
    private Integer numberOfPhysicalAttendees;
    private boolean hearingInWelshFlag;
    private List<HearingLocationModel> hearingLocations;
    private List<String> facilitiesRequired;
    private String listingComments;
    private String hearingRequester;
    private boolean privateHearingRequiredFlag;
    private boolean caseInterpreterRequiredFlag;
    private PanelRequirementsModel panelRequirements;
    private String leadJudgeContractType;
    private JudiciaryModel judiciary;
    private boolean hearingIsLinkedFlag;
    private List<PartyDetailsModel> parties;
    private List<ScreenNavigationModel> screenFlow;
    private List<VocabularyModel> vocabulary;
    private List<String> hearingChannels;
    private CaseFlags caseFlags;
}

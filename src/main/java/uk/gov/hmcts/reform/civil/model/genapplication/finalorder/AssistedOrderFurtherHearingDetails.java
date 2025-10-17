package uk.gov.hmcts.reform.civil.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.LengthOfHearing;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.genapplication.HearingLength;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
public class AssistedOrderFurtherHearingDetails {

    private LocalDate listFromDate;
    private LocalDate listToDate;
    private LengthOfHearing lengthOfNewHearing;
    private final HearingLength lengthOfHearingOther;
    private DynamicList alternativeHearingLocation;
    private GAJudicialHearingType hearingMethods;
    private String hearingNotesText;
    private DynamicList hearingLocationList;
    private AssistedOrderDateHeard datesToAvoidDateDropdown;
    private YesOrNo datesToAvoid;

    @JsonCreator
    AssistedOrderFurtherHearingDetails(@JsonProperty("listFromDate") LocalDate listFromDate,
                                       @JsonProperty("listToDate") LocalDate listToDate,
                                       @JsonProperty("lengthOfNewHearing") LengthOfHearing lengthOfNewHearing,
                                       @JsonProperty("lengthOfHearingOther") HearingLength lengthOfHearingOther,
                                       @JsonProperty("alternativeHearingLocation")
                                       DynamicList alternativeHearingLocation,
                                       @JsonProperty("hearingMethods") GAJudicialHearingType hearingMethods,
                                       @JsonProperty("hearingNotesText") String hearingNotesText,
                                       @JsonProperty("hearingLocationList") DynamicList hearingLocationList,
                                       @JsonProperty("datesToAvoidDateDropdown") AssistedOrderDateHeard datesToAvoidDateDropdown,
                                       @JsonProperty("datesToAvoidYesNo") YesOrNo datesToAvoid) {
        this.listFromDate = listFromDate;
        this.listToDate = listToDate;
        this.lengthOfNewHearing = lengthOfNewHearing;
        this.lengthOfHearingOther = lengthOfHearingOther;
        this.alternativeHearingLocation = alternativeHearingLocation;
        this.hearingMethods = hearingMethods;
        this.hearingNotesText = hearingNotesText;
        this.datesToAvoidDateDropdown = datesToAvoidDateDropdown;
        this.hearingLocationList = hearingLocationList;
        this.datesToAvoid = datesToAvoid;
    }
}

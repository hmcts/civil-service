package uk.gov.hmcts.reform.hmc.model.hearing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyDetailsModel {

    private String partyID;
    private PartyType partyType;
    private String partyName;
    private String partyRole;
    private IndividualDetailsModel individualDetails;
    private OrganisationDetailsModel organisationDetails;
    private List<UnavailabilityDOWModel> unavailabilityDOW;
    private List<UnavailabilityRangeModel> unavailabilityRange;
    private String hearingSubChannel;
}

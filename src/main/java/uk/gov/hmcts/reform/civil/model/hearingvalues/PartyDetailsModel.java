package uk.gov.hmcts.reform.civil.model.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.hearing.PartyType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyDetailsModel {

    private String partyID;

    private PartyType partyType;

    private String partyName;

    private String partyRole;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private IndividualDetailsModel individualDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OrganisationDetailsModel organisationDetails;

    private List<UnavailabilityDOWModel> unavailabilityDOW;

    private List<UnavailabilityRangeModel> unavailabilityRanges;

    private String hearingSubChannel;
}
